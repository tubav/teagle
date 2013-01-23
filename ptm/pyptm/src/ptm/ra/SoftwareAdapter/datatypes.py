#! /usr/bin/env python

import sqlalchemy

class Datatype(object):

	def __init__(self, name = None):
		self.__name = name

	def get_name(self):
		return self.__name
	def set_name(self, name):
		self.__name = name
	name = property(get_name, set_name)

class AtomicType(Datatype):
	def __init__(self, alctype, name = None, nullable = True):
		super(AtomicType, self).__init__(name)
		self.__alctype = alctype
		self.__nullable = nullable

	def make_column(self, name):
		if self.name is None:
			self.name = name

		if not self.name:
			raise ValueError("No name for Datatype given: " + str(name))

		return sqlalchemy.Column(self.name, self.__alctype, nullable = self.__nullable)

	def get_attribute_name(self):
		return self.__name
	
class String(AtomicType):
	def __init__(self, name = None, nullable = True, long = False):
		if long: 
			t = sqlalchemy.types.Unicode()
		else:
			t = sqlalchemy.types.Unicode(255)

		super(String, self).__init__(alctype = t, name = name, nullable = nullable)


class Reference(String):
	def __init__(self, name = None, nullable = True):
		assert(name is not False)
		super(Reference, self).__init__(name = name, nullable = nullable)
		
	def get_attribute_name(self):
		return self.name + "_uuid"

class Integer(AtomicType):
	def __init__(self, name = None, nullable = True):
		super(Integer, self).__init__(alctype = sqlalchemy.Integer, name = name, nullable = nullable)

class Float(AtomicType):
	def __init__(self, name = None, nullable = True):
		super(Float, self).__init__(alctype = sqlalchemy.Float, name = name, nullable = nullable)
	

class Boolean(AtomicType):
	def __init__(self, name = None, nullable = True):
		super(Boolean, self).__init__(alctype = sqlalchemy.Boolean, name = name, nullable = nullable)

import inspect

class List(Datatype):
	def __init__(self, payload, name = None):
		super(List, self).__init__(name = name)

		if inspect.isclass(payload):
			payload = payload()

		self.payload = payload

