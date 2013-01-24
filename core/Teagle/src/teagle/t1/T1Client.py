#! /usr/bin/env python

from xml.dom.minidom import parse

from ..base import Identifier
from T1Resource import T1Resource  
from ngniutils.net.httplib.RestClient import RestClient, HTTPError,\
	HTTPErrorResponse
from ngniutils.logging import LoggerMixin
from exc import T1IdentifierError
from . import T1Interface
from teagle.t1.serializer import ResultSerializer
from ngniutils.serializer.exc import ParseError
from teagle.t1.exc import T1ErrorResponse
from ngniutils.logging.logbook import Logbook

ILLEGAL_CHARS = "<>"
	
class BasicT1Client(LoggerMixin, T1Interface):
	def __init__(self, url, prefix, serializer, *args, **kw):
		super(BasicT1Client, self).__init__(*args, **kw)
		self.serializer = serializer
		self.prefix = prefix
	
	def _convert_identifier(self, identifier, default_prefix = None):
		default_prefix = default_prefix or self.prefix
		return Identifier(identifier, default_prefix = default_prefix)
	
	def _enforce_identfier(self, identifier):
		if not identifier.local_identifier:
			raise T1IdentifierError("No full identifier given for get_resource: %s" % (identifier, ))
		
	def _make_entity(self, identifier, typename, config):
		return T1Resource(identifier, self, config)
	
	def add_resource(self, parent, typename, config):
		parent = self._convert_identifier(parent)
		xml = self.serializer.assist_serialize(typename, None, config)
		return self._add_resource(parent, xml)
	
	def update_resource(self, identifier, config):
		identifier = self._convert_identifier(identifier)
		self._enforce_identfier(identifier)
		xml = self.serializer.assist_serialize(identifier.typename, None, config, None, "update")
		return self._update_resource(identifier, xml)
	update = update_resource

	def get_resource(self, identifier):
		identifier = self._convert_identifier(identifier)
		self._enforce_identfier(identifier)
		return self._get_resource(identifier)
	get_entity = get_resource
	
	def delete_resource(self, identifier):
		identifier = self._convert_identifier(identifier)
		return self._delete_resource(identifier)
	delete = delete_resource
	
	def execute_method(self, identifier, name, kwargs=None):
		identifier = self._convert_identifier(identifier)
		self._enforce_identfier(identifier)
		xml = self.serializer.assist_serialize(identifier.typename, None, kwargs, None, "methodcall")
		return self._exec_method(identifier, name, xml)
	execute = execute_method
	
def _t1method(f):
	def _f(self, *args, **kw):
		fname = f.__name__
		if fname[0] == "_":
			fname = fname[1:]
		name = "%s(%s, %s) at %s" % (fname, args, kw, self.uri)
		try:
			return f(self, *args, **kw)
		except HTTPErrorResponse, e:
			try:
				with e:
					result = self.result_serializer.load(e)
			except ParseError:
				self.logger.exception("Error parsing result from answer")
				raise T1ErrorResponse(1, str(e), Logbook(name = name, component = self.__class__.__name__, entries = ()))
			raise T1ErrorResponse(result.status, self._format_error(name, result), result.log)
		except Exception, e:
			raise T1ErrorResponse(1, str(e), Logbook(name = name, component = self.__class__.__name__, entries = ()))
	_f.__name__ = f.__name__
	return _f

class T1Client(BasicT1Client):
	def __init__(self, url, prefix = None, serializer = None, certfile = None, keyfile = None, *args, **kw):
		if not serializer:
			from T1Serializer import T1Serializer
			serializer = T1Serializer(self)
		super(T1Client, self).__init__(url = url, prefix = prefix, serializer = serializer, *args, **kw)
		self.result_serializer = ResultSerializer()
		self.__t1 = RestClient(url, cache = False, content_type = "text/xml", certfile = certfile, keyfile = keyfile)
		self.logger.debug("Created T1 client: url=%s prefix=%s certfile=%s keyfile=%s", url, prefix, certfile, keyfile)
	
	@property
	def uri(self):
		return self.__t1.uri
	
	def _get_path(self, identifier):
		return unicode(identifier)

	@_t1method
	def _add_resource(self, parent, xml):
		with self.__t1.post(self._get_path(parent) + "/", xml) as resp:
			_tn, cfg, _act = self.serializer.unserialize(resp)
		return self._make_entity(cfg.pop("identifier"), _tn, cfg)

	@_t1method
	def _update_resource(self, identifier, xml):
		with self.__t1.put(self._get_path(identifier), xml) as resp:
			_tn, cfg, _act = self.serializer.unserialize(resp)
		return self._make_entity(identifier, _tn, cfg)
		
	@_t1method
	def _get_resource(self, identifier):
		with self.__t1.get(self._get_path(identifier)) as cfg:
			_tn, cfg, _act = self.serializer.unserialize(cfg)
		identifier = self._convert_identifier(cfg.get("identifier", identifier))
		return self._make_entity(identifier, _tn, cfg)

	@_t1method
	def list_resources(self, identifier, typename = None):
		identifier = self._convert_identifier(identifier)
		try:
			response = self.__t1.get(self._get_path(identifier))
		except HTTPError, e:
			if e.reason.startswith("No adapters found for") or e.reason.startswith("NoAdapterFound"):
				return []
			raise
		with response:
			xml = parse(response)
			
		try:
			l = []
			child = xml.firstChild.childNodes
	
			for node in child:
				if node.nodeName != "#text":
					identifier = node.firstChild.data
					identifier = Identifier(identifier, default_prefix = self.prefix)
					l.append(self._make_entity(identifier, None, None))
			return l
		finally:
			xml.unlink()	

	@_t1method
	def _delete_resource(self, identifier):
		self.__t1.delete(self._get_path(identifier)).close()
		
	@_t1method
	def _exec_method(self, identifier, name, xml):
		with self.__t1.put(self._get_path(identifier) + "#" + name, xml) as resp:
			_tn, cfg, _act = self.serializer.unserialize(resp)
		return  self._make_entity(identifier, _tn, cfg)
	
	
	def _format_error(self, opname, r):
		return r.message

	