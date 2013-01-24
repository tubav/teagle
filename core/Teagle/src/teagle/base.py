'''
Created on 28.08.2011

@author: kca
'''
from exc import IdentifierError
from abc import ABCMeta, abstractmethod, abstractproperty

class Identifier(object):
	PREFIX_SEPARATOR = "."
	PREFIX_ILLEGAL = "/"
	PREFIX_PREFIX = ""
	
	def __init__(self, identifier, default_prefix = None, *args, **kw):
		super(Identifier, self).__init__(*args, **kw)
		try:
			self.__identifier = identifier.__identifier
		except AttributeError:
			try:
				identifier = identifier.identifier
			except AttributeError:
				pass
			
			if identifier:
				identifier = unicode(identifier)
				prefix, sep, id = identifier.partition(self.PREFIX_SEPARATOR)
				if not sep and not id:
					id = prefix
					prefix = default_prefix
				elif self.PREFIX_ILLEGAL in prefix:
					id = identifier
					prefix = default_prefix
				elif not prefix:
					prefix = default_prefix
			else:
				id = ""
				prefix = default_prefix
				
			if not prefix:
				raise IdentifierError("Illegal id: %s (default_prefix=%s)" % (identifier, prefix))
		
			self.__identifier = prefix + self.PREFIX_SEPARATOR + id
			
	def __unicode__(self):
		return self.__identifier
	
	def __str__(self):
		return self.__identifier.encode("utf-8")
	
	def __repr__(self):
		return "%s('%s')" % (self.__class__.__name__, str(self))
	
	@property
	def identifier(self):
		return self.__identifier
	
	@property
	def local_identifier(self):
		return self.tuple[1]
		
	@property
	def prefix(self):
		return self.tuple[0]
		
	@property
	def tuple(self):
		return self.__identifier.split(self.PREFIX_SEPARATOR, 1)
	
	@property
	def typename(self):
		lid = self.local_identifier
		if lid.startswith("/"):
			lid = lid.rpartition("/")[-1]
		return lid.split("-", 1)[0]
	
	def __eq__(self, o):
		try:
			return self is o or self.identifier == o.identifier
		except AttributeError:
			return super(Identifier, self) == o
		
	def __hash__(self):
		return hash(self.__identifier)
	
class Resource(object):
	__metaclass__ = ABCMeta

	@abstractproperty
	def identifier(self):
		raise NotImplementedError()

	@abstractproperty
	def config(self):
		raise NotImplementedError()
	
	def get_configuration(self):
		#TODO: deprecated warnng
		return self.config
	
	@abstractmethod
	def update(self, config):
		raise NotImplementedError()

	@abstractmethod
	def delete(self):
		raise NotImplementedError()
	
	@property
	def typename(self):
		return self.identifier.typename

	def __unicode__(self):
		return unicode(self.identifier)
	
	def __str__(self):
		return str(self.identifier)
	
class Testbed(object):
	__metaclass__ = ABCMeta
	
	@abstractproperty
	def name(self):
		raise NotImplementedError()
		
	@abstractproperty
	def instances(self):
		raise NotImplementedError()
	
	@abstractproperty
	def state(self):
		raise NotImplementedError()
