#! /usr/bin/env python
from exc import IdentifierException
	
class Identifier(object):
	SEPARATOR = u"/"
	TYPE_SEPARATOR = u"-"
	WILDCARD = u"*"
	CURRENT = u"."

	def __init__(self, identifier, need_full = None, need_abs = None, need_name = None, need_type = None, trust = False, *args, **kw):
		super(Identifier, self).__init__(*args, **kw)
	#	print("huhu")
		if trust:
			self.__identifier = identifier
			return
		if isinstance(identifier, Identifier):
			self.__identifier = identifier.__identifier
		elif hasattr(identifier, "identifier"):
			self.__identifier = unicode(identifier.identifier)
		else:
			self.__identifier = Identifier.__trim(identifier)

		if need_full is not None:
			need_abs = need_full
			need_name = need_full
			need_type = need_full

		if need_abs is not None and self.is_absolute != need_abs:
			raise IdentifierException("Need an absolute id here, not " + self.__identifier)

		if need_name is not None  and self.is_adapter == need_name:
			raise IdentifierException("Need a full id here, not " + self.__identifier)

		if need_type is not None and need_type == (not self.typename):
			raise IdentifierException("Need at least a typename here, not " + self.__identifier)
			

	@staticmethod
	def __trim(identifier):
		if not identifier:
			return Identifier.SEPARATOR
		identifier = unicode(identifier)
		double = Identifier.SEPARATOR + Identifier.SEPARATOR
		while double in identifier:
			identifier = identifier.replace(double,  Identifier.SEPARATOR)

		if identifier == Identifier.SEPARATOR or identifier == Identifier.CURRENT:
			return identifier

#		if not identifier.startswith(Identifier.SEPARATOR) and not identifier.startswith(Identifier.CURRENT + Identifier.SEPARATOR):
#			identifier = Identifier.CURRENT + Identifier.SEPARATOR + identifier
		return Identifier.__check(identifier)

	@staticmethod
	def __check(identifier):
		current = Identifier.CURRENT
		parts = identifier.split(Identifier.SEPARATOR)
		
		if identifier.count(Identifier.WILDCARD) > 1:
			raise IdentifierException("At most one wildcard character may be present in an id. (%s)" % (identifier, ))

		if parts[0]:
			result = []
		else:
			result = [""]
			
		#start = identifier.startswith(Identifier.SEPARATOR) and 1 or 0
		#stop = identifier.endswith(Identifier.SEPARATOR) and -2 or -1
		start = 1
		stop = -2

		result += [ Identifier.__check_element(e) for e in parts[start:stop] if e != current ]

		second_to_last = identifier.startswith(Identifier.SEPARATOR) and 2 or 1
		if len(parts) > second_to_last:
			if parts[-2].endswith(Identifier.WILDCARD):
				if Identifier.TYPE_SEPARATOR in parts[-2] or Identifier.TYPE_SEPARATOR in parts[-1]:
					raise IdentifierException(u"Illegal wildcard id: " + identifier)
				Identifier.__check_chars(parts[-2][:-1])
				result.append(parts[-2])		
			else:	
				result.append(Identifier.__check_element(parts[-2]))	

		last = parts[-1]
		if last == Identifier.CURRENT or last == u'':
			result.append(u'')
		elif last.endswith(Identifier.WILDCARD):
			if Identifier.TYPE_SEPARATOR in last:
				raise IdentifierException(u"Illegal wildcard id: " + identifier)
			Identifier.__check_chars(last[:-1])
			result.append(last)
		elif not Identifier.TYPE_SEPARATOR in last:
			Identifier.__check_chars(last)
			result.append(last)
		else:
			result.append(Identifier.__check_element(last))

		return Identifier.SEPARATOR.join(result)

	@staticmethod
	def __check_chars(id):
		for c in id:
			if c not in _LEGAL_CHARS:
				raise IdentifierException(u"Illegal character '%s' in id '%s'" % (c, id))

	@staticmethod
	def __check_element(id):
		if not id or _UP == id:
			raise IdentifierException(u"Illegal Id: '%s'" % (id, ))

		Identifier.__check_chars(id)

		i = id.find(Identifier.TYPE_SEPARATOR)
		if i < 0 or i == len(id) - 1:
			msg = u"Id '%s' is not well formed, missing or misplaced '%s'" % (id, Identifier.TYPE_SEPARATOR)
			raise IdentifierException(msg)
		return id

	@property
	def identifier(self):
		return self.__identifier

	def __repr__(self):
		return self.identifier

	def __hash__(self):
		return hash(self.identifier)
		
	@property
	def parts(self):
		return self.__identifier.split(self.SEPARATOR)

	def __get_elements(self):
		elements = self.parts
		if self.is_absolute:
			elements = elements[1:]
		if not self.typename:
			elements = elements[:-1]
		return elements

	def get_elements(self):
		return [ Identifier(e) for e in self.__get_elements() ]
	elements = property(get_elements)

	def get_slice(self, start = None, stop = None, step = None):
		#print("get_slice")
		elements = self.__get_elements()
		if self.is_adapter and not self.typename:
			elements.append("")
#		print(elements)
		sliced_elements = elements[start:stop:step]
#		print(sliced_elements)
		result = Identifier.SEPARATOR.join(sliced_elements)
#		print(result)
		#if result and self.is_absolute and (not start or len(sliced_elements) == len(elements)):
		if result and self.is_absolute:
			result = Identifier.SEPARATOR + result

		return Identifier(result)

	#TODO: iterator
	def __getitem__(self, i):
		#print("getitem %s" % (i, ))
		if isinstance(i, slice):
			#print("getslice")
			return self.get_slice(i.start, i.stop, i.step)
		if self == "/":
			raise IndexError(i)			
		#print("getnormal")
		element = self.__get_elements()[i]
		return Identifier(element)

	def __eq__(self, o):
		try:
			o = self.__class__(o)
		except IdentifierException:
			return False

		return self.identifier == o.identifier

	def __ne__(self, o):
		return not (self == o)

	def __rne__(self, o):
		return not (self == o)

	def __req__(self, o):
		return self == o
	
	def __sub__(self, o):
		o = unicode(o)
		self.__check_chars(o)
		if not self.is_adapter or not self.typename:
			raise IdentifierException("Cannot add name to %s" % (self, ))
		
		return self.__class__(self.identifier + self.TYPE_SEPARATOR + o)

	@property
	def is_absolute(self):
		return self.__identifier.startswith(self.SEPARATOR)

	@property
	def is_relative(self):
		return not self.is_absolute
	
	@property
	def is_adapter(self):
		return Identifier.TYPE_SEPARATOR not in self.parts[-1]
		
	@property
	def is_wildcard(self):
		return self.__identifier.endswith(self.WILDCARD)

	def get_managed_type(self):
		if not self.is_adapter:
			raise IdentifierException(u"I am not a manager (%s)" % (self.__identifier, ))
		t = self.parts[-1]
		if self.is_wildcard:
			t = t[:-len(Identifier.WILDCARD)]

		if t:
			return t
		return None
	managed_type = property(get_managed_type)
		
	def get_is_root(self):
		return self.__identifier.startswith(Identifier.SEPARATOR) and not self.elements
	is_root = property(get_is_root)

	def get_is_current(self):
		return self.__identifier == Identifier.CURRENT or self.__identifier == Identifier.CURRENT + Identifier.SEPARATOR + Identifier.WILDCARD or self.__identifier == Identifier.CURRENT + Identifier.SEPARATOR
	is_current = property(get_is_current)

	@property
	def parent(self):
		if self.is_adapter:
			if self.is_root:
				return self
			return self[:]
		if len(self) <= 1:
			return self.__class__(Identifier.SEPARATOR)
		return self.__class__(self.identifier.rpartition(Identifier.SEPARATOR)[0])
		
	@property
	def manager_name(self):
		if self.is_wildcard:
			return Identifier(self.identifier[:-len(Identifier.WILDCARD)])
		if self.is_adapter or self.is_current:
			return self
		return self.dirname / self.typename

	@property
	def dirname(self):
		parts = self.__identifier.rpartition(Identifier.SEPARATOR)
		return Identifier(parts[0] + parts[1])
	
	@property
	def basename(self):
		return self.__identifier.rsplit(self.SEPARATOR, 1)[-1]

	@property
	def typename(self):
		if self.is_current:
			return None
		if self.is_adapter:
			return self.managed_type
		return self.__get_name_part(0)

	@property
	def resourcename(self):
		if self.is_adapter:
			return None
		return self.__get_name_part(1)
	name = resourcename

	def __get_name_part(self, i):
		return str(self.basename).split(Identifier.TYPE_SEPARATOR, 1)[i]

	def __div__(self, identifier):
		identifier = self.__class__(identifier)
#		print("1: %s" % (identifier, ))
		if self.is_adapter:
#			print("2: %s" % (identifier, ))
			if identifier.is_root:
#				print("3: %s" % (identifier, ))
				return self
			if identifier.is_current:
#				print("4: %s" % (identifier, ))
				if identifier.is_wildcard and not self.is_wildcard:
#					print("5: %s" % (identifier, ))
					return self.__class__(self.identifier + Identifier.WILDCARD)
#				print("6: %s" % (identifier, ))
				return self
#			print("7: %s" % (identifier, ))
			u = self.dirname.__identifier
		elif not identifier.is_absolute:
#			print("8: %s" % (identifier, ))
			u = self.identifier + Identifier.SEPARATOR
		else:
#			print("9: %s" % (identifier, ))
			u = self.identifier
#		print("10: %s" % (u, ))
		return self.__class__(u + identifier.__identifier)

	__truediv__ = __div__

	def __rdiv__(self, o):
		if not o:
			o = self.__class__(self.SEPARATOR)
		return self.__class__(o) / self

	__rtruediv__ = __rdiv__

	__add__ = __div__
	
	def __radd__(self, o):
		if isinstance(o, basestring):
			return o + unicode(self)
		return self.__class__(o) + self

	def __len__(self):
		return len(self.elements)

	def __nonzero__(self):
		return len(self) != 0
	__bool__ = __nonzero__

	def is_responsible_for(self, identifier):
		if not self.is_adapter:
			raise IdentifierException("Only adapters are responsible for stuff (%s)" % (self, ))
#		my_name = self.manager_name

		return identifier.__identifier.startswith(self.manager_name.__identifier) and (self.is_wildcard or self.dirname == identifier.dirname)

	def make_child_identifier(self, typename, name):
		if not typename:
			raise IdentifierException("No type name given")
		if not name:
			raise IdentifierException("No name given")
		name = unicode(name)
		self.__check_chars(name)
		return self / (unicode(typename) + Identifier.TYPE_SEPARATOR + name)

	def relpath_to(self, identifier):
		identifier = self.__class__(identifier)

		if not identifier.is_absolute:
			return identifier

		if self.is_adapter:
			myid = self.dirname.__identifier
		else:
			myid = self.__identifier

		if not identifier.__identifier.startswith(myid):
			raise IdentifierException("No relative path from '%s' to '%s'" % (myid, identifier.__identifier))

		identifier = identifier.__identifier[len(myid):]
		if identifier.startswith(Identifier.SEPARATOR):
			identifier = Identifier.CURRENT + identifier
			
		return self.__class__(identifier)

	def manager_for(self, type = '', wildcard = False):
		if type:
			Identifier.__check_chars(type)
		else:
			type = u''

		if not self.is_adapter:
			self = self / ''

		if wildcard:
			wildcard = Identifier.WILDCARD
		else:
			wildcard = u''

		return self.__class__(unicode(self.dirname) + unicode(type) + wildcard)
	adapter_for = manager_for

	def get_submanager(self):
		if self.is_adapter:
			return self
		return self.manager_for()
	subadapter = submanager = property(get_submanager)

	def add_wildcard(self):
		if self.is_wildcard:
			return self
		return self.__class__(self.submanager.identifier + Identifier.WILDCARD)

	def startswith(self, identifier):
		return self.identifier.startswith(unicode(identifier))
	
	def endswith(self, identifier):
		return self.identifier.endswith(unicode(identifier))

import string

_LEGAL_CHARS = string.letters + string.digits + string.punctuation.replace(Identifier.SEPARATOR, '').replace(Identifier.WILDCARD, '')
_UP = Identifier.CURRENT + Identifier.CURRENT

del string

