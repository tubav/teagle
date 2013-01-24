'''
Created on 01.04.2011

@author: kca
'''

from teagle.exc import TeagleError

class RepositoryError(TeagleError):
    pass

class AuthenticationError(TeagleError):
    pass

class InternalError(RepositoryError):
    pass

class UnknownEntityType(RepositoryError, TypeError):
    pass

class NoEntityFound(RepositoryError):
    pass

class Surprised(RepositoryError):
    pass

class MultipleEntitiesFound(RepositoryError):
    pass
   


