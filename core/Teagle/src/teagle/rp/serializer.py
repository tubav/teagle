'''
Created on 31.01.2012

@author: kca
'''
from collections import namedtuple
from ngniutils.serializer.xml import AbstractXMLSerializer
from ngniutils.etree.impl import Element, SubElement
from teagle.exc import IllegalInput
from operator import attrgetter
from ngniutils.exc import errorstr

LegagyRPRequest = namedtuple("LegacyRPRequest", ("target", "operation", "user", "vct"))

class LegacyRPRequestSerializer(AbstractXMLSerializer):
	def _handle_parse_error(self, e):
		super(LegacyRPRequestSerializer, self)._handle_parse_error(e)
		raise IllegalInput("Error parsing RP request: " + errorstr(e))
		
	def _parse_input(self, root):	
		data = map(attrgetter("text"), root[:4])
		if len(data) < 4 or not all(data):
			raise IllegalInput("Missing field in input (%s)" % (data, ))
		
		return LegagyRPRequest(*data)
	
	def _dump_object(self, o):
		array = Element("string-array")
		SubElement(array, "string").text = o.target or "booking"
		SubElement(array, "string").text = o.operation or "book_vct"
		SubElement(array, "string").text = o.user
		SubElement(array, "string").text = o.vct
		return array
