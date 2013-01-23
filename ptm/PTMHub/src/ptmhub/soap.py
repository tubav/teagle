'''
Created on 25.08.2011

@author: kca
'''
from soaplib.wsgi_soap import SimpleWSGISoapApp
from soaplib.serializers.primitive import String
from soaplib.service import soapmethod
from soaplib.serializers.clazz import ClassSerializer
from FrontendModule import T1FrontendModule
import re

class ProvisioningResponse(ClassSerializer):
	class types:
		status_code = String
		request_id = String
		config_data = String
		
	def __init__(self, status_code = "0", request_id = "0", config_data = None):
		self.status_code = status_code
		self.request_id = request_id
		self.config_data = config_data

class SOAPFrontend(T1FrontendModule, SimpleWSGISoapApp):
	__method_pattern = re.compile(r".+<soapenv:Body>\s*<(?:.*?:)?([a-z_][a-z0-9_]+)\s.*", re.I | re.M)
	
	def _get_identifier(self, identifier):
		if not identifier.startswith("/"):
			pos = identifier.find("./")
			if (pos < 0 and identifier.find(".") != len(identifier) - 1) or pos > identifier.find("/") :
				identifier = "/" + identifier 
		return super(SOAPFrontend, self)._get_identifier(identifier)
	
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def query(self, vct_id, resource_id, config_data = None, callback = None):
		if vct_id == "status_check":
			return ProvisioningResponse(config_data = "top-0")

		return ProvisioningResponse(config_data = self.get_resource(resource_id))
	
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def create(self, vct_id, resource_id, config_data, callback = None):
		typename, config, _action = self.serializer.unserialize(config_data)
		result = self.add_resource(resource_id, typename, config)
		return ProvisioningResponse(config_data = result)
	
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def delete(self, vct_id, resource_id, config_data = None, callback = None):
		self.delete_resource(resource_id)
		return ProvisioningResponse()
	
	@soapmethod(String, String, String, String,_returns=ProvisioningResponse)
	def update(self, vct_id, resource_id, config_data = None, callback = None):
		_typename, config, _action = self.serializer.unserialize(config_data)
		result = self.update_resource(resource_id, config)
		return ProvisioningResponse(config_data = result)

	def onMethodExec(self,environ,body,py_params,soap_params):
		r = self.__method_pattern.match(body)
		method = r and r.group(1) or "<unknown>"
		self.logger.debug("Calling method %s with %s", method, py_params)
		
	def onException(self,environ,exc,resp):
		self.logger.exception("Error in SOAPFrontend")
		super(SOAPFrontend, self).onException(environ, exc, resp)
		