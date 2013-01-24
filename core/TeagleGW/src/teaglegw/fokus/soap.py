'''
Created on 25.08.2011

@author: kca
'''
from soaplib.serializers.primitive import String
from soaplib.service import soapmethod, SoapServiceBase
from soaplib.serializers.clazz import ClassSerializer
from teagle.t1.T1Client import BasicT1Client
from teagle.t1.T1Serializer import T1Serializer
from soaplib.client import make_service_client

class ProvisioningResponse(ClassSerializer):
	class types:
		status_code = String
		request_id = String
		config_data = String
		
	def __init__(self, status_code = "0", request_id = "0", config_data = None):
		self.status_code = status_code
		self.request_id = request_id
		self.config_data = config_data

class SOAPFrontendService(SoapServiceBase):
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def query(self, vct_id, resource_id, config_data = "", callback = None):
		raise NotImplementedError()
	
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def create(self, vct_id, resource_id, config_data, callback = None):
		raise NotImplementedError()
	
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def delete(self, vct_id, resource_id, config_data = "", callback = None):
		raise NotImplementedError()

	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def update(self, vct_id, resource_id, config_data, callback = None):
		raise NotImplementedError()

class T1SoapClient(BasicT1Client):
	def __init__(self, url, prefix = None, certfile = None, keyfile = None, *args, **kw):
		serializer = T1Serializer(self)
		super(T1SoapClient, self).__init__(url = url, prefix = prefix, serializer = serializer, *args, **kw)
		self.__t1 = make_service_client(url, SOAPFrontendService(), certfile = certfile, keyfile = keyfile)

	def _add_resource(self, parent, xml):
		resp = self.__t1.create("unknown_vct", parent, xml, None)
		_tn, cfg, _act = self.serializer.unserialize(resp)
		return self._make_entity(cfg.pop("identifier"), _tn, cfg)

	def _update_resource(self, identifier, xml):
		resp = self.__t1.update("unknown_vct", identifier, xml, None)
		_tn, cfg, _act = self.serializer.unserialize(resp)
		return self._make_entity(identifier, _tn, cfg)
		
	def _get_resource(self, identifier):
		resp = self.__t1.query("unknown_vct", identifier, "", None)
		_tn, cfg, _act = self.serializer.unserialize(resp)
		identifier = self._convert_identifier(cfg.get("identifier", identifier))
		return self._make_entity(identifier, _tn, cfg)

	def list_resources(self, identifier, typename = None):
		raise NotImplementedError("list_resources() is unavailable on SOAP interface")

	def _delete_resource(self, identifier):
		self.__t1.delete(identifier)
		