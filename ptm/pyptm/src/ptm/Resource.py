#! /usr/bin/env python

from Identifier import Identifier

class Resource(object):
	def __init__(self, adapter, identifier = None, parent = None, type = None, name = None, *args, **kw):
		super(Resource, self).__init__(*args, **kw)

		self.__adapter = adapter

		if identifier is None:
			if name is None:
				#raise PTMException("No name given")
				name = adapter.generate_name(parent, type)
			identifier = Identifier(parent).make_child_identifier(type, name)
		else:
			identifier = Identifier(identifier, need_full = True)

		self.__identifier = identifier

	def get_adapter(self):
		return self.__adapter
	def __get_adapter(self):
		return self.get_adapter()
	adapter = property(__get_adapter)

	def get_identifier(self):
		return self.__identifier
	identifier = property(get_identifier)

	def get_typename(self):
		return self.identifier.typename
	typename = property(get_typename)

	def get_name(self):
		return self.identifier.resourcename
	name = property(get_name)

	def get_client(self):
		return self.adapter.client
	def __get_client(self):
		return self.get_client()
	client = property(__get_client)

	def get_parent(self):
		parent_id = self.parent_id
		if parent_id is None:
			return None
		#raise Exception(self.identifier, parent_id)
		return self.adapter.client.get_resource(parent_id)
	parent = property(get_parent)

	def get_parent_id(self):
		parent_id = self.identifier.parent
		if parent_id.is_root:
			return None
		return parent_id
	parent_id = property(get_parent_id)

	def get_configuration(self):
		return self.adapter.get_configuration(self.identifier)
	config = configuration = property(get_configuration)

	def get_attribute(self, k):
		return self.adapter.get_attribute(self.identifier, k)

	def set_configuration(self, config):
		self.adapter.set_configuration(self.identifier, config)

	def set_attribute(self, k, v):
		self.adapter.set_attribute(self.identifier, k, v)

	def acquire(self, owner, weak = False):
		self.adapter.acquire_resource(self.identifier, owner, weak)

	def release(self, owner):
		self.adapter.release_resource(self.identifier, owner)

	def get_owners(self):
		return self.adapter.get_owners(self.identifier)
	owners = property(get_owners)
	
	def delete(self):
		return self.adapter.delete_resource(self.identifier)
	
	def execute_method(self,identifier, name, *args, **kw):
		return self.adapter.execute_method(self.identifier, name, *args, **kw)

class BasicResource(Resource):
	def get_client(self):
		return self.adapter.client

	def _get_configuration(self):
		raise NotImplementedError()

	def _set_attribute(self, k, v):
		raise NotImplementedError()

class DefaultResource(BasicResource):
	def __init__(self, adapter, name = None, identifier = None, parent = None, type = None, *args, **kw):
		if identifier is None:
			if type is None:
				type = self.get_typename()
			if parent is None:
				parent = adapter.base_id
			else:
				parent = Identifier(parent)
				if not parent.is_absolute:
					parent = adapter.base_id / parent
			if name is None:
				name = adapter.generate_name(parent_id = parent, typename = type)
		elif not identifier.is_absolute:
			identifier = adapter.base_id / identifier
		super(DefaultResource, self).__init__(name = name, identifier = identifier, type = type, adapter = adapter, parent = parent, *args, **kw)

	@classmethod
	def get_typename(klass):
		return klass.__name__.lower()

class GenericResource(BasicResource):
	def __init__(self, adapter, config, identifier = None, *args, **kw):
		id = config.pop("identifier", None)
		if identifier is None:
			identifier = id
		super(GenericResource, self).__init__(adapter = adapter, identifier = identifier, *args, **kw)
		self.__config = config

	def _get_configuration(self):
		if self.__config is None:
			return self.adapter.get_configuration(self.identifier)
		return self.__config

	def _set_attribute(self, k, v):
		if k not in self.__config:
			raise AttributeError(k)
		self.__config[k] = v

class ResourceProxy(Resource):
	def get_adapter(self):
		#TODO: ouch. Rethink.
		return super(ResourceProxy, self).get_adapter().get_hub()
