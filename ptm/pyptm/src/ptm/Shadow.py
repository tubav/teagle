#! /usr/bin/env python

from ResourceAdapter import AbstractResourceAdapter
from xmlrpclib import ServerProxy, Fault
from Marshaller import Marshaller
from Identifier import Identifier
from weakref import WeakValueDictionary
from exc import InternalTypeError, InternalIllegalArgumentError
from Owners import Owners
from xmlrpclib import ResponseError, ProtocolError
from exc import CommunicationError, get_exception
from socket import error

import logging

class MethodProxy(object):
	def __init__(self, method, name, uri, *args, **kw):
		super(MethodProxy, self).__init__(*args, **kw)
		self.__method = method
		self.__name = name
		self.__uri = uri
		
	def __call__(self, *args, **kw):
		logger.debug("Calling %s(%s, %s) at %s" % (self.__name, args, kw, self.__uri))
		try:
			return self.__method(*args, **kw)
		except (ResponseError, ProtocolError, error), e:
			raise CommunicationError(e) 
		except Fault, e:
			raise get_exception(e.faultCode, e.faultString)
		
class PTMServerProxy(ServerProxy):
	def __init__(self, uri, *args, **kw):
		ServerProxy.__init__(self, uri = uri, *args, **kw)
		self.__uri = uri
		
	def __getattr__(self, name):
		return MethodProxy(method = ServerProxy.__getattr__(self, name), name = name, uri = self.__uri)

	def get_uri(self):
		return self.__uri
	uri = property(get_uri)

logger = logging.getLogger("ptm")

class Shadow(AbstractResourceAdapter):
	def __init__(self, client, uri, *args, **kw):
		#logger.debug(self.__class__.__mro__)
		super(Shadow, self).__init__(parent = None, manager = None, *args, **kw)
		#logger.debug("Creating shadow for %s" % (uri, ))
		self.__marshaller = Marshaller(client)
		#self._proxy = PTMServerProxy(uri, allow_none = True)
		self.__uri = uri

	def get_uri(self):
		return self.__uri
	uri = property(get_uri)
	
	@property
	def _proxy(self):
		return PTMServerProxy(self.uri, allow_none = True)

	def _mangle_id(self, id):
		if Identifier(id) == Identifier("/"):
			return None
		if id is not None:
			return unicode(Identifier(id, need_full = True))
		return None

	def _mangle_config(self, config):
		if config is None:
			return {}
		if not isinstance(config, dict):
			raise InternalTypeError("Expecting dict, not %s" % (config, ))
		return self.__marshaller.pack_dict(config)

	def add_resource(self, parent_id, name, typename, config, owner = None):
		identifier = self._proxy.add_resource(self._mangle_id(parent_id), name and unicode(name) or None, typename and unicode(typename) or None, 
				self._mangle_config(config), owner and self.__marshaller.pack_value(owner) or None)
		return self.__marshaller.make_proxy(identifier)

	def get_resource(self, identifier):
		return self.__marshaller.make_proxy(self._proxy.get_resource(unicode(self.__marshaller.pack_identifier(identifier))))

	def have_resource(self, identifier):
		return bool(self._proxy.have_resource(unicode(self.__marshaller.pack_identifier(identifier))))

	def list_resources(self, parent_id, type = None):
		return [ self.__marshaller.make_proxy(i) for i in self._proxy.list_resources(self._mangle_id(parent_id), type and type or None) ]

	def get_configuration(self, identifier):
		return self.__marshaller.unpack_dict(self._proxy.get_configuration(unicode(Identifier(identifier, need_full =True))))

	def set_configuration(self, identifier, config):
		self._proxy.set_configuration(self.__marshaller.pack_identifier(identifier), self.__marshaller.pack_dict(config))

	def get_attribute(self, identifier, name):
		return self.__marshaller.unpack_value(self._proxy.get_attribute(self.__marshaller.pack_identifier(identifier), unicode(name)))

	def set_attribute(self, identifier, name, value):
		self._proxy.set_attribute(self.__marshaller.pack_identifier(identifier), unicode(name), self.__marshaller.pack_value(value))

	def delete_resource(self, identifier):
		self._proxy.delete_resource(self.__marshaller.pack_identifier(identifier))

	def execute_method(self, identifier, name, *args, **kw):
		print "############################### id %s name %s args %s kw %s "%(identifier, name, args, kw,)
		return self.__marshaller.unpack_value(self._proxy.execute_method(self.__marshaller.pack_identifier(identifier), unicode(name), self.__marshaller.pack_list(args), self.__marshaller.pack_dict(kw)))

	

	def notify(self, condition, owner, reference):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = unicode(Identifier(owner, need_abs = True))
		reference = self.__marshaller.pack_identifier(reference)
		self._proxy.notify(condition, owner, reference)

	def __str__(self):
		return "RA at %s" % (self.uri, )
	
	def __unicode__(self):
		return unicode(self.__str__())
	
	def __eq__(self, o):
		try:
			return self.uri == o.uri
		except AttributeError:
			return False

class HubShadow(Shadow):
	def signal(self, condition, identifier):
		identifier = Identifier(identifier, need_full = True)
		self._proxy.signal(condition, identifier)
		
	def acquire_resource(self, identifier, owner):
		self._proxy.acquire_resource(self.__marshaller.pack_identifier(identifier), owner and self.__marshaller.pack_value(owner) or None)

	def release_resource(self, identifier, owner):
		self._proxy.release_resource(self.__marshaller.pack_identifier(identifier), owner and self.__marshaller.pack_value(owner) or None)

	def subscribe(self, condition, owner, identifier):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = Identifier(owner, need_abs = True)
		identifier = Identifier(identifier, need_full = True)	
		self._proxy.subscribe(condition, owner, unicode(identifier))

	def unsubscribe(self, condition, owner, identifier):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = Identifier(owner, need_abs = True)
		identifier = Identifier(identifier, need_full = True)	
		self._proxy.unsubscribe(condition, owner, unicode(identifier))

	def get_owners(self, identifier):
		identifier = self.__marshaller.pack_identifier(identifier)
		return Owners(self._proxy.get_owners(identifier))
	
	def register(self, identifier, url):
		if not identifier:
			raise InternalIllegalArgumentError(identifier)
		identifier = Identifier(identifier, need_abs = True)
		self._proxy.register(unicode(identifier), unicode(url))
		
	def unregister(self, identifier):
		if not identifier:
			raise InternalIllegalArgumentError(identifier)
		identifier = Identifier(identifier, need_abs = True)
		self._proxy.unregister(unicode(identifier))
		
	def get_client(self):
		return self

		
class ShadowManager(object):
	def __init__(self, client, *args, **kw):
		super(ShadowManager, self).__init__(*args, **kw)
		self.__shadows = WeakValueDictionary()
		self.__client = client
		
	def get_client(self):
		return self.__client
	client = property(get_client)
		
	def get_shadow(self, url):
		try:
			return self.__shadows[url]
		except KeyError:
			s = self._make_shadow(url)
			self.__shadows[url] = s
			return s
			
	def _make_shadow(self, url):
		return Shadow(client = self.__client, uri = url)
	
class HubShadowManager(ShadowManager):
	def __init__(self, client, hub_url, *args, **kw):
		super(HubShadowManager, self).__init__(client = client, *args, **kw)
		self.__hub_url = hub_url
	
	def _make_shadow(self, url):
		if url == self.__hub_url:
			return HubShadow(client = self.client, uri = url)
		return super(HubShadowManager, self)._make_shadow(url)
	
	def get_hub(self):
		return self.get_shadow(self.__hub_url)
	
