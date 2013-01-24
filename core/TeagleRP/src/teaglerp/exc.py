'''
Created on 24.08.2011

@author: kca
'''

from teagle.exc import TeagleError, InternalError, IllegalInput

class RPError(TeagleError):
	pass

class RPInternalError(RPError, InternalError):
	pass

class RPIllegalInput(RPError, IllegalInput):
	pass

class NoPtmFound(RPError):
	pass