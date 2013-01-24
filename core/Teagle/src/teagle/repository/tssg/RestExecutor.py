'''
Created on 03.09.2011

@author: kca
'''
from ngniutils.net.httplib.RestClient import RestClient
from teagle.repository.Entity import Entity

class RestExecutor(RestClient):	
	def list(self, klass):
		collection = self.__get_collection(klass)
		#logger.debug("Listing: %s" % (collection,))
		#return ResponseWrapper(self.get(collection))
		return self.get(collection)
	
	def update(self, entity, xml):
		assert isinstance(entity, Entity) and entity.is_persistent
		target = self.__get_collection(entity) + "/" + str(entity.id)
		self.put(target, payload = xml)
		
	def add(self, entity, xml):
		assert isinstance(entity, Entity) and not entity.is_persistent
		target = self.__get_collection(entity) + "/"
		return self.post(target, xml)

	def put(self, path, payload, headers = None):	  
		super(RestExecutor, self).put(path, payload, headers).close()
		
	def delete(self, entity):
		assert isinstance(entity, Entity) and entity.is_persistent
		target = self.__get_collection(entity) + "/" + str(entity.id)
		super(RestExecutor, self).delete(target)
	
	def __get_collection(self, e):
		if isinstance(e, Entity):
			e = e.__class__
		name = e.__name__
				
		return name[0].lower() + name[1:]
