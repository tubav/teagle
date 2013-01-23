#! /usr/bin/env python
'''
Created on 11.07.2010

@author: kca
'''

import logging
from ptm import Identifier
from ptm.exc import NoAdapterFoundError, AdapterNotAvailableError
import threading
import collections

logger = logging.getLogger("ptm")

PayloadEntryTuple = collections.namedtuple("PayloadEntryTuple", ("payloadentry", "is_wildcard"))

class PayloadEntry(object):
    def __init__(self, id, payload, available = True, *args, **kw):
        super(PayloadEntry, self).__init__(*args, **kw)
        assert(payload is not None)
        self.__id = id
        self.__payload = payload
        self.__available = available
        
    def get_id(self):
        return self.__id
    id = property(get_id)
        
    def get_is_available(self):
        return self.__available
    is_available = property(get_is_available)
    
    def get_payload(self):
        return self.__payload
    payload = property(get_payload)
        
class Registry(object):
    class __Node(object):
        def __init__(self):
            self.children = {}
            self.wildcard_children = {}
            self.type_payload = {}
            self.instance_payload = {}

        def get_type_payload(self, type):
            try:
                return self.type_payload[type][0]
            except KeyError:
                try:
                    return self.type_payload[None][0]
                except KeyError:
                    return None
            
        def get_payload(self, idpart):
            idpart = Identifier(idpart)
            if not idpart.is_adapter:
                try:
                    return self.instance_payload[idpart]
                except KeyError:
                    pass
                
            return self.get_type_payload(idpart.typename)
        
        def get_wildcard_payload(self, idpart):
            pl = self.get_payload(idpart)
            if pl is not None and pl.id.is_wildcard:
                return pl
            return None
        
        def add_payload(self, id, payload, available = True):
#            logger.debug("adding payload: %s %s -> %s" % (self, id, payload))
            if id.is_adapter:
                old = self.type_payload.get(id.typename, (None, ))[0]
                self.type_payload[id.typename] = (PayloadEntry(id, payload, available), id.is_wildcard)
            else:
                old = self.instance_payload.get(id, None)
                self.instance_payload[id[-1]] = PayloadEntry(id, payload, available)

            if old is not None:
                logger.debug("Replacing old entry: %s" % (old.payload, ))

    def __init__(self, *args, **kw):
        super(Registry, self).__init__(*args, **kw)
        self.__good = threading.Event()
        self.__good.set()
        self.__root = Registry.__Node()

    def register(self, identifier, url):
        logger.info("Register: %s ==> %s" % (identifier, url))
        if not url:
            raise ValueError("No url given")
        
        identifier = Identifier(identifier)
        if not identifier.is_absolute:
            raise ValueError("I need an absolute identifier, not this: " + identifier)
            
        node = self.__root
        self.__good.clear()
        parts = identifier[:-1]
        logger.debug("prts: %s" % (parts, ))
        try:
            for part in identifier[:-1]:
                try:
                    #logger.debug("getting child for %s from %s" % (part, node))
                    if part.endswith(Identifier.WILDCARD):
                        node = node.wildcard_children[unicode(part)[:-1]]
                    else:
                        node = node.children[part]
                #    logger.debug("got child")
                except KeyError:
                    n = Registry.__Node()
                    if part.endswith(Identifier.WILDCARD):
                        #logger.debug("wccreate")
                        node.wildcard_children[unicode(part)[:-1]] = n
                    else:
                        node.children[part] = n
                    node = n
            
            node.add_payload(identifier, url)
        finally:
            self.__good.set()
          
        return url

    def resolve(self, identifier):
        entry = self.resolve_payload_entry(identifier)
        if not entry.is_available:
            raise AdapterNotAvailableError(identifier)
        return self._mangle_payload(entry.payload)

    def resolve_payload_entry(self, identifier):
        identifier = Identifier(identifier)
        #logger.debug(u"Resolving: " + unicode(identifier))
            
        node = self.__root
        result = None

        self.__good.wait()
        parts = identifier[:-1]
        for i, part in enumerate(parts, 1):
            payload = node.get_wildcard_payload(part.typename)
            if payload is not None:
                result = payload

            try:
                node = node.children[part]
            except KeyError:
                if i == len(parts):
                    try:
                        node = node.wildcard_children[part.typename]
                    except KeyError:
                        break
                else:
                    break
        else:
            payload = node.get_payload(identifier[-1])
            if payload is not None:
                result = payload

        if result is None:
            raise NoAdapterFoundError(identifier)            
        return result

    def resolve_all(self, identifier):
        identifier = Identifier(identifier)

#        logger.info("Resolving all: " + unicode(identifier))

        node = self.__root
        current = Identifier(Identifier.SEPARATOR)
        result = {}

        self.__good.wait()
        parts = identifier[:-1]
        for i, part in enumerate(parts, 1):
            logger.debug(part)
            if None in node.type_payload and node.type_payload[None][1]:
                if identifier.typename:
                    result = {}
                result[current / Identifier.WILDCARD] = node.type_payload[None][0]
            for typename, payload in node.type_payload.iteritems():
                if payload[1] and typename == part.typename and payload[0] not in result.values():
                    id = current / typename / Identifier.WILDCARD
                    assert(id not in result)
                    if identifier.typename:
                        result = {}
                    result[id] = payload[0]
                    
            try:
#                logger.debug("getting child %s form %s" % (part, node.children, ))
                node = node.children[part]
#                logger.debug("have child")
                current = (current / part).submanager
            except KeyError:
                if i == len(parts):
                    try:
#                        logger.debug("trying wildcard child")
                        node = node.wildcard_children[part.typename]
#                        logger.debug("have child")
                        #current = (current / part.typename).submanager
                        current = current / (part.typename + Identifier.WILDCARD + Identifier.SEPARATOR)
                    except KeyError:
                        break
                else:
                    break

        else:
#            logger.debug("traversed till end %s %s %s"% (node, node.instance_payload, node.type_payload))
            #logger.debug("through------")
            if not identifier.is_adapter:
                try:
                    payload = node.instance_payload[identifier[-1]]
                    return {identifier: payload} 
                except KeyError:
                    pass
                
            catchall = None
            result2 = {}
            if None in node.type_payload:
                catchall = current / (node.type_payload[None][1] and Identifier.WILDCARD or '')
                result2[catchall] = node.payload[None][0]
            for typename, payload in node.type_payload.iteritems():
                if typename is not None and (typename == identifier.typename or not identifier.typename) and payload[0] not in result2.values():
                    id = current / typename / (payload[1] and Identifier.WILDCARD or '')
                    #logger.debug(id)
                    #logger.debug(typename)
                    #logger.debug(payload)
                    #logger.debug(result)
                    assert(id not in result)
                    if not identifier.is_adapter:
                        return {id: payload[0]}
                    result2[id] = payload[0]
                    if identifier.typename:
                        result2.pop(catchall, None)
                        break
            
            if identifier.is_adapter:
                for idpart, payload in node.instance_payload.iteritems():
                    if (not identifier.typename or idpart.typename == identifier.typename) and payload not in result2.values():
                        id = current / idpart
                        assert(id not in result)
                        result2[id] = payload
                        if identifier.typename:
                            result2.pop(catchall, None)
            
            if result2:
                result = result2                

        if not result:
            raise NoAdapterFoundError(identifier)

        #logger.debug("Returning result: %s" % (result, ))
        return result
            
    def _mangle_dict(self, result):
        for k, v in result.items():
            if not v.is_available:
                del result[k]
            else:
                result[k] = self._mangle_payload(v.payload)
    
    def _mangle_payload(self, payload):
        return payload
    
def main():
    import SimpleXMLRPCServer
    console = logging.StreamHandler()
    formatter = logging.Formatter('Registry: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
    console.setFormatter(formatter)
    console.setLevel(logging.DEBUG)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(console)

    server = SimpleXMLRPCServer.SimpleXMLRPCServer(("0.0.0.0", 8000))
    logger.info("Starting...")
    server.register_instance(Registry())

    try:
        server.serve_forever()
    except (KeyboardInterrupt, KeyboardInterrupt):
        print ("Exiting...")
    
if __name__ == "__main__":
    main()
    
    
