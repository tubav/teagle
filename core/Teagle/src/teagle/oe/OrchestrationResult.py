'''
Created on 25.07.2011

@author: kca
'''

from collections import namedtuple

class OrchestrationResult(namedtuple("OrchestrationResultTuple", ("status", "message", "idmapping", "log_url", "log"))):
	def __new__(cls, status, message, idmapping = {}, log_url = "", log = None, *args, **kw):
		assert log is not None
		assert log.entries is not None
		return super(OrchestrationResult, cls).__new__(cls, status = int(status), message = message, idmapping = idmapping, log_url = log_url, log = log)

	@property
	def successful(self):
		return self.status == 0