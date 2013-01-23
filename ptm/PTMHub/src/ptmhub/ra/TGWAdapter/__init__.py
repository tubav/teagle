+'''
Created on 13.08.2010

@author: kca
'''

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.Resource import GenericResource
from ptm.exc import InstanceNotFound, ConfigurationAttributeError, NotLocalError, InstanceLimitReached, NotResponsible
from teagle.tgw import LegacyTGWClient
from teagle.t1.T1Serializer import T1Serializer
import logging

logger = logging.getLogger("ptm")

class TGWAdapterSerializer(T1Serializer):
	def _unserialize_reference(self, identifier):
		pass
		

class TGWAdapter(LegacyTGWClient, AbstractResourceAdapter):
	'''
	classdocs
	'''

	def __init__(self, manager, tgw_url, prefix = None, *args, **kw):
		super(TGWAdapter, self).__init__(manager = manager, url = tgw_url, *args, **kw)
		
		self.__prefix = prefix
		
		manager.register_adapter("/ptm*", self)
		
		logger.debug("---TGW RA up---")
		
	def _convert_identifier(self, identifier, default_prefix = None):
		identifier = Identifier(identifier)
		if identifier.is_root or identifier[0].typename != "ptm":
			raise NotLocalError(str(identifier))
		#raise Exception(identifier, self.__prefix)
		if identifier[0].name == self.__prefix:
			raise NotResponsible("Not a remote resource: %s" % (identifier, ))
		return super(TGWAdapter, self)._convert_identifier(identifier[1:], identifier[0].name)
	
	def _make_entity(self, identifier, typename, config):
		identifier = Identifier("/ptm-%s%s" % (identifier.prefix, identifier.local_identifier))
		return GenericResource(adapter = self, identifier = identifier, config = config)

	def add_resource(self, parent, name, typename, config, owner = None):
		if Identifier(parent).is_root:
			raise InstanceLimitReached(typename)
		return super(TGWAdapter, self).add_resource(parent, typename, config)
	
	def get_resource(self, identifier):
		identifier = Identifier(identifier, need_full = True)
		if not identifier.parent:
			if not identifier.typename == "ptm":
				raise NotResponsible(identifier)
			return GenericResource(adapter = self, identifier = identifier, config = {})
		return  super(TGWAdapter, self).get_resource(identifier)
	
	def list_resources(self, parent, typename = None):
		if not parent or parent == "/":
			return ()
		parent = Identifier(parent, need_full = True)
		return super(TGWAdapter, self).list_resources(parent, typename)

	def have_resource(self, identifier):
		self.get_resource(identifier)
		return True

	def get_configuration(self, identifier):
		if not identifier.parent and identifier.typename == "ptm":
			return {}
		return self.get_resource(identifier)._get_configuration()
	
	set_configuration = LegacyTGWClient.update_resource
			
	def get_attribute(self, identifier, name):
		return self.get_configuration(identifier)["name"]

	def set_attribute(self, identifier, name, value):
		self.set_configuration(identifier, {name: value})

