'''
Created on 30.04.2011

@author: kca
'''

import os
from os import stat
from teagle.repository.tssg import TSSGRepository 
from ngniutils.multiprocess import RWLock

class SynchronizedTSSGRepository(TSSGRepository):
	def __init__(self, uri, username = None, password = None, syncfile = "/tmp/tssgreposync", *args, **kw):
		self.__syncfile = syncfile
		
		if not os.path.exists(syncfile):
			open(syncfile, "w").close()
		
		self.__synctime = self.__stat()
		
		super(SynchronizedTSSGRepository, self).__init__(uri = uri, username = username, password = password, lock = RWLock(syncfile), *args, **kw)
		
	def _do_refresh(self):
		super(SynchronizedTSSGRepository, self)._do_refresh()
		self.__synctime = self.__stat()
		
	def _do_delete_entity(self, entity):
		super(SynchronizedTSSGRepository, self)._do_delete_entity(entity)
		self._mark_dirty()
		
	def _do_persist(self, persisting):
		super(SynchronizedTSSGRepository, self)._do_persist(persisting)
		self._mark_dirty()
		
	def _mark_dirty(self):
		open(self.__syncfile, "w").close()
		self.__synctime = self.__stat()
		
	def __stat(self):
		return stat(self.__syncfile).st_mtime
			
	def __check_sync(self):
		if self.__stat() > self.__synctime:
			self.logger.debug("cache needs updating")
			self.refresh()
			
	def get_entity(self, klass, id):
		self.__check_sync()
		return super(SynchronizedTSSGRepository, self).get_entity(klass, id)
		
	def list_entities(self, klass, order_by = None, owns = {}, **filter):
		self.__check_sync()
		return super(SynchronizedTSSGRepository, self).list_entities(klass = klass, order_by = order_by, owns = owns, **filter)
		