'''
Created on 21.07.2011

@author: kca
'''

from xml.dom.minidom import getDOMImplementation, parseString, Element, parse, Text
import logging
from ptm import Resource, Identifier
from ptm.exc import IllegalInputError
from GlobalIdentifier import GlobalIdentifier
from cStringIO import StringIO
from ngniutils.logging import LoggerMixin
logger = logging.getLogger("ptm")

class BaseSerializer(LoggerMixin):
	dom_implementation = getDOMImplementation()

	def __init__(self, prefix, *args, **kw):
		super(BaseSerializer, self).__init__(*args, **kw)
		
		if prefix:
			if GlobalIdentifier.PREFIX_ILLEGAL in prefix or GlobalIdentifier.PREFIX_SEPARATOR in prefix:
				raise ValueError("Illegal prefix: %s" % prefix)
			prefix = GlobalIdentifier.PREFIX_PREFIX + prefix + GlobalIdentifier.PREFIX_SEPARATOR
		self.__prefix = prefix

	@property
	def prefix(self):
		return self.__prefix

	@property
	def ptm_name(self):
		return self.prefix[:-1]

	def unserialize(self, rfile):
		if isinstance(rfile, basestring):
			doc = parseString(rfile)
		else:
			doc = parse(rfile)

		return self._unserialize_doc(doc)

	def __find_config_elem(self, root):
			for e in root.childNodes:
				if isinstance(e, Element) and e.tagName.lower() == "configuration":
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
		if not isinstance(child, Text):
			if type == "reference":
				return self._unserialize_reference_data(child)
			raise IllegalInputError("Illegal element in XML: %s" % (child.toxml(), ))
		return self._unserialize_value(type, child.data)

	def _unserialize_reference_data(self, child):
		config = self.__find_config_elem(child)
		for e in config.childNodes:
			if isinstance(e, Element) and e.tagName.lower() == "identifier":
				id = e.firstChild
				if id is None or not isinstance(id, Text) or not id.data:
					raise ValueError("Illegal identifier in reference data: %s", child.toxml())
				return self._unserialize_reference(id.data)
		raise ValueError("No identifier found in reference data: %s" % ( child.toxml()))

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

	def _unserialize_reference(self, id):
		raise NotImplementedError()

	def serialize(self, resource, wfile):
		doc = self._get_resource_doc(resource)
		try:
			doc.writexml(wfile, encoding = "utf-8")
		finally:
			doc.unlink()
			
	def dumps(self, resource):
		doc = self._get_resource_doc(resource)
		try:
			return doc.toxml()
		finally:
			doc.unlink()
		
	def _get_resource_doc(self, resource):
		doc = self.dom_implementation.createDocument(None, None, None)
		if isinstance(resource, Resource):
			self._dump_resource_data(doc, resource.typename, resource.identifier, resource.get_configuration())
		else:
			doc.appendChild(self.__serialize(resource, doc))
		return doc
				
	def _dump_resource_data(self, doc, typename, identifier, config, action = None, owners = None):
		# create root node with typename
		root_element = doc.createElement(typename)
		top_element = doc.createElement("configuration")
		root_element.appendChild(top_element)
		if action:
			root_element.setAttribute("action", action)
		# create first child with actual identifier
		if identifier is not None:
			element = doc.createElement("identifier")
			id = self.prefix and self.__prefix + unicode(identifier) or unicode(identifier)
			element.appendChild(doc.createTextNode(id))
			element.setAttribute("type", "string")
			top_element.appendChild(element)

		for a, value in config.iteritems():
			top_element.appendChild(self.__serialize(value, doc, a))

		if owners is not None:
			owners_element = doc.createElement("owners")
			owners_element.appendChild(self.__serialize(owners, doc, "owners"))
			root_element.appendChild(owners_element)
		
		doc.appendChild(root_element)
	
	def assist_serialize(self, typename, identifier, config, wfile = None, action = None, owners = None):
		buf = None
		if wfile is None:
			buf = wfile = StringIO()

		doc = self.dom_implementation.createDocument(None, None, None)
		try:
			self._dump_resource_data(doc, typename, identifier, config, action, owners)
			doc.writexml(wfile, encoding = "utf-8")
		finally:
			doc.unlink()

		if buf is not None:
			return buf.getvalue()
	
	def __get_type_attribute(self, v):
		if v is None:
			return "object"
		if isinstance(v, (Resource, Identifier)) or v.__class__.__name__.endswith("T1Entity"):
			return "reference"
		if isinstance(v, bool):
			return "boolean"
		if isinstance(v, (int, long)):
			return "integer"
		if isinstance(v, float):
			return "float"
		if isinstance(v, (str, unicode)):
			return "string"
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
		elif v is not None:
			top_element.appendChild(self._serialize_value(v, doc))

		return top_element

	def _serialize_value(self, v, doc):
		if isinstance(v, Resource) or v.__class__.__name__.endswith("T1Entity"):
			v = Identifier(v.identifier)
		if isinstance(v, Identifier) and self.__prefix:
			v = self.__prefix + unicode(v)
		return doc.createTextNode(unicode(v))
