'''
Created on 25.07.2011

@author: kca
'''

from Controller import ReflectiveController
from exc import MissingParameter

class ParameterBasedController(ReflectiveController):
	def __init__(self, name, parameter_name = "op", *args, **kw):
		super(ParameterBasedController, self).__init__(name = name, *args, **kw)
		self.parameter_name = parameter_name
	
	def _handle_request(self, request):
		try:
			method = self._get_param(self.parameter_name)
		except MissingParameter:
			return self._default_handler()
		
		return super(ParameterBasedController, self)._handle_request(request, method)
		
	def _default_handler(self):
		raise MissingParameter(self.parameter_name)
		