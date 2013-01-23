'''
Created on 25.08.2011

@author: kca
'''
from ptm.t1client.BaseSerializer import BaseSerializer
from ptm.PTMModule import PTMModule
from ptm.PTMClient import PTMClient
from ptm.t1client.GlobalIdentifier import GlobalIdentifier
from ptm.Identifier import Identifier

class FrontendModuleSerializer(BaseSerializer):	
	def __init__(self, client, prefix, *args, **kw):
		super(FrontendModuleSerializer, self).__init__(prefix = prefix, *args, **kw)
		self.__client = client

	def _unserialize_reference(self, v):
			#raise Exception(v)
			v = unicode(v)

			self.logger.debug("unser ref: %s (%s)" % (v, self.prefix))

			if v.startswith("//"):
				ptm_name, sep, id = v[2:].partition("/")
				if not ptm_name:
					raise ValueError("Illegal id: %s" % v)
				if ptm_name == self.ptm_name:
					v = sep + id
				else:
					v = "/ptm-" + ptm_name + sep + id
			elif not v.startswith("/"):
				ps = v.find("./")
				if ps > 0 and ps <= v.find("/") + 1:
					prefix, v = v.split("./", 1)
					if prefix != self.prefix[:-1]:
						v = "/ptm-" + prefix + "/" + v
					else:
						v = "/" + v
			return self.__client.get_resource(v)
		
class T1FrontendModule(PTMModule):
	def __init__(self, huburl = None, prefix = None, *args, **kw):
		super(T1FrontendModule, self).__init__(*args, **kw)
		self.client = PTMClient(huburl)
		self.serializer = FrontendModuleSerializer(self.client, prefix)
		self.prefix = prefix
		
	def _get_identifier(self, identifier):
		identifier = GlobalIdentifier(identifier, default_prefix = self.prefix)

		if identifier.prefix != self.prefix:
			return Identifier("/ptm-" + identifier.prefix) / Identifier(identifier)
	
		return Identifier(identifier)
	
	def get_resource(self, identifier):
		identifier = self._get_identifier(identifier)
		return self._get_resource(identifier)
	
	def _get_resource(self, identifier):
		resource = self.client.get_resource(identifier)
		return self.serializer.dumps(resource)
	
	def add_resource(self, parent_id, typename, config):
		parent_id = self._get_identifier(parent_id)
		config.pop("identifier", None)
		resource = self.client.add_resource(parent_id, None, typename, config)
		return self.serializer.dumps(resource)
	
	def delete_resource(self, identifier):
		identifier = self._get_identifier(identifier)
		resource = self.client.get_resource(identifier)
		resource.delete()
		
	def update_resource(self, identifier, config):
		identifier = self._get_identifier(identifier)
		resource = self.client.get_resource(identifier)
		config.pop("identifier", None)
		resource.set_configuration(config)
		return self.serializer.dumps(resource)
	
	def execute_method_resource(self, identifier, name, **params):
		identifier = self._get_identifier(identifier)
		resource = self.client.get_resource(identifier)
		params.pop("identifier", None)
		resource.execute_method(identifier, name, **params)
		return self.serializer.dumps(resource)
	
	