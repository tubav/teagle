from .logging import LoggerMixin
	
Base = LoggerMixin
	
class NOT_SET(object):
	__slots__ = ()
	
	def __bool__(self):
		return False
	__nonzero__ = __bool__
	
	def __str__(self):
		return ""
	
NOT_SET = NOT_SET()

DEFAULT_ENCODING = "utf-8"
DEFAULT_CHUNK_SIZE= 16 * 1024
BASE_STR = unicode
THREADSAFE = True

def noop(*args, **kw):
	pass

def not_implemented(*args, **kw):
	raise NotImplementedError()

def uc(s):
	if isinstance(s, unicode):
		return s
	if isinstance(s, basestring):
		return s.decode(DEFAULT_ENCODING)
	return unicode(s)

def encstr(s):
	if isinstance(s, str):
		return s
	if not isinstance(s, unicode):
		s = unicode(s)
	return s.encode(DEFAULT_ENCODING)
	
def tostr(o):
	if isinstance(o, basestring):
		return o
	return BASE_STR(o)

def identity(x):
	return x

_isc = issubclass
def issubclass(o, classes):
	return isinstance(o, type) and _isc(o, classes)

class ObjectProxy(object):
	def __init__(self, proxyobject, *args, **kw):
		super(ObjectProxy, self).__init__(*args, **kw)
		self._o = proxyobject
		
	def __getattr__(self, k):
		return getattr(self._o, k)
