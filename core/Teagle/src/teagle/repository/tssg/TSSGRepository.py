'''
Created on 01.04.2011

@author: kca
'''
from teagle.repository import Entity, TeagleRepository, Password
import threading
import TSSGSerializer
from Cache import Cache
from hashlib import md5
from teagle.repository.exc import MultipleEntitiesFound, NoEntityFound, AuthenticationError
from teagle.repository.entities import Person, PersonRole, VctState, ResourceInstanceState, get_entity_class
from ngniutils.logging import LoggerMixin
from teagle.repository.tssg.RestExecutor import RestExecutor

class LazyRepoEntity(object):
	def __init__(self, klass, id, *args, **kw):
		self.__klass = klass
		self.__id = id
		
	def __get__(self, repo, klass):
		return repo.get_entity(self.__klass, self.__id)

class TSSGRepository(LoggerMixin, TeagleRepository):	
	ROLE_CUSTOMER = LazyRepoEntity(PersonRole, 1)
	ROLE_ADMIN = LazyRepoEntity(PersonRole, 2)
	ROLE_PARTNER = LazyRepoEntity(PersonRole, 3)
	
	VCT_STATE_BOOKED = LazyRepoEntity(VctState, 10)
	
	RESOURCE_INSTANCE_STATE_PROVISIONED = LazyRepoEntity(ResourceInstanceState, 3)
	RESOURCE_INSTANCE_STATE_UNPROVISIONED = LazyRepoEntity(ResourceInstanceState, 8)
	
	def __init__(self, uri, username = None, password = None, lock = None, classes = None, timeout = None, *args, **kw):
		super(TSSGRepository, self).__init__(*args, **kw)
		
		#raise Exception(uri)
		
		if lock is None:
			from ngniutils.threading import RWLock
			lock = RWLock() 
			
		self.timeout = timeout
		self.__lock = lock
		self.__serializer = TSSGSerializer.TSSGSerializer(self)
		self.__executor = RestExecutor(uri = uri, username = username, password = password, content_type = "text/xml")
		self.__cache = Cache(self, classes, timeout = timeout)
		self.__refresh_done = threading.Condition()
		self.__refreshing = False
		
		if classes:
			import teagle.repository.entities
			teagle.repository.entities._classes = classes
		
		self.refresh()
		
	def refresh(self):
		with self.__lock.read_transaction(self.timeout):
			self.__refresh_done.acquire()
			if not self.__refreshing:
				self.__refreshing = True
				self.__refresh_done.release()
				try:
					self._do_refresh()
				finally:
					self.__refreshing = False
					with self.__refresh_done:
						self.__refresh_done.notify_all()
			else:
				self.__refresh_done.wait()
				assert(not self.__refreshing)
				self.__refresh_done.release()
				
	def _do_refresh(self):
		self.__cache.refresh()
				
	def list_entities(self, klass, order_by = None, owns = {}, order_desc = False, **filtr):
		if isinstance(klass, basestring):
			klass = get_entity_class(klass)
		entities = self.__cache.list_entities(klass)
		
		if order_by is None and entities and hasattr(entities[0], "commonName"):
			order_by = "commonName"
		
		for k, v in filtr.iteritems():
			#entities = [ e for e in entities if getattr(e, k) == v ]
			entities = filter(lambda e: getattr(e, k) == v, entities)
			
		for k, v in owns.iteritems():
			#entities = [ e for e in entities if v in getattr(e, k) ]
			entities = filter(lambda e: v in getattr(e, k), entities)
		
		if order_by and order_by[0] != "_":
			try:
				entities.sort(key = lambda x: getattr(x, order_by))
			except AttributeError:
				self.logger.exception("Error sorting result")
			else:
				if order_desc:
					entities.reverse()
		
		return tuple(entities)
	
	def get_unique_entity(self, klass, owns = {}, **filter):
		entities = self.list_entities(klass, order_by = False, owns = owns, **filter)
		
		if not entities:
			self.logger.debug("No unique entity found for %s (owns=%s, filter=%s). retrying after refresh." % (klass.__name__, owns, filter))
			self.refresh()
			entities = self.list_entities(klass, order_by = False, owns = owns, **filter)
			if not entities:
				raise NoEntityFound(klass, filter)
		
		if len(entities) > 1:
			raise MultipleEntitiesFound("Multiple entities found for %s owning %s with filter %s" % (klass, owns, filter))
		
		return entities[0]

	def _get_entity(self, klass, id):
		#print ("get2", klass, id)	

		return self.__cache.get_entity(klass, id)
	
	def _get_entity_unlocked(self, klass, id):
		#print ("get2", klass, id)	

		return self.__cache.get_entity_unlocked(klass, id)

	def get_entity(self, klass, id):
		if isinstance(klass, basestring):
			klass = get_entity_class(klass)
		id = int(id)
		try:
			return self._get_entity(klass, id)
		except NoEntityFound:
			self.logger.debug("No entity found for %s-%s. retrying after refresh." % (klass.__name__, id))
			self.refresh()
			return self._get_entity(klass, id)

	def _do_persist(self, persisting):
		for e in persisting:
			self.__do_persist(e)

	def persist(self, entity):
		self.logger.debug("persist: %s" % (entity, ))
		persisting = []
		considered = set()
		
		with self.__lock.write_transaction(self.timeout):
			self.__persist(entity, persisting, considered)
			
			#classes = set( p.__class__ for p in persisting )
			
			persisting.reverse()
			
			self.logger.debug("need persisting: %s" % (persisting, ))
						
			self._do_persist(persisting)
		
		if persisting:			
			self.refresh()
			
	def delete_entity(self, entity):
		self._do_delete_entity(entity)
		self.refresh()
		
	def _do_delete_entity(self, entity):
		self.__executor.delete(entity)

	def __persist(self, entity, persisting, considered):
		if isinstance(entity, (list, set, tuple, frozenset)):
			for e in entity:
				assert isinstance(e, Entity), "Strange value in collection: %s" % (e, )
				self.__persist_entity(e, persisting, considered)
		else:
			self.__persist_entity(entity, persisting, considered)
	
	def __persist_entity(self, entity, persisting, considered):
		assert(isinstance(entity, Entity))
		
		#self.logger.debug("Considering entity: %s" % (entity, ))
		if entity not in considered:
			considered.add(entity)
			
			if entity.is_updated:
				persisting.append(entity)
			
			for v in entity._get_fields().itervalues():
				#self.logger.debug("Considering fieldvalue: %s" % (v, ))
				if isinstance(v, (Entity, list, set, tuple, frozenset)):
					if isinstance(v, Entity) and not v.is_persistent:
						assert(not isinstance(v, Person))
						entity.set_is_updated(True)
						if entity not in persisting:
							persisting.append(entity)
					self.__persist(v, persisting, considered)
				
	def __do_persist(self, e):
		self.logger.debug("executing persist for %s" % (e, ))
		xml = self.__serializer.serialize(e)
		#logger.debug("xml: %s" % (xml, ))
		if e.is_persistent:
			self.logger.debug("Updating: %s" % (e, ))
			self.__executor.update(e, xml)
		else:
			self.logger.debug("Adding: %s" % (e, ))
			xml = self.__executor.add(e, xml)
			with xml:
				id, values = self.__serializer.unserialize_entity(xml, e.__class__)
			self.logger.debug("Received after add: %s %s" % (id, values))
			e._set_id(id)
			e._set_fields(values)
			e._set_repository(self)
			e.set_is_updated(False)
			self.__cache.put_entity(e)
	
	def _list_data(self, klass):
		with self.__executor.list(klass) as xml:
			for x in self.__serializer.unserialize_values(xml, klass):
				yield x
		
	def make_password(self, password):
		return Password(md5(password).hexdigest())
	
	def check_password(self, password, target):
		if hasattr(target, "password"):
			target = target.password
		return self.make_password(password) == target
		
	def authenticate_user(self, username, password):
		user = self.get_unique_entity(Person, commonName = username)
		
		if not self.check_password(password, user):
			raise AuthenticationError(username)
		
		return user
		
