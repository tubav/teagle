'''
Created on 11.07.2010

@author: kca
'''

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm.Manager import MethodDispatcher, BaseManager
from ptm.exc import InternalIllegalArgumentError
from ptm.Identifier import Identifier
from ptm.Shadow import ShadowManager
from threading import Lock
from ptm.notifications import NotificationId, DELETED
from NotificationManager import NotificationManager
import logging

logger = logging.getLogger("ptm")

class HubMethodDispatcher(MethodDispatcher):
	def acquire_resource(self, identifier, owner):
		identifier = self._marshaller.unpack_identifier(identifier)
		owner = self._marshaller.unpack_owner(owner)
		self._manager.do_acquire_resource(identifier, owner)

	def release_resource(self, identifier, owner):
		identifier = self._marshaller.unpack_identifier(identifier)
		owner = self._marshaller.unpack_owner(owner)
		self._manager.do_release_resource(identifier, owner)

	def get_owners(self, identifier):
		identifier = self._marshaller.unpack_identifier(identifier)
		owners = self._manager.do_get_owners(identifier)
		return owners.as_tuple()

	def notify(self, condition, owner, reference):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		reference = self._marshaller.unpack_identifier(reference)
		owner = Identifier(owner, need_abs = True)
		self._manager.do_notify(condition, owner, reference)

	def signal(self, condition, reference):
		reference = self._marshaller.unpack_identifier(reference)
		return self._manager.do_signal(condition, reference)
	
	def register(self, identifier, url):
		if not identifier:
			raise InternalIllegalArgumentError(identifier)
		identifier = Identifier(identifier, need_abs = True)
		self._manager.do_register(identifier, url)
		
	def unregister(self, identifier):
		if not identifier:
			raise InternalIllegalArgumentError(identifier)
		identifier = Identifier(identifier, need_abs = True)
		self._manager.do_unregister(identifier)
		
	def subscribe(self, condition, owner, identifier):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = Identifier(owner, need_abs = True)
		identifier = Identifier(identifier, need_full = True)
		self._manager.do_subscribe(condition, owner, identifier)
		
	def unsubscribe(self, condition, owner, identifier):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		owner = Identifier(owner, need_abs = True)
		identifier = Identifier(identifier, need_full = True)
		self._manager.do_unsubscribe(condition, owner, identifier)
	
class Subscriptions(object):
	def __init__(self, *args, **kw):
		super(Subscriptions, self).__init__(*args, **kw)
		self.__subscriptions = {}
		
	def __nonzero__(self):
		return bool(self.__subscriptions)
	__bool__ = __nonzero__
		
	def _get_subscriptions(self, condition):
		try:
			return self.__subscriptions[condition]
		except KeyError:
			s = set()
			self.__subscriptions[condition] = s
			return s
		
	def add_subscription(self, condition, owner):
		for c in condition:
			self._get_subscriptions(c).add(owner)
		
	def remove_subscription(self, condition, owner):
		try:
			for c in condition:
				s = self.__subscriptions[c]
				s.discard(owner)
				if not s:
					self.__subscriptions.pop(condition, None)
		except KeyError:
			pass
		
	def get_subscribers(self, condition):
		try:
			return self.__subscriptions[condition]
		except KeyError:
			return ()
		
	def remove_subscriber(self, owner):
		for condition in self.__subscriptions.keys():
			self.remove_subscription(condition, owner)
		
class SubscriptionManager(object):
	def __init__(self, *args, **kw):
		super(SubscriptionManager, self).__init__(*args, **kw)
		self.__subscriptions = {}
		self.__lock = Lock()
			
	def add_subscription(self, condition, owner, reference):
		with self.__lock:
			try:
				s = self.__subscriptions[reference]
			except KeyError:
				s = Subscriptions()
				self.__subscriptions[reference] = s
			s.add_subscription(condition, owner)
		
	def remove_subscription(self, condition, owner, reference):
		with self.__lock:
			try:
				s = self.__subscriptions[reference]
				s.remove_subscription(condition, owner)
				if not s:
					self.__subscriptions.pop(reference, None)
			except KeyError:
				pass
	
	def remove_subscriper(self, owner):
		with self.__lock:
			for reference, subscriptions in self.__subscriptions.items():
				subscriptions.remove_subscriper(owner)
				if not subscriptions:
					self.__subscriptions.pop(reference, None)
					
	def remove_subscriptions(self, reference):
		with self.__lock:
			self.__subscriptions.pop(reference, None)
			
	def get_subscribers(self, condition, reference):
		with self.__lock:
			try:
				return self.__subscriptions[reference].get_subscribers(condition)
			except KeyError:
				return ()
"""				 
class HubRegistry(Registry):
	def __init__(self, client, *args, **kw):
		super(HubRegistry, self).__init__(*args, **kw)
		self.__shadow_manager = ShadowManager(client = client)
		
	def _mangle_payload(self, payload):
		return self.__shadow_manager.get_shadow(payload)
	
	def get_shadow_manager(self):
		return self.__shadow_manager
	shadow_manager = property(get_shadow_manager)
"""

class PTMHub(BaseManager):
	def __init__(self, *args, **kw):
		super(PTMHub, self).__init__(*args, **kw)
		self.__shadow_manager = ShadowManager(client = self)
		self.__subscription_manager = SubscriptionManager()
		self.__notification_manager = NotificationManager(registry = self.registry)
		
	def get_method_dispatcher(self):
		return HubMethodDispatcher(self)
		
	def __check_resolvable(self, owner):
		self.get_adapter(owner)
		
	def __mangle_notify_args(self, condition, owner, reference):
		if not owner:
			raise InternalIllegalArgumentError(owner)
		return (NotificationId(condition), Identifier(owner, need_abs = True), Identifier(reference, need_abs = True))
		
	def do_subscribe(self, condition, owner, reference):
		self.__check_resolvable(owner)
		self.__subscription_manager.add_subscription(*self.__mangle_notify_args(condition, owner, reference))
		
	def do_unsubscribe(self, condition, owner, reference):
		self.__subscription_manager.remove_subscription(*self.__mangle_notify_args(condition, owner, reference))
		
	def do_notify(self, condition, owner, reference):
		(condition, owner, reference) = self.__mangle_notify_args(condition, owner, reference)
		self.__do_notify(condition, owner, reference)
		
	def __do_notify(self, condition, owner, reference):
		if DELETED in condition:
			condition = DELETED
		self.__notification_manager.notify(condition, owner, reference)
		
	def do_signal(self, condition, reference):
		(condition, _, reference) = self.__mangle_notify_args(condition, "/", reference)
		subscribers = self.__subscription_manager.get_subscribers(condition, reference)
		for s in subscribers:
			try:
				self.do_notify(condition, s, reference)
			except:
				logger.exception("notify failed")
	
	def do_register(self, identifier, payload):
		payload = self.__shadow_manager.get_shadow(payload)
		super(PTMHub, self).do_register(identifier, payload)
		
	def register_adapter(self, identifier, adapter):
		if not isinstance(adapter, AbstractResourceAdapter):
			raise TypeError("Need an instance of ResourceAdapter here. Got: " + str(adapter))
		super(PTMHub, self).do_register(identifier, adapter)
	