'''
Created on 01.04.2011

@author: kca
'''

class TeagleError(Exception):
	pass

class InternalError(TeagleError):
	pass

class PermissionDenied(TeagleError):
	pass

class IllegalInput(TeagleError):
	pass

class IdentifierError(TeagleError):
	pass