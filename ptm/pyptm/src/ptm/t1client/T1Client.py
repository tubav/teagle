#! /usr/bin/env python

from xml.dom.minidom import parse

from GlobalIdentifier import GlobalIdentifier 
from ptm.exc import NoAdapterFoundError
from T1EntitySerializer import T1EntitySerializer
from T1Entity import T1Entity
import StringIO 
from ngniutils.net.httplib.RestClient import RestClient, HTTPError

import logging
logger = logging.getLogger("ptm")


ILLEGAL_CHARS = "<>"

class T1Client(object):
	def __init__(self, url, prefix = None, serializer = None, *args, **kw):
		super(T1Client, self).__init__(*args, **kw)
		self.__t1 = RestClient(url, cache = False)
		if not serializer:
			serializer = T1EntitySerializer(self, prefix = prefix)
		self.serializer = serializer
		self.__prefix = prefix
		
	@property
	def prefix(self):
		return self.__prefix

	def add(self, parent, name, typename, config):
		parent = GlobalIdentifier(parent, prefix = self.prefix).identifier
		wfile = StringIO.StringIO()
		
		self.serializer.assist_serialize(typename, None, config, wfile)
		xml = wfile.getvalue()
		print xml
		wfile.close()
		#self.conn.request("POST", parent, params, headers)
		with self.__t1.post(parent, xml) as resp:
			_tn, cfg, _act = self.serializer.unserialize(resp)
		return T1Entity(cfg.pop("identifier"), self, cfg)
	add_resource = add

	def update(self, identifier, config):
		identifier = GlobalIdentifier(identifier, prefix = self.prefix)
		#wfile = StringIO.StringIO()
		req = self.serializer.assist_serialize(identifier.typename, None, config, None, "update")
		with self.__t1.put(identifier, req) as resp:
			_tn, cfg, _act = self.serializer.unserialize(resp)
		return T1Entity(identifier, self, cfg)
	update_resource = update
		
	def get_entity(self, identifier):
		#raise Exception("hjhu", identifier, self.prefix)
		#raise Exception(identifier, type(identifier))
		identifier = unicode(GlobalIdentifier(identifier, prefix = self.prefix))
		#raise Exception("hjhu", identifier, self.prefix)
		if identifier.endswith("/"):
			identifier = identifier[:-1]
		with self.__t1.get(identifier) as cfg:
			_tn, cfg, _act = self.serializer.unserialize(cfg)
		#raise Exception("huu", identifier, cfg)
		identifier = GlobalIdentifier(cfg.get("identifier", identifier), default_prefix = self.prefix)
		return T1Entity(identifier, self, cfg)
	get_resource = get_entity

	def list_entities(self, identifier):
		identifier = GlobalIdentifier(identifier, prefix = self.prefix)
		try:
			response = self.__t1.get(unicode(identifier))
		except HTTPError, e:
			if e.reason.startswith("No adapters found for") or e.reason.startswith("NoAdapterFoundError"):
				raise NoAdapterFoundError(identifier)
			raise
		with response:
			xml = parse(response)
			
		try:
			l = []
			child = xml.firstChild.childNodes
	
			for node in child:
				if node.nodeName != "#text":
					identifier = node.firstChild.data
					identifier = GlobalIdentifier(identifier, prefix = self.prefix)
					l.append(T1Entity(identifier, self, None))
			return l
		finally:
			xml.unlink()
	list_resource = list_entities
	

	def delete(self, identifier):
		#self.conn.request("DELETE", unicode(identifier))
		self.__t1.delete(identifier)
	delete_resource = delete
