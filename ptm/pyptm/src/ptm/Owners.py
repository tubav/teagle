#! /usr/bin/env python

from Identifier import Identifier

class Owners(object): 
	def __get_string(self, s):
		if not isinstance(s, basestring): 
			raise Exception("Illegal value for string: %s" % (s,))
		return unicode(s)
	
	def __init__(self, data, *args, **kw):
		super(Owners, self).__init__(*args, **kw)
		
		try:
			self.__anonymous = int(data[0])
			self.__outside = tuple( map(self.__get_string, data[1]) )
			self.__strong = tuple( [ Identifier(i, need_abs = True) for i in map(self.__get_string, data[2]) ] )
			self.__weak = tuple( [ Identifier(i, need_abs = True) for i in map(self.__get_string, data[3]) ] )
		except Exception, e:
			raise Exception("Error parsing owners: %s" % e)

	def get_weak_owners(self):
		return self.__weak
	weak_owners = property(get_weak_owners)

	def get_strong_owners(self):
		return self.__strong
	strong_owners = property(get_strong_owners)

	def get_outside_owners(self):
		return self.__outside
	outside_owners = property(get_outside_owners)

	def get_anonymous_owners(self):
		return self.__anonymous
	anonymous_owners = property(get_anonymous_owners)	

	def as_tuple(self):
		return ( self.anonymous_owners, self.outside_owners, self.strong_owners, self.weak_owners )
		
	def __nonzero__(self):
		return self.__anonymous_owners or self.__outside_owners or self.__strong_owners