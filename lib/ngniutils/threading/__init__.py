from RWLock import RWLock, Timeout
from synchronized import synchronized

try:
	from threading import current_thread
except ImportError:
	from threading import currentThread as current_thread