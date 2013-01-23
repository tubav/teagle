#! /usr/bin/env python
import logging
from Identifier import Identifier
from Resource import Resource
from Shadow import HubShadowManager
from util import check_owner
from exc import InternalIllegalArgumentError

logger = logging.getLogger("ptm")

class PTMClient(object):
	def __init__(self, registry_url = None, *args, **kw):
		super(PTMClient, self).__init__(*args, **kw)
		if registry_url is None:
			registry_url = "http://ptm:8000"
		self.__shadow_manager = HubShadowManager(client = self, hub_url = registry_url)

	def _mangle_parent(self, parent, typename):
#		logger.debug(parent)
		if parent is None:
			if not typename:
				return (None, None)
			return (Identifier(None).manager_for(unicode(typename)), unicode(typename))
		parent = Identifier(parent, need_abs = True)
		
		if not parent.is_adapter:
			#return (parent, parent.typename)
			return (parent, typename)
		
		#parent = parent.submanager
		
		if not typename:
			typename = parent.typename
		elif typename is True:
			typename = None
		
		if not parent or parent.is_root:
			parent = None
		else:
			parent = parent[:-1]
		
		#logger.debug("mangled2: %s %s" % (parent, typename))
		return (parent, typename)
		"""
		if typename is not True:
			typename = typename or parent.typename
			if not typename:
				raise ValueError("No type name given")
			parent =  parent.manager_for(unicode(typename))
		return (parent, parent.typename)
		"""

	def _resolve_adapter(self, _identifier):
		return self._get_hub()
	
	def _get_hub(self):
		return self.__shadow_manager.get_hub()
	get_hub = _get_hub
	
	def get_resource(self, identifier):
		if isinstance(identifier, Resource):
			return identifier
		identifier = Identifier(identifier, need_full = True)
		return self._resolve_adapter(identifier).get_resource(unicode(identifier))

	def add_resource(self, parent, name, typename, config, owner = None):
	#	parent, typename = self._mangle_parent(parent, typename)
		if parent == "/":
			parent = None
		else:
			parent = parent and Identifier(parent, need_full = True) or None
		if not typename:
			raise ValueError("No typename given")
		typename = unicode(typename)
		name = name or None
		owner = owner and unicode(owner) or None
		return self._resolve_adapter(Identifier(parent) / typename).add_resource(parent, name, typename, config, owner)

	def aquire_resource(self, identifier, owner, weak = False):
		identifier = Identifier(identifier)
		owner = owner and unicode(check_owner(owner, weak)) or None
		return self._get_hub().aquire_resource(identifier, owner, bool(weak))

	def list_resources(self, parent, typename = None):
		parent, typename = self._mangle_parent(parent, typename or True)
#		logger.debug("Listing: " + parent)
		return self._get_hub().list_resources(parent, typename)

	def register(self, identifier, url):
		logger.debug("remote register %s -> %s" % (identifier, url))
		return self._get_hub().register(identifier, url)
	
	def unregister(self, identifier):
		return self._get_hub().unregister(identifier)
	
	def signal(self, condition, identifier):
		return self._get_hub().signal(condition, identifier)
	
	def subscribe(self, condition, owner, reference):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = Identifier(owner, need_abs = True)
		reference = Identifier(reference, need_full = True)	
		self._get_hub().subscribe(condition, owner, reference)
		
	def unsubscribe(self, condition, owner, reference):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = Identifier(owner, need_abs = True)
		reference = Identifier(reference, need_full = True)	
		self._get_hub().unsubscribe(condition, owner, reference)
		
	def get_owners(self, identifier):
		identifier = Identifier(identifier, need_full = True)
		return self._get_hub().get_owners(identifier)

class OwningClient(object):
	def __init__(self, client, owner, *args, **kw):
		super(OwningClient, self).__init__(*args, **kw)
		self.__client = client
		self.__owner = owner

	def __getattr__(self, k):
		if k in ("add_resource", "aquire_resource"):
			return super(OwningClient, self).__getattr__(k)
		return self.__client.__getattr__(k)

	def add_resource(self, parent, name, typename, config, owner):
		parent, typename = self._mangle_parent(parent, typename)
		owner = unicode(Identifier(owner and owner or self.__owner))
		return self._resolve_adapter(parent).add_resource(parent, name, typename, config, owner)

	def aquire_resource(self, identifier, owner, weak = False):
		identifier = Identifier(identifier)
		owner = unicode(Identifier(owner and owner or self.__owner))
		return self._resolve_adapter(identifier).aquire_resource(identifier, owner, bool(weak))
	
class TestClient(PTMClient):
		def __init__(self):
			PTMClient.__init__(self, "http://localhost:8000/")

