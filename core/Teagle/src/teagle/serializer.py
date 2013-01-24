'''
Created on 21.07.2011

@author: kca
'''

from xml.dom.minidom import getDOMImplementation, parseString, Element
#from . import T1Identifier, AbstractT1Resource
from cStringIO import StringIO
from ngniutils.logging import LoggerMixin
from base import Resource, Identifier, Testbed
from ngniutils import encstr, uc
from abc import ABCMeta, abstractmethod

class TeagleSerializer(LoggerMixin):
	__metaclass__ = ABCMeta
	
	dom_implementation = getDOMImplementation()
	
	@abstractmethod
	def _unserialize_reference(self, id):
		raise NotImplementedError()

	def unserialize(self, rfile):
		if isinstance(rfile, basestring):
			self.logger.debug("Unserializing: %s" % (rfile, ))
			doc = parseString(rfile)
		else:
			return self.unserialize(rfile.read())
		return self._unserialize_doc(doc)

	def __find_config_elem(self, root):
			for e in root.childNodes:
				if isinstance(e, Element) and e.tagName.lower() == 'configuration':
					return e
			return root

	def _unserialize_doc(self, doc):
		try:
			root = doc.documentElement
			typename = root.tagName
			action = root.getAttribute("action")
			root = self.__find_config_elem(root)
			if root != doc.documentElement:
				root = self.__find_config_elem(root)

			config = self._unserialize_config(root)
			return typename, config, action
		finally:
			doc.unlink()

	def _unserialize_config(self, root):
		config = dict()
		for e in root.childNodes:
			if isinstance(e, Element):
				config[str(e.tagName).replace("__", "_")] = self.__unserialize(e)
		return config

	def __unserialize(self, element, need_type = None):
#			raise Exception("Malformed input: " + str(element))

		type = None
		if element.hasAttribute("type"):
			type = element.getAttribute("type").lower()
			if type.endswith("array"):
				if type == "array" or type == "object-array":
					type = None
				else:
					type = type.rsplit("-", 1)[0]

				return [ self.__unserialize(e, type) for e in element.childNodes if isinstance(e, Element) ]

		if need_type is not None and type != need_type:
			if type is not None:
				raise TypeError("Wrong type on attribute: %s. need %s" % (type, need_type))
			type = need_type	

		child = element.firstChild
		if child is None:
			return None

		return self._unserialize_value(type, child.data)

	def _unserialize_value(self, type, v):
		if type == "object":
			return None
		if type == "boolean":
			v = unicode(v).lower()
			if v and v[0] in ("1", "t", "y", "j"):
				return True
			return False
		if type == "reference":
			return self._unserialize_reference(v)

		types = {None: unicode, "": unicode, "string": unicode, "float": float, "integer": int}
		try:
			return types[type](v)
		except KeyError:
			raise ValueError("Unknown type: " + str(type))
					
	def serialize(self, resource, wfile = None):
		buf = None
		if wfile is None:
			buf = wfile = StringIO()
			
		doc = self._get_doc(resource)
		try:
			doc.writexml(wfile)
		finally:
			doc.unlink()
			
		if buf is not None:
			return buf.getvalue()
			
	def dumps(self, resource):
		doc = self._get_doc(resource)
		try:
			return doc.toxml()
		finally:
			doc.unlink()
		
	def _get_doc(self, resource):
		doc = self.dom_implementation.createDocument(None, None, None)
		self._fill_doc(resource, doc)
		return doc
	
	def _fill_doc(self, resource, doc):
		if isinstance(resource, Resource):
			self._dump_resource(doc, doc, resource)
		else:
			doc.appendChild(self.__serialize(resource, doc))
			
	def _dump_resource(self, parent, doc, resource):
		return self._dump_resource_data(parent, doc, resource.typename, resource.identifier, resource.config)
	
	def _dump_resource_data(self, parent, doc, typename, identifier, config, action = None):
		root_element = doc.createElement(typename)
		self._dump_resource_config(root_element, doc, identifier, config, action)
		parent.appendChild(root_element)
		return root_element
		
	def _dump_resource_config(self, parent, doc, identifier, config, action = None):
		top_element = doc.createElement("configuration")
		parent.appendChild(top_element)
		if action:
			parent.setAttribute("action", action)

		if identifier is not None:
			element = doc.createElement("identifier")
			element.appendChild(doc.createTextNode(unicode(identifier)))
			element.setAttribute("type", "string")
			top_element.appendChild(element)

		print config
		for a, value in config.iteritems():
			top_element.appendChild(self.__serialize(value, doc, a))
	
	def assist_serialize(self, typename, identifier, config, wfile = None, action = None):
		buf = None
		if wfile is None:
			buf = wfile = StringIO()

		doc = self.dom_implementation.createDocument(None, None, None)
		try:
			self._dump_resource_data(doc, doc, typename, identifier, config, action)
			doc.writexml(wfile, encoding = "utf-8")
		finally:
			doc.unlink()

		if buf is not None:
			return buf.getvalue()
	
	def __get_type_attribute(self, v):
		if v is None:
			return "object"
		if isinstance(v, (Resource, Identifier)):
			return "reference"
		if isinstance(v, bool):
			return "boolean"
		if isinstance(v, (int, long)):
			return "integer"
		if isinstance(v, (str, unicode)):
			return "string"
		if isinstance(v, float):
			return "float"
		if isinstance(v, (tuple, set, frozenset, list)):
			return self.__get_array_tag(v) + "-array"
		if isinstance(v, dict):
			return "map"
		raise TypeError("Illegal type for %s: %s" % (v, v.__class__.__name__))

	def __get_array_tag(self, v):
		klass = None
		obj = None
		#TODO: rewrite
		for i in v:
			if not isinstance(i, (tuple, set, frozenset, list)):
				newklass = type(i)
				if klass is not None:
					if not issubclass(newklass, klass):
						if issubclass(klass, newklass):
							klass = newklass
							obj = i
						else:
							klass = None
							obj = None
							break
				else:
					klass = newklass
					obj = i
			else:
				klass = None
				obj = None
				break

		if obj is not None:
			return self.__get_type_attribute(obj)
		return "object"

	def __tag_name(self, v):
		if isinstance(v, (tuple, set, frozenset, list)):
			return "array"
		return v.__class__.__name__

	def __serialize(self, v, doc, tagname = None):
		if tagname is None:
			tagname = self.__get_type_attribute(v)
		else:
			tagname.replace("_", "__")

		top_element = doc.createElement(tagname)
		ta = self.__get_type_attribute(v)
		if ta is not None:
			top_element.setAttribute("type", ta)
		if isinstance(v, (tuple, set, frozenset, list)):
			for i in v:
				top_element.appendChild(self.__serialize(i, doc))
		elif isinstance(v, dict):
			for k, o in v.iteritems():
				ee = doc.createElement("entry")
				ee.appendChild(self.__serialize(k, doc))
				ee.appendChild(self.__serialize(o, doc))
				top_element.appendChild(ee)
		else:
			top_element.appendChild(self._serialize_value(v, doc))

		return top_element

	def _serialize_value(self, v, doc):
		try:
			v = v.identifier
		except AttributeError:
			pass
		return doc.createTextNode(uc(v))
	
	def _add_text_node(self, parent, name, value, doc):
		top_element = doc.createElement(name)
		if value is not None:
			top_element.appendChild(doc.createTextNode(encstr(value)))
		parent.appendChild(top_element)
	
class TestbedSerializer(TeagleSerializer):
	def _fill_doc(self, o, doc):
		if (isinstance(o, Testbed)):
			root_element = doc.createElement("testbed")
			self._add_text_node(root_element, "name", o.name, doc)
			self._add_text_node(root_element, "state", o.state.name, doc)
			components = doc.createElement("components")
			for i in o.instances:
				self._dump_resource(components, doc, i)
			root_element.appendChild(components)
			doc.appendChild(root_element)
			return root_element
		return super(TestbedSerializer, self)._fill_doc(o, doc)

	def _dump_resource(self, parent, doc, resource):
		elem = self._dump_resource_data(parent, doc, resource.specname, resource.identifier, resource.config)
		self._add_text_node(elem, "identifier", resource.identifier, doc)
		self._add_text_node(elem, "state", resource.state.name, doc)
		self._add_text_node(elem, "parent", resource.parent, doc)
		return elem
	
	def _unserialize_reference(self, id):
		return id and Identifier(id) or None

		
