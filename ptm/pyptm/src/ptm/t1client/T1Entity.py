#! /usr/bin/env python

from GlobalIdentifier import GlobalIdentifier
from ngniutils.logging import LoggerMixin 
#from HtmlFrontend import HtmlFrontend

class T1Entity(LoggerMixin):
	def __init__(self, identifier, t1client, config = None):
		self.logger.debug("Creating T1Entity: %s %s" % (identifier, config))
		#raise Exception("huhu")
		self.identifier = GlobalIdentifier(identifier)
		self.__config = config
		self.t1client = t1client

	@property
	def config(self):
		if self.__config is None:
			self.__config = self.t1client.get_entity(self.identifier).config
		return self.__config
		
	@property
	def parent_id(self):
		return self.identifier.parent
	
	@property
	def parent_entity(self):
		p_id = self.parent_id
		if p_id.is_root:
			return None
		return T1Entity(p_id, self.t1client, None)
	parent = parent_entity

	def update(self, config):
		self.__config = self.t1client.update(self.identifier, config).config

	def delete(self):
		self.t1client.delete(self.identifier)

	def __unicode__(self):
		return unicode(self.identifier)
	
	def __str__(self):
		return str(self.identifier)
