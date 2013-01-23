'''
Created on 07.10.2010

@author: kca
'''

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import DuplicateNameError, ConfigurationAttributeError, InstanceNotFound
import logging

logger = logging.getLogger("ptm")

class MininetBlocksAdapter(AbstractResourceAdapter):
    def __init__(self, manager, parent, *args, **kw):
        super(MininetBlocksAdapter, self).__init__(*args, **kw)
        parent_id = Identifier(parent)
        manager.register_adapter(parent_id / "mininetswitch", self)
        manager.register_adapter(parent_id / "mininetcontroller", self)
        manager.register_adapter(parent_id / "mininethost", self)
        self.__parent_id = parent_id
        
        self.__hosts = {}
        self.__switches = {}
        
    def add_resource(self, parent_id, name, typename, config, owner = None):
        assert(self.__parent_id == parent_id)
        assert(typename in ("mininetswitch", "mininetcontroller", "mininethost"))
        
        if typename == "mininetswitch":
            name = self.__generate_name("s", self.__switches)
            
            peers = config["PEERS"]
            
            for p in peers:
                ip = p.get_attribute("IP")
                print "Connecting: " + ip
            
        elif typename == "mininethost":
            name = self.__generate_name("h", self.__hosts)
            
            ip = config["IP"]
            #add this point instantiate the host in mininet
            self.__hosts[name] = ip
            
            return "mininethost-%s" % (name, )

    def get_configuration(self, identifier):
        if  identifier.typename == "mininethost":
            ip = self.__hosts("h%s" % (identifier.resourcename, ))
            config = {}
            config["IP"] = ip
            return config
        elif identifier.typename == "mininetswitch":
            return {}

    def get_attribute(self, identifier):
        return None

    def __generate_name(self, prefix, instances):
        n = 0
        while True:
            name = "%s%d" % (prefix, n)
            n += 1
            if name not in instances:
                return name
            
            
            