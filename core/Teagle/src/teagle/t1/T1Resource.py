#! /usr/bin/env python

from ..base import Identifier, Resource
from ngniutils.logging import LoggerMixin 
#from HtmlFrontend import HtmlFrontend

class T1Resource(Resource, LoggerMixin):
	def __init__(self, identifier, t1client, config = None, *args, **kw):
		super(T1Resource, self).__init__(*args, **kw)
		self.logger.debug("Creating T1Entity: %s %s" % (identifier, config))
		#raise Exception("huhu")
		self.__identifier = Identifier(identifier)
		self.__config = config
		self.t1client = t1client
		
	def get_identifier(self):
		return self.__identifier
	def set_identifier(self, identifier):
		self.__identifier = identifier
	identifier = property(get_identifier, set_identifier)

	@property
	def config(self):
		if self.__config is None:
			self.__config = self.t1client.get_entity(self.identifier).config
		return self.__config
		
	def update(self, config):
		self.__config = self.t1client.update(self.identifier, config).config

	def delete(self):
		self.t1client.delete(self.identifier)
		
	def __hash__(self):
		return hash(self.__identifier)
	
	def __eq__(self, o):
		try:
			return self is o or self.__identifier == o.__identifier
		except AttributeError:
			return super(T1Resource, self) == o
		
	def __ne__(self, o):
		return not (self == o)
	
	def __repr__(self):
		return "<%s for %s (@%s)>" % (self.__class__.__name__, self.identifier, hex(id(self)))
		