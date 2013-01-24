'''
Created on 28.08.2011

@author: kca
'''

from teagle.serializer import TestbedSerializer
from teagle.base import Testbed
from teagle.repository.entities import ResourceInstance

class LegacyTestbedSerializer(TestbedSerializer):
	def _serialize_value(self, v, doc):
		if isinstance(v, ResourceInstance) and not v.provisioned:
			v = "$dynid(%s)" % (v.identifier, )
		return super(LegacyTestbedSerializer, self)._serialize_value(v, doc)
	
	def _add_connection(self, type, src, dst, node, doc):
		connection = doc.createElement("connection")
		typeelem = doc.createElement("type")
		typeelem.appendChild(doc.createTextNode(type))
		srcelem = doc.createElement("src")
		dstelem = doc.createElement("dst")
		srcid = doc.createElement("id")
		dstid = doc.createElement("id")
		srcid.appendChild(doc.createTextNode(src.identifier))
		dstid.appendChild(doc.createTextNode(dst.identifier))
		srcelem.appendChild(srcid)
		dstelem.appendChild(dstid)
		connection.appendChild(typeelem)
		connection.appendChild(srcelem)
		connection.appendChild(dstelem)
		node.appendChild(connection)
	
	def _fill_doc(self, o, doc):
		root_elem = super(LegacyTestbedSerializer, self)._fill_doc(o, doc)
		if (isinstance(o, Testbed)):
			connections = doc.createElement("connections")				
			
			for i in o.instances:
				if i.parent:
					self._add_connection("contains", i.parent, i, connections, doc)
				for ri in i.get_references():
					self._add_connection("references", i, ri, connections, doc)
			root_elem.appendChild(connections)
		return root_elem
	
	def _dump_resource(self, parent, doc, resource):
		elem = super(LegacyTestbedSerializer, self)._dump_resource(parent, doc, resource)
		self._add_text_node(elem, "id", resource.identifier, doc)
		return elem
