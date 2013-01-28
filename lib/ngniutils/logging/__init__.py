'''
Created on 15.07.2011

@author: kca
'''
import logging
from types import ClassType
from ..threading import current_thread
from logging import DEBUG, Filter
from ngniutils.collections import get_iterable

def get_logger(loggername = None):
	logging.basicConfig(level=logging.DEBUG)
	if loggername:
		if not isinstance(loggername, basestring):
			if not isinstance(loggername, (type, ClassType)):
				klass = loggername.__class__ 
			else:
				klass = loggername
			loggername = klass.__module__ + "." + klass.__name__
	else:
		loggername = __name__
		
	try:
		logger = logging.getLogger(loggername)
	except Exception, e:
		print ("Failed to get logger '%s': %s" % (loggername, e))
		raise

	logger.setLevel(logging.DEBUG)
	return logger
	
class LoggerMixin(object):
	def get_logger(self):
		try:
			return self.__logger
		except AttributeError:
			self.__logger = get_logger(self)
			return self.__logger
	def set_logger(self, logger):
		self.__logger = logger
	logger = property(get_logger, set_logger)
		
	@classmethod
	def get_class_logger(cls):
		try:
			return cls.__clslogger
		except AttributeError:
			cls.__clslogger = get_logger(cls)
			return cls.__clslogger
		
	def __getstate__(self):
		l = getattr(self, "_LoggerMixin__logger", None)
		self.__logger = None
		try:
			sgs = super(LoggerMixin, self).__getstate__
		except AttributeError:
			state = self.__dict__.copy()
		else:
			state = sgs()
		self.__logger = l
		return state
		
class ThreadFilter(Filter):
	def __init__(self, thread = None, name = ''):
		Filter.__init__(self, name = name)
		self.thread = thread or current_thread()
		
	def filter(self, record):
		return current_thread() == self.thread
	
class ErrorLogger(LoggerMixin):
	def __init__(self, name = "operation", logger = None, level = DEBUG, *args, **kw):
		super(ErrorLogger, self).__init__(*args, **kw)
		if logger is not None:
			self.logger = logger
		self.name = name
		self.level = level
		assert level is not None
			
	def __enter__(self):
		self.logger.debug("Entering %s", self.name)
		return self
			
	def __exit__(self, type, value, traceback):
		if type is not None:
			self.logger.exception("Error in %s", self.name)
		else:
			self.logger.log(self.level, "%s finished", self.name)
			
def log_errors(f):
	def _f(*args, **kw):
		with ErrorLogger(f.__name__):
			result = f(*args, **kw)
		get_logger(f).debug("%s returning: %s", f.__name__, result)
		return result
	_f.__name__ = f.__name__
	return _f

def sanitize_dict(d, keys = ("password", ), replacement = "*", inplace = False):
	keys = get_iterable(keys)
	if not inplace:
		d = dict(d)
	
	if replacement is None:
		for k in keys:
			d.pop(k, None)
	else:
		for k in keys:
			v = d[k]
			if isinstance(v, basestring):
				d[k] = replacement * len(v)
			else:
				d[k] = replacement
	return d
			
	