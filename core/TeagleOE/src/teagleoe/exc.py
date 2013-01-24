'''
Created on 10.07.2011

@author: kca
'''

from teagle.exc import TeagleError, InternalError

class OEError(TeagleError):
	pass

class ParseError(OEError):
	pass

class ModelError(ParseError):
	pass

class InternalError(OEError, InternalError):
	pass

class OperationNotFoundError(InternalError):
	pass