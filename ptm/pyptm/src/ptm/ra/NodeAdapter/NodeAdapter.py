'''
Created on 04.06.2009

@author: kca
'''

from Node import Node 

from ptm.ResourceAdapter import ResourceAdapter

class NodeAdapter(ResourceAdapter):
	'''
	classdocs
	'''


	def __init__(self, manager, parent = None):
		ResourceAdapter.__init__(self, manager)
		self.__nodes = { "0": Node("192.168.144.56", "*4teagle#", self, "/pnode-0") }
		self.register("pnode")
		
	def _get_resource(self, uuid):
		if uuid.resourcename != "0":
			raise Exception("No such node here: " + uuid)
		return self.__nodes[uuid.resourcename]

	def _add_resource(self, *args, **kw):
		return self.__nodes["0"]
		#raise Exception("Adding nodes is not supported")

	def _list_resources(self, parent, type = None):
		return self.__nodes.values()
	
	def _set_attribute(self, *args, **kw):
		pass
