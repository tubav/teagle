'''
Created on 27.01.2012

@author: sha	
'''
from teaglesfa.rspec.abstract_rspec_creator import AbstractRspecCreator
from teagle.repository.entities import ResourceSpec, ResourceInstance, Vct, Ptm
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
from sfa.util.xrn import Xrn


Info = namedtuple("Info", ("rtype", "parent", "config"))

class RspecCreator(AbstractRspecCreator):
	
	def create_rspec(self, options = None):
		logger.debug("=======================RspecCreator.create_rspec called")
		logger.debug("=======================options: %s" %(options, ))
		# get slice's hrn from options
		slice_xrn = options.get('geni_slice_urn', None)
		geni_rspec_version = options.get('geni_rspec_version', None)
		
		if not geni_rspec_version:
			geni_rspec_version = {'namespace': None, 'version': '1', 'type': 'SFA', 'extensions': [], 'schema': None}
		
		version_manager = VersionManager()
		version = version_manager.get_version(geni_rspec_version)

		if not slice_xrn:
			slice_hrn, slice_urn = None, None
			rspec_version = version_manager._get_version(version.type, version.version, 'ad')
			rspec = RSpec(version=rspec_version, user_options=options)
#			pnodespec = self.repo.get_unique_entity(ResourceSpec, commonName = "simpletestpnode")
#			instances = self.repo.list_entities(ResourceInstance, resourceSpec = pnodespec)

			ptms = [
				self.repo.get_unique_entity(Ptm, commonName = "fokusptm"),
				self.repo.get_unique_entity(Ptm, commonName = "upatrasptm")
				]
			
			allowed_resources = ["fokusopenims", "imscreateuseraccount", "imscoreaccess", "imsopensipsaccess", "imsmediaserveraccess", "imswebrtc2sipgw"]
			
			pnodespec = namedtuple("pnodespec", ("ptmname", "resourceSpec"))
			pnodespecs = []
			
			for ptm in ptms:
				for rs in ptm.resourceSpecs:
					if rs.commonName in allowed_resources:
#						print ptm.commonName + "./" + rs.commonName
						pnodespecs.append(pnodespec(ptmname = ptm.commonName, resourceSpec = rs))
			
#			pnodespec = [
#				#self.repo.get_unique_entity(ResourceSpec, commonName = "openims"),
#				self.repo.get_unique_entity(ResourceSpec, commonName = "fokusopenims"),
#				#self.repo.get_unique_entity(ResourceSpec, commonName = "vmadapter"),
#				#self.repo.get_unique_entity(ResourceSpec, commonName = "simpletestpnode"),
#				self.repo.get_unique_entity(ResourceSpec, commonName = "imscreateuseraccount"),
#				self.repo.get_unique_entity(ResourceSpec, commonName = "imscoreaccess"),
#				self.repo.get_unique_entity(ResourceSpec, commonName = "imsopensipsaccess"),
#				self.repo.get_unique_entity(ResourceSpec, commonName = "imsmediaserveraccess"),
#				self.repo.get_unique_entity(ResourceSpec, commonName = "imswebrtc2sipgw")
#				]
		

#			instances_tuples = []
#			for p in pnodespec:
#				instances_tuples.append(self.repo.list_entities(ResourceInstance, resourceSpec = p))
#		
#			instances = []
#			for i in instances_tuples:
#				for j in i:
#					instances.append(j)
			
			nodes = [
				{'component_manager_id': 'urn:publicid:IDN+raven+authority+cm',
				 'component_id': 'urn:publicid:IDN+raven:raven+node+' + pnodespec.ptmname + './' + pnodespec.resourceSpec.commonName,
				 'authority_id': 'urn:publicid:IDN+raven:raven+authority+sa',
				 'tags': [{'tagname': 'settings', 'value': [{'tagname': p.commonName, 'value': p.defaultParamValue, 'description': p.description} for p in pnodespec.resourceSpec.configurationParameters.configParams]}]
				 #'tags': [{'tagname': p.commonName, 'value': p.defaultParamValue} for p in pnode.resourceSpec.configurationParameters.configParams]
				} for pnodespec in pnodespecs
			]
			#print "len(instances): %s"% len(instances)
			
#			nodes = [
#				{'component_manager_id': 'teagle',
#				 'component_id': i.commonName,
#				 'tags': [{'tagname': p.commonName, 'value': ''} for p in i.resourceSpec.configurationParameters.configParams]
#				} for i in instances
#			]
			
#			rspec.version.add_nodes(nodes)
		else:
			xrn = Xrn(slice_xrn)
			slice_urn=xrn.get_urn()
			slice_hrn=xrn.get_hrn()
			rspec_version = version_manager._get_version(version.type, version.version, 'manifest')
			rspec = RSpec(version=rspec_version, user_options=options)
			vct_name = slice_hrn.rpartition('.')[2]
			nodes = []
			try:
				vct = self.repo.get_unique_entity(Vct, commonName = vct_name)
				logger.debug("=======================vct: %s" %(vct, ))
				instances = vct.providesResources
				logger.debug("=======================instances: %s" %(instances, ))
	
				#add login information
				nodes = [
					{'component_manager_id': 'urn:publicid:IDN+raven+authority+cm',
					 'component_id': 'urn:publicid:IDN+raven:raven+node+' + i.commonName,
					 'authority_id': 'urn:publicid:IDN+raven:raven+authority+sa',
					 'tags': [{'tagname': p.commonName, 'value': p.paramValue} for p in i.configurationData]
					} for i in instances
				]

			except NoEntityFound:
				logger.debug("=======================no vct %s found" %(vct_name, ))
	
		rspec.version.add_nodes(nodes)
				
		#logger.error("options1: %s" %(options, ))
		#if options is None:
		#	options = {}
		#logger.error("rspec_creator.create_rspec called")
		#logger.error("options2: %s" %(options, ))
		
		# get the rspec's return format from options
		
		#if not slice_xrn:
			
		#else:
			
		
		
		
		return rspec.toxml()
	
	def create_options(self):
		options = {}
		version_dict = {'type': 'SFA', 'version': '1', }
		#version_dict = {'type':'ProtoGENI', 'version':'2'}
		options ['geni_rspec_version'] = version_dict
		#options ['geni_slice_urn'] = 'asjdhf'
		
		return options

	
def main():
	rc = RspecCreator()
	print rc.create_rspec(rc.create_options())

if __name__ == "__main__":
	main()
