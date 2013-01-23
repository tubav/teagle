'''
Created on 25.07.2011

@author: kca
'''

from ptm import Identifier
from ptm.exc import IdentifierException

class GlobalIdentifier(Identifier):
	PREFIX_PREFIX = ""
	PREFIX_SEPARATOR = "."
	PREFIX_ILLEGAL = Identifier.SEPARATOR
	
	def __init__(self, identifier, prefix = None, default_prefix = None, *args, **kw):
		prefix = prefix and unicode(prefix) or ""
		if self.PREFIX_ILLEGAL in prefix:
			raise IdentifierException("Illegal character '%s' in prefix %s" % (self.PREFIX_ILLEGAL, prefix)) 

		if isinstance(identifier, GlobalIdentifier):
			if not prefix:
				prefix = identifier.prefix
		elif not isinstance(identifier, Identifier):
			identifier = unicode(identifier)
			if identifier and not identifier.startswith(self.SEPARATOR):
				pre, _, id = identifier.partition(self.PREFIX_SEPARATOR)
				if not id:
					identifier = pre
				else:
					if self.PREFIX_ILLEGAL in pre:
						raise IdentifierException(u"Missing prefix on global identifier: %s" % (identifier, ))
					if not prefix:
						prefix = pre
				identifier = id
			
			if not prefix:
				prefix = default_prefix
				if not prefix:
					raise IdentifierException(u"Missing prefix for global identifier: %s" % (identifier, ))
		
		self.__prefix = prefix
		kw["need_abs"] = True
		super(GlobalIdentifier, self).__init__(identifier = identifier, *args, **kw)
		
	@property
	def prefix(self):
		return self.__prefix
	
	@property
	def identifier(self):
		return self.__prefix + self.PREFIX_SEPARATOR + super(GlobalIdentifier, self).identifier

	@property
	def parent(self):
		if not self.is_adapter and len(self) <= 1:
			return self.__class__(Identifier.SEPARATOR, prefix = self.prefix)
		return super(GlobalIdentifier, self).parent
	
def main():
	g = GlobalIdentifier("testptm.")
	print (g)

if __name__ == "__main__":
	main()
		
		