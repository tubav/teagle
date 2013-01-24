from exc import T1IdentifierError
from abc import ABCMeta, abstractmethod, abstractproperty
from collections import namedtuple
			
class T1Interface(object):
	__metaclass__ = ABCMeta
	
	@abstractmethod
	def add_resource(self, parent, typename, config):
		raise NotImplementedError()
	
	@abstractmethod
	def get_resource(self, identifier):
		raise NotImplementedError()
	
	@abstractmethod
	def update_resource(self, identifier, config):
		raise NotImplementedError()
	
	@abstractmethod
	def delete_resource(self, identifier):
		raise NotImplementedError()	
	
	@abstractmethod
	def list_resources(self, parent, typename = None):
		raise NotImplementedError()

	@abstractmethod
	def execute_method(self, identifier, name, **kwargs):
		raise NotImplementedError()
	
class ProvisioningResult(namedtuple("ProvisioningResult", ("status", "message", "log"))):
	def __new__(cls, status, message, log = None, *args, **kw):
		return super(ProvisioningResult, cls).__new__(cls, status = int(status), message = message, log = log)

	@property
	def successful(self):
		return self.status == 0
	