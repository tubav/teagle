'''
Created on 26.08.2011

@author: kca
'''

from teagle.t1.T1Serializer import T1Serializer

class DynamicReference(object):
	def __init__(self, identifier):
		self.identifier = identifier

class OEInputParser(T1Serializer):
	def _unserialize_reference(self, id):
		if id.startswith("$dynid(") and id[-1] == ")":
			dynid = id[7:-1]
			if not dynid:
				raise ValueError("Illegal dynid: %s" % (id, ))
			self.logger.debug("Creating dynid: %s" % (dynid, ))
			return DynamicReference(dynid)
		return super(OEInputParser, self)._unserialize_reference(id)