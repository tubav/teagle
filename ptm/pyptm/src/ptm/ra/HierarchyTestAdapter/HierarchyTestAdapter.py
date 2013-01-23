'''
Created on 13.08.2010

@author: kca
'''

import sys
sys.path.append("/home/kca/vcs/ptm/pyptm/src")
print (sys.path)

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import InstanceNotFound, DuplicateNameError, ConfigurationAttributeError
import logging

logger = logging.getLogger("ptm")

#For this (very simple) example we will implement a basic RA that manages only onetype of resource: called "simpletest".
#The type has no further configuration and can be freely instantiated and deleted. 

#For this example we will extend AbstractResourceAdpters, which is absolutely basic.
#We'll have to do everything ourselves, some of it might seem redundant or plain odd.
#There are a number of convenience classes that provide default behaviour for a number of different scenarios.
#These will be covered later

class HierarchyTestAdapter(AbstractResourceAdapter):
	'''
	classdocs
	'''

	#ctor for our RA. By convention it MUST accept the manager argument.
	#please refer to http://fuhm.net/super-harmful/ if you wonder about the ctor signature / supercall
	def __init__(self, parent, manager, *args, **kw):
		super(HierarchyTestAdapter, self).__init__(*args, **kw)
		
		#this is where we store our instances. We simply store the names of all existing instances.
		#here, one instance ("0") already exists. The full id of this instance would be "/simpletest-0"
		self.__instances = set("0")
		self.__instances0 = set("0")
		
		#tell the manager to announce that we are responsible for that type "simpletest" at the root of the hierarchy
		#manager.register_adapter("/level0-0", self)
		manager.register_adapter("/level0", self)
		manager.register_adapter("/level0*/level1", self)
		
		logger.debug("---up---")
		
	def _get_set(self, typename):
		assert(typename in ("level0", "level1"))

		if typename == "level0":
			return self.__instances0
		assert typename == "level1"
		return self.__instances
		
	#This will get called whenever someone requests a list of existing instances.
	#parent tells us what part of the hierarchy is requested.
	#typename tells us which types to include in the list. None means all types. 
	def list_resources(self, parent, typename):
		
		#The asserts are here only to show you what cou can rely on.
		#since we have only registered for the type "simpletest", thats the only type that can be requested
		assert(typename in (None, "level0", "level1"))
		
		#our resources have no parent
		assert(parent == None or parent == "/level0-0")
		
		if not parent:
			return [ Identifier("/level0-" + i) for i in self.__instances0 ]
		
		#we have the option of returning a list of objects which represent instances (will be covered later)
		#or just a list of the exitsing IDs. For simplicity we'll do the latter.
		return [ Identifier("/level0-0/level1-" + i) for i in self.__instances ]

	#This will be called whenever someone requests to instantiate a new instance of a certain type
	#parent_id specifies the parent of the new instance
	#name specifies a name to be given for the new resource.
	#   it can be None or empty in which case the RA must generate a unique name
	#   if a name is given, the RA _must_ adhere to it. If the name already exists, its an error.
	def add_resource(self, parent_id, name, typename, config, owner = None):
		assert(typename in ("level1", "level0"))
		
		instances = self._get_set(typename)
		
		if not name:
			#No name was specified. generate one.
			i = 0
			while True:
				n = str(i)
				if n not in instances:
					break
				i += 1
			name = n
		else:
			#A name was specified. Check if its still available.
			if name in self.__instances:
				raise DuplicateNameError(parent_id, typename, name)
		
		#Add the new instance
		instances.add(n)
		
		return name

	#tell the system if a given id exists
	def have_resource(self, identifier):
		#assert(identifier.parent == None)
		#assert(identifier.typename == "simpletest")
		instances = self._get_set(identifier.typename)
		
		return identifier.name in instances

	#return a given resource instance.
	#since we dont have some specific implementation, it boils down to checking if the instance exists.
	def get_resource(self, identifier):
		if not self.have_resource(identifier):
			raise InstanceNotFound(identifier)
		return identifier


	#Return the config of an existing instance. 
	def get_configuration(self, identifier):
		#assert(identifier.parent == None)
		#assert(identifier.typename == "simpletest")
		
		if not self.have_resource(identifier):
			raise InstanceNotFound(identifier)
		
		#since our instances have no config we return an empty dict
		return { "key": "value" }

	#reconfigure an existing instance
	def set_configuration(self, identifier, config):
		#assert(identifier.parent == None)
		#assert(identifier.typename == "simpletest")
		
		#Just ignore it
		return
			

	#return a single config parameter of an existing instance
	def get_attribute(self, identifier, name):
		#assert(identifier.parent == None)
		#assert(identifier.typename == "simpletest")
		
		if not self.have_resource(identifier):
			raise InstanceNotFound(identifier)
		
		if name == "key":
			return "value"
		
		#Our instances have no config, so this is always an error
		raise ConfigurationAttributeError(name)

	#set a single config parameter
	def set_attribute(self, identifier, name, value):
		#assert(identifier.parent == None)
		#assert(identifier.typename == "simpletest")
		
		#Our instances have no config, so this is always an error
		raise ConfigurationAttributeError(name)

	#delete an existing instance
	def delete_resource(self, identifier):
		assert False
		
def RARunner():
	from ptm.ManagerServer import ManagerServer
	
	logger = logging.getLogger("ptm")
	console = logging.StreamHandler()
	formatter = logging.Formatter('Manager: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
	console.setFormatter(formatter)
	console.setLevel(logging.DEBUG)
	logger.setLevel(logging.DEBUG)
	logger.addHandler(console)

	#module, _, klassname = fullname.rpartition(".")
	
	klass = HierarchyTestAdapter
	
	m = ManagerServer(None, port = 8001, registry_url = "http://localhost:7000")
	_a = klass(parent = None, manager = m.manager)
	m.serve_forever()

if __name__ == '__main__':
	RARunner()
		
