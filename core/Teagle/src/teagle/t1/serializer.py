'''
Created on 24.09.2011

@author: kca
'''
from ngniutils.serializer.xml import AbstractXMLSerializer
from ngniutils.etree.impl import Element, SubElement, tostring
from ngniutils.logging.logbook import Logbook
from teagle.exc import IllegalInput
from teagle.t1 import ProvisioningResult

class ResultSerializer(AbstractXMLSerializer):
	def serialize_result(self, result):
		root = Element("return")
		self._fill_doc(root, result)
		return tostring(root, pretty_print = True)
	
	def _fill_doc(self, root, result):
		SubElement(root, "status").text = str(result.status)
		SubElement(root, "message").text = result.message
		self._drop_log(root, result.log)
	
	def _drop_log(self, node, logbook):
		logbookelem = SubElement(node, "logbook")
		if logbook is not None:
			SubElement(logbookelem, "name").text = logbook.name
			SubElement(logbookelem, "component").text = logbook.component
			entries = SubElement(logbookelem, "entries")
			for entry in logbook.entries:
				if isinstance(entry, Logbook):
					self._drop_log(entries, entry)
				else:
					SubElement(entries, "logentry").text = entry
					
	def _parse_input(self, root):
		if root.tag != "return":
			IllegalInput("Start tag must be 'return'. Is %s" % (root.tag, ))
			
		try:
			status = int(root.findtext("status"))
		except (ValueError, TypeError):
			raise IllegalInput("Illegal status in OrchestrationResult: %s" % (tostring(root), ))
		
		message = root.findtext("message")
		
		logbookelem = root.find("logbook")
		if logbookelem is not None and len(logbookelem):
			entries = logbookelem.find("entries") or ()
			log = Logbook(
						name = logbookelem.findtext("name", "<unknown>"),
						component = logbookelem.findtext("component", "<unknown>"),
						entries = [ entry.text for entry in entries ]
			)
		else:
			log = None
			
		return ProvisioningResult(status = status, message = message, log = log)
	
	def _dump_object(self, *args, **kw):
		raise NotImplementedError()
	
	