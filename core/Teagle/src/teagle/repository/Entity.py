'''
Created on 01.04.2011

@author: kca
'''
import collections
from teagle.repository.exc import InternalError
from abc import ABCMeta
from ngniutils.logging import LoggerMixin

class Field(collections.namedtuple("FieldTuple", ("name", "type", "nullable", "default", "coerce_none", "inline_hint", "wrapper_for", "unique", "shared", "usereditable", "enum", "displayname", "backref", "description"))):
	#TODO: a lot. among others, properly ensure uniqueness in backends (currently only in forms)
	def __new__(cls, name, type, nullable = False, default = None, coerce_none = False, inline_hint = None, wrapper_for = None, unique = False, shared = False, usereditable = True, enum = None, displayname = None, backref = None, description = None):
		return super(Field, cls).__new__(cls, name = name, type = type, nullable = nullable, default = default, coerce_none = coerce_none, inline_hint = inline_hint, wrapper_for = wrapper_for, unique = unique, shared = shared, usereditable = usereditable, enum = enum, displayname = displayname, backref = backref, description = description)
	
	#only yield two values for compatibility with old behaviour
	def __iter__(self):
		yield self.name
		yield self.type
		
	def get_default_value(self):
		if self.type == Password:
			from os import urandom
			from hashlib import md5
			return Password(md5(urandom(16)).hexdigest())
		if isinstance(self.default, type):
			return self.default()
		return self.default
	
	@property
	def type_class(self):
		if not isinstance(self.type, type):
			return self.type.__class__
		return self.type
	
	@property
	def is_container(self):
		return isinstance(self.type, (tuple, list, set, frozenset))
	
	@property
	def display_inline(self):
		return self.inline_hint or not issubclass(self.type_class, (Entity, list, set, frozenset, tuple, Password))
	
	def convert(self, v, e):
		assert e is not None, "convert guard"
		
		if v is None:
			if self.nullable:
				return None
			if self.coerce_none:
				v = self.get_default_value()
			else:
				raise ValueError("Value for %s must not be None" % (self.name, ))
		
		if self.is_container:
			if not isinstance(self.type, list):
				raise NotImplementedError()
			v = List(entity = e, field = self, iterable = v)
			
		if not isinstance(v, self.type_class):
			if self.type_class is unicode and v.__class__ is str:
				v = unicode(v)
			else:
				raise TypeError("Wrong type for %s. Want %s, have %s (%s)." % (self.name, self.type_class, v.__class__, v))
			
		#todo: remove IP
		if self.enum and not (v is None and self.nullable) and v not in self.enum and v != "IP":
			raise ValueError("Illegal value for enum: %s (%s)" % (v, self.enum))
		
		return v
	
	@property
	def type_to_check(self):
		if isinstance(self.type, (tuple, list, set, frozenset)):
			assert self.type
			return self.type[0]
		
		assert isinstance(self.type, type)
		return self.type
	
	@property
	def label(self):
		return self.displayname and self.displayname or self.name
		
class Password(unicode):
	pass
		
class List(LoggerMixin, list):
	def __init__(self, entity, field, iterable):
		super(List, self).__init__(iterable)
		
		#self.__list = list(iterable)
		self.__entity = entity
		self.__field = field
		
	def __len__(self):
		return super(List, self).__len__()
	
	def __typecheck(self, v):
		if not isinstance(v, self.__field.type_to_check):
			raise TypeError("Wrong type for %s of %s: need %s have %s (%s)" % (self.__field.name, self.__entity, self.__field.klass, v, type(v)))
	
	def __getitem__(self, k):
		return super(List, self).__getitem__(k)
	
	def __setitem__(self, k, v):
		self.__typecheck(v)
		super(List, self).__setitem__(k, v)
		self.__entity.set_is_updated(True)
	
	def __delitem__(self, k):
		super(List, self).__delitem__(k)
		self.__entity.set_is_updated(True)
		
	def append(self, o):
		self.__typecheck(o)
		self.logger.info("Adding: %s to list of %s" % (self, self.__entity))
		super(List, self).append(o)
		self.logger.debug("appended an item. flagging entity for update. (%s)" % (self, ))
		self.__entity.set_is_updated(True)
		
	def insert(self, index, object):
		self.__typecheck(object)
		super(List, self).insert(index, object)
		self.__entity.set_is_updated(True)
		
	def remove(self, o):
		super(List, self).remove(o)
		self.__entity.set_is_updated(True)
		
class AbstractBackref(object):
	def __init__(self, klass, name, *args, **kw):
		super(AbstractBackref, self).__init__(*args, **kw)
		self.__name = name
		self.__klass = klass
		
	@property
	def name(self):
		return self.__name
	
	@property
	def klass(self):
		return self.__klass
	
class Backref(AbstractBackref):
	def __get__(self, entity, klass):
		assert entity.is_persistent
		
		return entity.repository.list_entities(self.klass, **{self.name: entity})
	
class ScalarBackref(Backref):
	def __get__(self, entity, klass):
		l = super(ScalarBackref, self).__get__(entity, klass)
		assert len(l) < 2
		return l and l[0] or None
	
class ListBackref(AbstractBackref):
	def __get__(self, entity, klass):
		assert entity.is_persistent
		return entity.repository.list_entities(self.klass, owns = {self.name: entity})

class Self(object):
	pass

class EntityType(type):
	def __init__(self, name, bases, dct):
		super(EntityType, self).__init__(name, bases, dct)
		
		for f in self.__dict__.get("__fields__", ()):
			if f.backref:
				target = f.type

				if hasattr(target, f.backref):
					raise NameError("Type %s already has attribute %s" % (target.__name__, f.backref))
				if isinstance(target, (tuple, list, set)):
					setattr(target[0], f.backref, ListBackref(self, f.name))
				elif not (isinstance(target, type) and issubclass(f.type, Entity)):
					raise TypeError("Illegal type for backref: " + str(f.type))
				else:
					if f.shared:
						setattr(target, f.backref, Backref(self, f.name))
					else:
						setattr(target, f.backref, ScalarBackref(self, f.name))
								
class AbstractEntityType(EntityType, ABCMeta):
	pass
		
#TODO: make properties work on Entity (set part)
class Entity(LoggerMixin): 
	__metaclass__ = EntityType

	__fields__ = ()
	
	__ignore__ = ()
	
	__allow_attributes__ = ()
	
	__field_cache = {}
	
	def __init__(self, id = None, fields = None, *args, **kw):			   
		self.__id = id
		self.__updated = id is None
		self.__fields_updated = set()
		#print ("11111 creating %s %s %s %s" % (self.__class__, id, fields, kw))
				
		if id is not None:
			assert isinstance(fields, dict)
			self._set_fields(fields)
		else:
			if fields is None:
				fields = {}
			self.__fields = {}
			for field in self.get_fields():
				self.__fields[field.name] = None
				#if field.type == Password:
				#	setattr(self, field.name, field.get_default_value())
				#else:
				#print("Setting field %s from %s in %s" % (field.name, kw, self))

				setattr(self, field.name, kw.pop(field.name, fields.get(field.name, field.get_default_value())))
				
			if kw:	
				self.logger.debug("Remaining kwargs: %s", kw)
		super(Entity, self).__init__(*args, **kw)
		
	def __getattr__(self, name):
		if name.startswith("_") or self.__fields is None:
			raise AttributeError(name)
		try:
			return self.__fields[name]
		except KeyError:
			raise AttributeError("%s object has no attribute %s" % (self.__class__.__name__, name))
	
	def __setattr__(self, k, v):
		if k.startswith("_") or k in self.__allow_attributes__:
			return super(Entity, self).__setattr__(k, v)

		fields = self.get_field_dict()
		# print("---setting %s %s %s" % (k, v, self.id))
		try:
			field = fields[k]
		except KeyError:
			raise AttributeError(k)

		#TODO: do properly
		if k == "commonName" and v is None:
			v = u"auto generated"
		else:
			v = field.convert(v, self)
		
		old = self.__fields[k]
		self.__fields[k] = v 
		if v != old:
			if not self.__updated:
				self.logger.debug("%s needs update due to %s != %s" % (repr(self), v, old))
			#else:
			#	self.logger.debug("Updated field: %s.%s: %s" % (repr(self), k, v))
			self.__updated = True
			self.__fields_updated.add(k)
	
	def get_values(self):
		return tuple( (f, getattr(self, f.name)) for f in self.get_fields() )
		#return self.__fields.items()
	values = property(get_values)
	
	def get_value_dict(self):
		return self.__fields.copy()
	value_dict = property(get_value_dict)
	
	@property
	def fieldcount(self):
		return len(self.__fields)
	
	@classmethod
	def get_fields(klass):
		try:
			return Entity.__field_cache[klass][0]
		except KeyError:
			fields = []
			
			bases = list(klass.__mro__)
			bases.reverse()
			for c in bases:
				newfields = list(c.__dict__.get("__fields__", []))
				for f in newfields:
					if not isinstance(f, Field):
						f = Field(*f)
					if f.type is Self:
						f = Field(f.name, c, *f[2:])
					fields.append(f)

			#klass.get_logger().debug("Created fields for %s: %s" % (klass, fields))
			field_dict = {}
			for field in fields:
				field_dict[field.name] = field

			Entity.__field_cache[klass] = (fields, field_dict)
			return fields
				
	@classmethod
	def get_field_dict(klass):
		klass.get_fields()
		return Entity.__field_cache[klass][1]
	
	@classmethod
	def get_field(klass, name):
		return klass.get_field_dict()[name]
	
	def __eq__(self, o):
		return self is o or ((isinstance(o, self.__class__) or isinstance(self, o.__class__)) and self.id == o.id)
	
	def _get_fields(self):
		return self.__fields
	def _set_fields(self, fields):
		assert(isinstance(fields, dict))
		attrs = self.get_fields()
		
		#self.logger.debug("Setting fields of %s. Updated Fields: %s", repr(self), self.__fields_updated)
		
		for attr in attrs:
			if attr.name not in self.__fields_updated:
				try:
					v = fields[attr.name]
				except KeyError:
					raise InternalError("Missing value for %s (%s)" % (attr.name, fields))
				fields[attr.name] = attr.convert(v, self)
			else:
				assert self.__updated
				v = getattr(self, attr.name)
				#self.logger.debug("Skipping attribute %s of %s since it has been updated. (%s)", attr.name, repr(self), v)
				fields[attr.name] = v
			
		self.__fields = fields
		
	def get_id(self):
		return self.__id
	def _set_id(self, id):
		assert self.id is None and id is not None, "Id assertion failed: %s -> %s" % (self.id, id)
		self.__id = id
	id = property(get_id)
	
	def get_is_persistent(self):
		return self.__id is not None
	is_persistent = property(get_is_persistent)
	
	def get_is_updated(self):
		return self.__updated
	def set_is_updated(self, v):
		self.__updated = v
		if not v:
			self.__fields_updated.clear()
	is_updated = property(get_is_updated, set_is_updated)
	
	@property
	def display_name(self):
		return unicode(self.id)
	
	@property
	def display_typename(self):
		return self.get_display_typename()
		
	@classmethod
	def get_display_typename(klass):
		try:
			return klass.__display_typename__
		except AttributeError:
			return klass.__name__
	
	@property
	def typename(self):
		return self.__class__.__name__
	
	def dump(self):
		return "%s: %s - %s" % (self.__class__.__name__, self.id, self.__fields)
	
	def __repr__(self):
		return "%s-%s" % (self.__class__.__name__, self.id)
	