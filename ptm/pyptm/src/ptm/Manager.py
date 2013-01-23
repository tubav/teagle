#! /usr/bin/env python

from PTMClient import PTMClient
from Identifier import Identifier
from ResourceAdapter import AbstractResourceAdapter
from exc import NoAdapterFoundError, InternalIllegalArgumentError
from Marshaller import Marshaller
from Registry import Registry

import logging

logger = logging.getLogger("ptm")

class MethodDispatcher(object):
	def __init__(self, manager, *args, **kw):
		super(MethodDispatcher, self).__init__(*args, **kw)
		self._manager = manager
		self._marshaller = Marshaller(manager)

	def add_resource(self, parent, name, typename, config, owner):
		owner = self._marshaller.unpack_owner(owner)
		config = self._marshaller.unpack_dict(config)
		r = self._manager.do_add_resource(parent, name, typename, config, owner)
		return self._marshaller.pack_identifier(r)

	def get_resource(self, identifier):
		identifier = Identifier(identifier, need_full = True)
		return self._marshaller.pack_identifier(self._manager.do_get_resource(identifier))

	def list_resources(self, parent, typename):
		typename = typename and unicode(typename) or None
		parent = (parent and parent != "/") and Identifier(parent, need_full = True) or None
		
		return [ self._marshaller.pack_identifier(i) for i in self._manager.do_list_resources(parent, typename) ]

	def have_resource(self, identifier):
		identifier = self._marshaller.unpack_identifier(identifier)
		return bool(self._manager.do_have_resource(identifier))

	def get_configuration(self, identifier):
		identifier = self._marshaller.unpack_identifier(identifier)
		config = self._manager.do_get_configuration(identifier)
		logger.debug("Got config: %s" % (config, ))
		config = self._marshaller.pack_dict(config.copy())
		logger.debug("Returnin packed config: %s" % (config, ))
		return config

	def set_configuration(self, identifier, config):
		identifier = self._marshaller.unpack_identifier(identifier)
		self._manager.do_set_configuration(identifier, self._marshaller.unpack_dict(config))

	def get_attribute(self, identifier, name):
		identifier = self._marshaller.unpack_identifier(identifier)
		return self._marshaller.pack_value(self._manager.do_get_attribute(identifier, name))

	def set_attribute(self, identifier, name, value):
		identifier = self._marshaller.unpack_identifier(identifier)
		self._manager.do_set_attribute(identifier, name, self._marshaller.unpack_value(value))

	def delete_resource(self, identifier):
		identifier = self._marshaller.unpack_identifier(identifier)
		self._manager.do_delete_resource(identifier)

	
	def execute_method(self, identifier, name, args, kw):
		identifier = self._marshaller.unpack_identifier(identifier)
		self._manager.do_execute_method(identifier, name, self._marshaller.unpack_list(args), self._marshaller.unpack_dict(kw))

	def notify(self, condition, owner, ref):
		self._manager.do_notify(condition, owner, ref)

class ManagerMethod(object):
	def __init__(self, f, *args, **kw):
		super(ManagerMethod, self).__init__(*args, **kw)
		self.__f = f
		
	def __call__(self, *args, **kw):
		name = self.__f.__name__
		if name.startswith("do_"):
			name = name[3:]
		
		logger.debug("Calling: %s(%s, %s)" % (name, args, kw))
		try:
			return self.__f(*args, **kw)
		except:
			logger.exception("Exception occurred during %s" % (name, ))
			raise

class BaseManager(object):
	def __init__(self, registry = None, *args, **kw):
		super(BaseManager, self).__init__(*args, **kw)
		if registry is None:
			registry = Registry()
		self.__registry = registry
		self.__skip_on_error = True
		
	def _get_registry(self):
		return self.__registry
	registry = property(_get_registry)
		
	def __getattribute__(self, k):
		v = super(BaseManager, self).__getattribute__(k)
		if k.startswith("do_"):
			return ManagerMethod(v)
		return v
		
	def do_add_resource(self, parent, name, typename, config, owner):
		logger.debug("add, parent: %s name: %s typename: %s config: %s owner: %s" % (parent, name, typename, config, owner))
		if not typename:
			raise InternalIllegalArgumentError("No typename given")
		if not isinstance(config, dict):
			raise InternalIllegalArgumentError("Illegal value for config: %s" % ( config, ))
		adapter_id = parent and Identifier(parent, need_full = True) / typename or Identifier(u"/" + typename)
		r = self.get_adapter(adapter_id).add_resource(Identifier(parent), name and unicode(name) or None, unicode(typename), config, owner)
		
		if isinstance(r, basestring) and not Identifier.TYPE_SEPARATOR in r:
			r = typename + Identifier.TYPE_SEPARATOR + r
		
		id = Identifier(r, need_name = True)
		
		if not id.is_absolute:
			id = Identifier(parent) / id
		
		return id

	def do_get_resource(self, identifier):
		identifier = Identifier(identifier, need_full = True)
		return self.get_adapter(identifier).get_resource(identifier)

	def do_list_resources(self, parent, typename):
#		logger.debug("do_list")
		typename = typename and unicode(typename) or None
		parent = (parent and parent != "/") and Identifier(parent, need_full = True) or None
		if typename:
			logger.debug("type given")
			return self.get_adapter(parent / typename).list_resources(parent, typename)
		result = []
		
#		logger.debug("no type given")
		adapters = self.get_adapters(parent and parent.submanager or None)
		logger.debug("Adapters: %s" % (adapters, ))
		listed = []
		for a in adapters:
			if a in listed:
				continue
			listed.append(a)

			try:
				logger.debug("Listing: %s" % (a, ))
				result += list(a.list_resources(parent, None))
			except:
				logger.exception("Error while listing resources of %s. %s." % (a, self.__skip_on_error and "Skipping" or "Aborting"))
				if not self.__skip_on_error:
					raise
				
		return result

	def do_have_resource(self, identifier):
		identifier = Identifier(identifier, need_full = True)
		return bool(self.get_adapter(identifier).have_resource(identifier))

	def do_get_configuration(self, identifier):
		identifier = Identifier(identifier, need_full = True)		
		return self.get_adapter(identifier).get_configuration(identifier)

	def do_set_configuration(self, identifier, config):
		identifier = Identifier(identifier, need_full = True)		
		self.get_adapter(identifier).set_configuration(identifier, config)
		
	def do_get_attribute(self, identifier, name):
		identifier = Identifier(identifier, need_full = True)		
		return self.get_adapter(identifier).get_attribute(identifier, name)

	def do_set_attribute(self, identifier, name, value):
		identifier = Identifier(identifier, need_full = True)		
		self.get_adapter(identifier).set_attribute(identifier, name, value)

	def do_delete_resource(self, identifier):
		identifier = Identifier(identifier, need_full = True)		
		self.get_adapter(identifier).delete_resource(identifier)

	
	def do_execute_method(self, identifier, name, args, kw):
		identifier = Identifier(identifier, need_full = True)
		self.get_adapter(identifier).execute_method(identifier, name, *args, **kw)

	def do_notify(self, condition, owner, ref):
		self.get_adapter(owner).notify(condition, owner, ref)

	def do_register(self, identifier, payload):
		self.__registry.register(identifier, payload)

	def do_unregister(self, identifier):
		self.__registry.unregister(identifier)

	def get_method_dispatcher(self):
		return MethodDispatcher(self)
		
	def get_adapter(self, id):
		logger.debug("get_adapter: %s" % (id, ))
		return self.__registry.resolve(Identifier(id, need_abs = True))

	def get_adapters(self, parent, typename = None):
		parent = parent and Identifier(parent, need_abs = True) or Identifier("/")
		if typename:
			parent = parent / typename
		#return [ a for a in self.registry.resolve_all(parent) ]
		return [ a.payload for a in self.registry.resolve_all(parent).values() ]

class Manager(PTMClient, BaseManager):
	def __init__(self, manager_url, registry_url = None, *args, **kw):
		super(Manager, self).__init__(registry_url = registry_url, *args, **kw)

		self.__url = unicode(manager_url)

	def get_client(self):
		return self
	client = property(get_client)

	def register_adapter(self, identifier, adapter):
		if not isinstance(adapter, AbstractResourceAdapter):
			raise TypeError("Need an instance of ResourceAdapter here. Got: " + str(adapter))
		self.do_register(identifier, adapter)
		self.register(identifier, self.__url)

	def responsible(self, identifier):
		try:
			self.get_local_adapter(identifier.manager_name)
			return True
		except NoAdapterFoundError:
		#	logger.exception("Not resp for : " + str(identifier))
			return False
		except:
			logger.exception("Error Not resp for : " + str(identifier))
			raise

	
