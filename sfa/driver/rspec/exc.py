'''
Created on 06.01.2012

@author: sha
'''
from teaglesfa.exc import TeagleSFAError	

class UnknownRspecId(TeagleSFAError):
	pass

class NoInstanceFound(TeagleSFAError):
	pass

class MultibleInstancesFound(TeagleSFAError):
	pass

class NoPnodeError(TeagleSFAError):
	pass

class RSpecError(TeagleSFAError):
	pass