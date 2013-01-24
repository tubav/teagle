'''
Created on 26.07.2011

@author: kca
'''

from ..exc import TeagleError

class T1Error(TeagleError):
	pass

class T1IdentifierError(T1Error):
	pass

class T1ErrorResponse(T1Error):
	def __init__(self, status, message, log = None, *args, **kw):
		super(T1ErrorResponse, self).__init__(status, message, log, *args, **kw)
		
	@property
	def status(self):
		return self.args[0]
	
	@property
	def message(self):
		return self.args[1]
	
	@property
	def log(self):
		return self.args[2]
	
	def __iter__(self):
		return self.args[:3]
