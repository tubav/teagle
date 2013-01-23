#! /usr/bin/env python

from Resource import Resource, ResourceProxy
from Identifier import Identifier
from helpers import is_iterable, is_primitive
from exc import InternalTypeError, PTMException

#TODO: cache
class Marshaller(object):
	def __init__(self, client, *args, **kw):
		super(Marshaller, self).__init__(*args, **kw)
		self.__client = client

	def pack_dict(self, d):
		for k, v in d.iteritems():
			d[k] = self.pack_value(v)
		return d

	def pack_list(self, l):
		return [ self.pack_value(v) for v in l ]

	def pack_value(self, v):
		if isinstance(v, basestring):
			return u"s" + unicode(v)
		if isinstance(v, dict):
			return self.pack_dict(v)
		if is_iterable(v):
			return self.pack_list(v)
		if isinstance(v, Resource):
			return u"r" + unicode(v.identifier)
		if isinstance(v, Identifier):
			return u"i" + unicode(v)
		if not is_primitive(v):	
			raise InternalTypeError(v)
		return v

	def unpack_dict(self, d):
		for k, v in d.iteritems():
			d[k] = self.unpack_value(v)
		return d

	def unpack_value(self, v):
		if isinstance(v, basestring):
			return self.unpack_string(v)
		if isinstance(v, dict):
			return self.unpack_dict(v)
		if is_iterable(v):
			return self.unpack_list(v)
		if not is_primitive(v):	
			raise InternalTypeError(v)
		return v

	def unpack_list(self, l):
		return tuple([ self.unpack_value(v) for v in l ])

	def unpack_string(self, s):
		t = s[0]
		if t == "s":
			return s[1:]
		if t == "r":
			return self.make_proxy(s[1:])
		if t == "i":
			return Identifier(s[1:])
		raise PTMException("Illegal string prefix: " + t)

	def unpack_owner(self, owner):
		if not owner:
			return None
		return self.unpack_value(owner)

	def make_proxy(self, identifier):
		if isinstance(identifier, Resource):
			return identifier
		identifier = Identifier(identifier, need_full = True)
		return ResourceProxy(identifier = identifier, adapter = self.__client)

	def pack_identifier(self, identifier):
		return unicode(Identifier(identifier, need_full = True))

	def unpack_identifier(self, identifier):
		return Identifier(identifier, need_full = True)
