'''
Created on 22.10.2010

@author: kca
'''

import shelve
import FLock
import fcntl
from path import path as Path

class LockingShelf(FLock.FLock):
	def __init__(self, filename, flag = 'c', protocol = None, writeback = False, mode = fcntl.LOCK_EX, *args, **kw):
		if not Path(filename).exists():
			s = shelve.open(filename)
			s.close()

		super(LockingShelf, self).__init__(filename = filename, mode = mode, *args, **kw)

		self.__shelf = shelve.open(filename, flag, protocol, writeback)

	def close(self):
		try:
			self.__shelf.close()
		finally:
			super(LockingShelf, self).close()

	def __getattr__(self, name):
		return getattr(self.__shelf, name)

	def __enter__(self):
		return self
	
	def __contains__(self, o):
		return self.__shelf.__contains__(o)
	
	def __setitem__(self, k, v):
		return self.__shelf.__setitem__(k, v)
	
	def __str__(self):
		return self.__shelf.__str__()
	
	def __unicode__(self):
		return self.__shelf.__unicode__()
	
	def __repr__(self):
		return self.__shelf.__repr__()