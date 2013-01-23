'''
Created on 04.06.2009

@author: kca
'''
from ptm.Resource import Resource
from socket import gethostbyname

class Node(Resource):
	'''
	classdocs
	'''

	def __init__(self, hostname, pw, adapter, identifier = None, parent_id = None, type = None, name = None):
		Resource.__init__(self, adapter, identifier, parent_id, type, name)
		if not isinstance(hostname, basestring):
			raise TypeError("Need a string for Hostname: " + str(hostname))
		self.__hostname = hostname
		self.__pw = pw
	
	def get_hostname(self):
		return self.__hostname
	hostname = property(get_hostname)

	def get_public_ip(self):
		return gethostbyname(self.hostname)
	public_ip = property(get_public_ip)

	def _get_configuration(self):
		return dict(hostname = self.identifier.basename, public_ip = self.public_ip, password = self.__pw)

	def _set_attribute(self, name, value):
		pass
