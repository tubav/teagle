'''
Created on 01.04.2011

@author: kca
'''

from teagle.repository.exc import NoEntityFound
from exc import UnavailableDueToPreviousError
from ngniutils.threading import RWLock
from threading import Thread, current_thread
from time import time
from ngniutils.logging import LoggerMixin

class Refresher(LoggerMixin, Thread):	
	def run(self):
		self.error = None
		try:
			super(Refresher, self).run()
		except Exception, e:
			self.logger.exception("Error in fetcher %s" % current_thread().name)
			self.error = e

class CacheEntry(RWLock, dict):
	pass

class Cache(object):
	def __init__(self, repo, klasses, timeout = 30.0, *args, **kw):
		super(Cache, self).__init__(*args, **kw)
		
		self.__cache = {}
		self.__repo = repo
		self.__have_error = False
		self.__timeout = timeout
		
		if klasses is None:
			from teagle.repository.entities import get_entity_classes
			klasses = get_entity_classes()
			
		for klass in klasses:
			self.__cache[klass] = CacheEntry()

	def list_entities(self, klass):
		entry = self.__get_cache_entry(klass)
		with entry.read_transaction(self.__timeout):		   
			return entry.values()
			
	def get_entity(self, klass, id):
		entry = self.__get_cache_entry(klass)
		
		try:
			with entry.read_transaction(self.__timeout):		   
				return entry[id]
		except KeyError:
			raise NoEntityFound("%s %s" % (klass, id))
		finally:
			if self.__have_error:
				raise UnavailableDueToPreviousError()
			
	def get_entity_unlocked(self, klass, id):
		entry = self.__get_cache_entry(klass)
		
		try:
			return entry[id]
		except KeyError:
			raise NoEntityFound("%s %s" % (klass, id))
		finally:
			if self.__have_error:
				raise UnavailableDueToPreviousError()
			
	def put_entity(self, entity):
		klass = entity.__class__
		entry = self.__get_cache_entry(klass)
		with entry.write_transaction(self.__timeout):
			entry[entity.id] = entity
		
	def refresh(self):
		threads = []
		start = time()
		self.logger.debug("refreshing cache")
		self.__block()

		for klass, entry in self.__cache.iteritems():
			thread = Refresher(target = self.__refresh_entry, args = (klass, entry), name = "fetcher-for-" + klass.__name__)
			threads.append(thread)
			#self.logger.debug("Starting thread: %s" % (thread.name, ))
			thread.start()
			
		map(Thread.join, threads)
		count = map(len, self.__cache.itervalues())
		count = reduce(int.__add__, count, 0)
		self.logger.info("full refresh finished in %fs (%d entries)" % (time() - start, count))
		
		if self.__have_error:
			raise self.__have_error
			
		self.__have_error = False
		
	def __refresh_entry(self, klass, entry):
		try:
			start = time()			 
			have_error = False
			values = {}
			
			self.logger.info("Refreshing %s" % (klass.__name__))
			
			values = self.__repo._list_data(klass)

			vals = set()
			for id, fields in values:
				try:
					e = entry[id]
				except KeyError:
					entry[id] = self.__make_entity(klass, id, fields)
				else:
					e._set_fields(fields)
				vals.add(id)
			
			for id in entry.keys():
				if id not in vals:
					del entry[id]
		except Exception, e:
			have_error = e
			if not self.__have_error:
				self.__have_error = e
			raise
		finally:	
			entry.write_release()	
			self.logger.info("Refreshing %s finished %s in %fs (%d entries)" % (klass.__name__, have_error and "with error (%s)" % (repr(have_error), ) or "successful", time() - start, len(entry)))
		
	def __make_entity(self, klass, id, fields):
		e = klass(id = id, fields = fields, repo = self.__repo)
		return e
		
	def __block(self):
		for entry in self.__cache.itervalues():
			entry.write_acquire(self.__timeout)

	def __get_cache_entry(self, klass):
		try:
			entry = self.__cache[klass]
		except KeyError:
			self.logger.warning("Unhandled type: %s. Trying my best (Congrats, you hit an untested code path)." % (klass, ))
			entry = CacheEntry()
			self.__cache[klass] = entry
			with entry.write_transaction(self.__timeout):
				self.__refresh_entry(klass, entry)
			#raise InternalError("Unhandled type: %s" % (klass, ))
		return entry

	def get_logger(self):
		return self.__repo.logger
	logger = property(get_logger)
