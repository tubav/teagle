#! /usr/bin/env python

import Identifier

class PTMException(Exception):	
	__code__ = 1
	
	def __init__(self, msg = None, code = None, *args, **kw):
		if msg is not None:
			args = list(args)
			args.insert(0, str(msg))
			#args = tuple(args)
		super(PTMException, self).__init__(*args, **kw)
		if code is not None:
			self.__code__ = int(code)
			
	def get_message(self):
		return " ".join(self.args)
	message = property(get_message)
	
	def get_code(self):
		return self.__code__
	code = property(get_code)
			
	def __str__(self):
		return "%s (%d): %s" % (self.__class__.__name__, self.__code__, super(PTMException, self).__str__()) 

class FunctionUnavailableError(PTMException):
	__code__ = 2

class InternalError(PTMException):
	__code__ = 10000

class CommunicationError(PTMException, IOError):
	__code__ = 10001
	
class InternalTypeError(InternalError, TypeError):
	__code__ = 10002
	
class InternalValueError(InternalError, ValueError):
	__code__ = 10003
	
class InternalIllegalArgumentError(InternalError, ValueError):
	__code__ = 10004

class RegistryError(PTMException):
	__code__ = 1000

class LookupError(RegistryError):
	__code__ = 1001

class NoAdapterFoundError(RegistryError):
	__code__ = 1002
	
	def __init__(self, id = None, msg = None, *args, **kw):
		if msg is None:
			msg = "No adapter exists which is responsible for %s" % (id, )
		super(NoAdapterFoundError, self).__init__(msg = msg)
	
class AdapterError(PTMException):
	__code__ = 2000

class AdapterNotAvailableError(AdapterError):
	__code__ = 2001
	
class InstanceNotFound(AdapterError):
	__code__ = 2002
	
	def __init__(self, id, *args, **kw):
		super(InstanceNotFound, self).__init__(msg = "An instance with this id does not exist: %s" % (id, ), *args, **kw)

class InstanceLimitReached(AdapterError):
	__code__ = 2003

class NotResponsible(AdapterError):
	__code__ = 2004

class ConfigurationError(AdapterError):
	__code__ = 3000

class ConfigurationTypeError(ConfigurationError, TypeError):
	__code__ = 3001

class ConfigurationAttributeError(ConfigurationError, AttributeError):
	__code__ = 3002
	
	def __init__(self, id, name, *args, **kw):
		super(ConfigurationAttributeError, self).__init__(msg = "Instance with id %s has no attribute %s" % (id, name), *args, **kw)

class ConfigurationValueError(ConfigurationError, ValueError):
	__code__ = 3003
	
class DuplicateNameError(ConfigurationError):
	__code__ = 3004
	
	def __init__(self, parent, type, name, *args, **kw):
		parent = Identifier.Identifier(parent)
		super(DuplicateNameError, self).__init__(msg = "An instance of type %s with name %s already exists at %s" % (type, name, parent))
		
class ParameterAccessError(ConfigurationError):
	__code__ = 3005
	
class ParameterReadOnlyError(ParameterAccessError):
	__code__ = 3006
		
class ParameterWriteOnlyError(ParameterAccessError):
	__code__ = 3007
	
class MissingAttributeError(ConfigurationAttributeError):
	__code__ = 3008
	
class NoSuchMethodError(ConfigurationAttributeError):
	__code__ = 3009

class ApiError(PTMException):
	__code__ = 4000

class IdentifierException(ApiError):
	__code__ = 4001

class ManagerError(PTMException):
	__code__ = 5000

class NotLocalError(PTMException):
	__code__ = 5001
	
class FrontendError(PTMException):
	__code__ = 6000
	
class IllegalInputError(PTMException):
	__code__ = 6001

import logging
logger = logging.getLogger("ptm")

_exceptions = { }
__all__ = []

def _add_exception_class(c):
	assert(issubclass(c, PTMException))
	
	__all__.append(c)
	
	try:
		code = c.__dict__["__code__"]
		code = int(code)
	except ValueError, e:
		logger.error("Illegal value for __code__ of Exception class %s: %s (%s). Ignoring." % (c.__name__, code, e))
	except KeyError:
		logger.error("Exception class %s is missing the __code__ attribute. Ignoring." % (c.__name__))
	else:
		if code in _exceptions:
			logger.error("Duplicate error code for Exception classes %s and %s (%d). Ignoring." % (c.__name__, _exceptions[code].__name__, code))
		else:
			_exceptions[code] = c

	for c in c.__subclasses__():
		_add_exception_class(c)

_add_exception_class(PTMException)

def convert_exception(e):
	if isinstance(e, PTMException):
		return e
	return InternalError(msg = repr(e))

def get_exception(code, msg = None):
	if isinstance(code, BaseException):
		return convert_exception(code)
	
	try:
		code = int(code)
	except:
		raise InternalValueError(code)
		
	try:
		cls = _exceptions[code]
	except KeyError:
		try:
			cls = _exceptions[ code - code % 1000 ]
		except KeyError:
			return PTMException(msg = msg, code = code)
	except Exception, e:
		raise InternalError(msg = "Error creating exception: %s" % (e, ))
	
	e = PTMException.__new__(cls)
	e.args = (msg, )
	e.__code__ = code
	
	return e

__all__ = tuple( [ c .__name__ for c in __all__ ] + [ "get_exception", "convert_exception" ])
