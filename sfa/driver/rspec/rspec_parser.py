'''
Created on 27.01.2012

@author: sha	
'''
from teaglesfa.rspec.abstract_rspec_parser import AbstractRspecParser
from teagle.repository.entities import ResourceSpec, ResourceInstance
#from teaglesfa.rspec.exc import UnknownRspecId
from sfa.rspecs.rspec import RSpec
from sfa.rspecs.version_manager import VersionManager 
#from sfa.util.sfalogging import logger
from ngniutils.etree import ElementTree
from teagle.base import Identifier

from collections import namedtuple
from teagle.repository.exc import NoEntityFound, MultipleEntitiesFound
from teaglesfa.rspec.exc import NoPnodeError, RSpecError,\
	NoInstanceFound, MultibleInstancesFound
from sfa.util.sfalogging import logger
from sfa.rspecs.rspec_elements import RSpecElements 

Info = namedtuple("Info", ("rtype", "hostname", "parent"))

class RspecParser(AbstractRspecParser):
	
	def convert_tags(self, tags):
		tags_dict = {}
		for tag in tags:
			tags_dict[tag['tagname']] = tag['value'] 
		return tags_dict
	
	def parse_rspec(self, rspec_string):
		
#		aggregate = PlAggregate(self)
#		slices = PlSlices(self)
#		peer = slices.get_peer(slice_hrn)
#		sfa_peer = slices.get_sfa_peer(slice_hrn)
#		slice_record=None	
#		if users:
#			slice_record = users[0].get('slice_record', {})
				
		# parse rspec
		result = []
		rspec = RSpec(rspec_string)
		#logger.error("rspec: %s" %(rspec, ))
		#requested_attributes = rspec.version.get_slice_attributes()
		#logger.error("requested_attributes: %s" %(requested_attributes, ))
		
		nodes = rspec.version.get_nodes();
		logger.debug("nodes: %s" %(nodes, ))
		
		for node in nodes:
			#logger.error('tagname: ' + str(tag['tagname'], ) + ' | value: ' + str(tag['value'], ))
#			component_id = ''
#			component_id = node['component_id']
			
			#logger.error(component_id)
			
			#for tag in [ t for t in node['tags'] if 'hostname' == str(t['tagname']) ]:
			#	hostname = str(tag['value'], )
			tags = self.convert_tags(node['tags'])
			#check if node has to be slivered
			if 'sliver' in tags:
				#logger.error('tags: ' + str(tags))
				try:
					hostname = tags['hostname'].rpartition('./')[2]#filter out ptm name
					#hostname = tags['hostname']#changed for OpenLabReview
					parent = self.repo.get_unique_entity(ResourceSpec, commonName = hostname)
				except NoEntityFound:
					raise NoInstanceFound()
				except MultipleEntitiesFound():
					raise MultibleInstancesFound()
				
				#logger.error(parent.config)
				
				#for now every time the same resource type. later mapping between p and v types needed, or name convention
				#rtype = self.repo.get_unique_entity(ResourceSpec, commonName = "openims")
				
				result.append(Info(rtype = parent, hostname = hostname, parent = parent))
			else:
				logger.debug('no sliver tag')
		print result 
		return result
#				config = parent.config
#				for c in parent.config:
#					try:
#						config[c] = tags[c]
#					except KeyError:
#						logger.error('No parameter ' + str(c) + ' found in RSpec, using default value')
				
				#logger.error(config)
			
			
			

#			rtype = self.repo.get_unique_entity(ResourceSpec, commonName = "VNode")#because a sliver of a PNode is always a VNode
#			
#			declaration = rtype.get_config_declaration()
#
#			logger.error('declaration: ' + str(declaration))
#
#			config = {}
#					for child in node.getchildren():
#				try:
#					need_type = declaration[child.tag]
#				except KeyError:
#					print "No such key: %s" % (child.tag, )
#					pass
#				else:
#					value = child.text
		
#		# ensure site record exists
#		site = slices.verify_site(slice_hrn, slice_record, peer, sfa_peer, options=options)
#		# ensure slice record exists
#		slice = slices.verify_slice(slice_hrn, slice_record, peer, sfa_peer, options=options)
#		# ensure person records exists
#		persons = slices.verify_persons(slice_hrn, slice, users, peer, sfa_peer, options=options)
#		# ensure slice attributes exists
#		slices.verify_slice_attributes(slice, requested_attributes, options=options)
		
		
		
		#nodes auseinandernehmen und jeweils in teagle buchen
		#erforderliche information in myplcdb eintragen
		
		
		
#		from StringIO import StringIO
#		infile = StringIO(rspec_string)
#		
#		tree = ElementTree.parse(infile)
#		#root = tree.getroot()
#		
#		result = []
#		
#		for node in tree.getiterator("node"):
#			hostname = node.findtext("hostname")
#			try:
#				identifier = Identifier(hostname)
#			except IdentifierError:
#				raise UnknownIdentifier()
#			if identifier.typename.lower() != "pnode":	
#				raise NoPnodeError()
#			rtype = self.repo.get_unique_entity(ResourceSpec, commonName = "VNode")#because a sliver of a PNode is always a VNode
#			parent = self.repo.get_unique_entity(ResourceInstance, commonName = identifier.local_identifier)
#			
#			declaration = rtype.get_config_declaration()
#			
#			# {"mem": "int", "hostname": "string"}
#			
#			config = {}
#			for child in node.getchildren():
#				try:
#					need_type = declaration[child.tag]
#				except KeyError:
#					print "No such key: %s" % (child.tag, )
#					pass
#				else:
#					value = child.text
#					
#					def convert_value(value_to_convert, type_to_convert_to):
#						type_to_convert_to = type_to_convert_to == "string" and "str" or type_to_convert_to
#						type_to_convert_to = type_to_convert_to == "integer" and "int" or type_to_convert_to
#						
#						import __builtin__
#						try:
#							t = getattr(__builtin__, type_to_convert_to)
#						except AttributeError:
#							raise RSpecError("No such type: %s" % (type_to_convert_to, ))
#						
#						return t(value_to_convert)
#					
#					value = convert_value(value_to_convert = value, type_to_convert_to = need_type)
#					config[child.tag] = value
#			result.append(Info(rtype = rtype, parent = parent, config = config))
#		#print result 
#		return result


def main():
	rp = RspecParser()
	rspec_string = """\
<?xml version="1.0"?>
<RSpec type="SFA" expires="2012-04-04T13:16:42Z" generated="2012-04-04T12:16:42Z">
  <network name="teagle">
    <node component_manager_id="teagle" component_id="testptm./pnode-0" component_name="/pnode-0">
      <hostname>SFAPNode-instance</hostname>
      <memory>256</memory>
      <sliver/>
    </node>
  </network>
</RSpec>"""
#	rspec_string = """\
#<?xml version="1.0"?>
#<RSpec type="SFA" expires="2012-03-21T11:26:54Z" generated="2012-03-21T10:26:54Z">
#	<statistics call="ListResources">
#		<aggregate status="success" name="plc" elapsed="0.231594085693"/>
#	</statistics>
#	<network name="plc">
#		<node component_manager_id="urn:publicid:IDN+plc+authority+cm" component_id="urn:publicid:IDN+plc:ft+node+radiax.fokus.fraunhofer.de" boot_state="reinstall" component_name="radiax.fokus.fraunhofer.de" site_id="urn:publicid:IDN+plc:ft+authority+sa">
#			<hostname>testptm./pnode-0</hostname>
#			<interface component_id="urn:publicid:IDN+plc+interface+node1:eth0" ipv4="192.168.144.15"/>
#			<hrn>planetlab.test.ft.radiax</hrn>
#			<cpu>2</cpu>
#			<memory>256</memory>
#			<sliver/>
#		</node>
#	</network>
#</RSpec>"""
	rp.parse_rspec(rspec_string)

if __name__ == "__main__":
	main()
