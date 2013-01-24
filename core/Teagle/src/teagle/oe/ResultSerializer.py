'''
Created on 25.07.2011

@author: kca
'''

from ngniutils import tostr
from ngniutils.etree.impl import SubElement
from OrchestrationResult import OrchestrationResult
from teagle.exc import IllegalInput
from teagle.t1.serializer import ResultSerializer

class ResultSerializer(ResultSerializer):
	def _fill_doc(self, root, result):
		super(ResultSerializer, self)._fill_doc(root, result)
		SubElement(root, "log").text = result.log_url
				
		resultelem = SubElement(root, "result")
		idmapping = SubElement(resultelem, "idmapping")
		for designid, runtimeid in result.idmapping.iteritems():
			mapping = SubElement(idmapping, "mapping")
			mapping.set("designid", tostr(designid))
			mapping.set("runtimeid", tostr(runtimeid))
	
	def _parse_input(self, root):
		pr = super(ResultSerializer, self)._parse_input(root)
		
		result = root.find("result")
		if result is None:
			raise IllegalInput("Missing result tag")
		
		idmapping = result.find("idmapping")
		if idmapping is None:
			raise IllegalInput("Missing idmapping")
		
		mappings = {}
		for mapping in idmapping:
			mappings[mapping.attrib["designid"]] = mapping.attrib["runtimeid"]
		
		return OrchestrationResult(pr.status, pr.message, mappings, log = pr.log) 
		
		