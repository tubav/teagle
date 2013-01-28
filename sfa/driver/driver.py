'''
Created on 13 Jan 2012

@author: gca
'''
#import datetime
from time import strftime
from sfa.managers.driver import Driver
from teaglesfa.rspec.rspec_creator import RspecCreator
from teaglesfa.rspec.rspec_parser import RspecParser
from teaglesfa.register import Register
from ngniutils.net import django
from teagle.repository.tssg.TSSGRepository import TSSGRepository
from teagle.repository.Entity import Entity, Password
from teagle.repository.entities_default import Person, ResourceSpec, PersonRole,\
	Organisation, ResourceInstance, Configlet, Vct	

from sfa.util.sfalogging import logger
from teagle.repository.exc import NoEntityFound
from teagle.rp.client import LegacyRequestProcessorClient
from sfa.rspecs.rspec import RSpec
from teagle import Identifier
from sfa.util.xrn import hrn_to_urn

class TeagleDriver(Driver):
	def __init__(self, config, repourl = "http://130.149.80.14:8080/repository/rest", rp = "http://130.149.80.14:8000/reqproc/"):
		self.repo = TSSGRepository(repourl)
		self.__rp = LegacyRequestProcessorClient(rp)
	
	def list_slices (self, creds, options):
		vct_urns = []
		try:
			vcts = self.repo.list_entities(Vct)
			#logger.debug("***********************found VCTs: %s " % vcts)
			for vct in vcts:
				logger.debug("***********************VCT commonName: %s  | urn: %s" %(vct.commonName, hrn_to_urn(vct.commonName,'slice')))
				vct_urns.append(hrn_to_urn(vct.commonName,'slice'))
		except NoEntityFound:
			logger.error("***********************No VCTs found")
		logger.debug("***********************VCT_urns: %s" % vct_urns)
		return vct_urns
	
	def list_resources (self, creds, options):
		logger.debug("=======================driver.list_resources called")
		rc = RspecCreator()
		return rc.create_rspec(options)
	
	def register(self, sfa_record, hrn, pub_key):
		logger.debug("***********************register() called")
		register_type = sfa_record['type']
		if register_type == "slice":
			r = Register(self.repo)
			r.register_slice(sfa_record, hrn, pub_key)
	
	def _get_sfa_vct(self, slice_hrn):
		vct_name = slice_hrn.rpartition('.')[2]
		try:
			return self.repo.get_unique_entity(Vct, commonName = vct_name)
		except NoEntityFound:
			owner = self.repo.get_entity(Person, 1)
			vct = Vct(commonName = vct_name, shared = True, user = owner, state = self.repo.VCT_STATE_BOOKED)
			self.repo.persist(vct)
			logger.debug('=======================VCT %s created'%vct_name)
			return vct
			
	
	def create_sliver (self, slice_urn, slice_hrn, creds, rspec_string, users, options):
		
		logger.debug("=======================slice_urn: %s" %(slice_urn, ))
		logger.debug("=======================slice_hrn: %s" %(slice_hrn, ))
		#logger.error("***creds %s" %(creds, ))
		logger.debug("=======================rspec_string: %s" %(rspec_string, ))
		#logger.error("***users %s" %(users, ))
		logger.debug("=======================options: %s" %(options, ))
				
		rp = RspecParser()
		infos = rp.parse_rspec(rspec_string)

		vct = self._get_sfa_vct(slice_hrn)	
		
		for info in infos:
			rtype = info.rtype
			hostname = info.hostname
			parent = info.parent
			#config = parent.get_configuration()
			logger.info("=======================resource %s and  parent %s" %(rtype,parent))
			
			#create new instance of type rtype
			#now = datetime.datetime.now()
			now = strftime("%Y%m%dT%H%M%S")
			#also insert ptm in common name
			#ptm = Identifier(hostname).prefix
			commonName = ("%s-%s"%(rtype.commonName,now))
			
			
			#add configuration to instance (for now default values for parent parameters)
			configLets = []
			configParamAtList = rtype.configurationParameters.configParams
			logger.debug("=======================configParamAtList: %s " % configParamAtList)

			for configParamAtomic in configParamAtList:
				newCL = Configlet(configParamAtomic=configParamAtomic)
				newCL.commonName = configParamAtomic.commonName
				newCL.description = configParamAtomic.description
				newCL.paramValue = configParamAtomic.defaultParamValue
				logger.debug("=======================setting param %s with value %s " %(newCL.commonName, newCL.paramValue))
				configLets.append(newCL)

			#state = self.repo.RESOURCE_INSTANCE_STATE_PROVISIONED
			state = self.repo.RESOURCE_INSTANCE_STATE_UNPROVISIONED
			instance = ResourceInstance(resourceSpec=rtype, configurationData=configLets, parentInstance=None, state=state, commonName = commonName)
			logger.debug("=======================Created a resource instance %s " % instance)
			vct.providesResources.append(instance)
			logger.debug("=======================Resource instance appended to vct %s " % vct)
			
			#fake IP value setting until values are not persisted in the repo from OE
			for configlet in instance.configurationData:
				logger.debug("***********************configlet: %s" % (configlet, ))
				if configlet.configParamAtomic.commonName == "ip":
					#logger.debug("***********************ip found")
					configlet.paramValue = "192.168.144.42"
					#logger.debug("***********************ip set")
			
			#test if parent instance is already in vct, if not: add it
			#if parent in vct.providesResources:
			#	logger.debug("=======================Parent instance %s already in vct %s " % (parent,vct))
			#else:
			#	vct.providesResources.append(parent)
			#	logger.debug("=======================Parent instance %s appended to vct %s " % (parent,vct))
			
		self.repo.persist(vct)
		logger.debug("=======================Persisted vct %s " % vct)
		result = self.__rp.book(vct)
		#self.__rp.start(vct)
		logger.debug("=======================Booked vct %s " % vct)
		logger.debug("=======================result: %s " % result)
		
		
		
		options['geni_slice_urn'] = slice_urn
		logger.debug("=======================geni_slice_urn for RspecCreator: %s " % slice_urn)
		rc = RspecCreator()
		return rc.create_rspec(options)
	
	def start_slice (self, slice_urn, slice_hrn, creds):
		logger.debug("***********************start_slice() called")
		logger.debug("***********************slice_hrn: %s " % slice_hrn)
		vct_name = slice_hrn.rpartition('.')[2]
		try:
			vct = self.repo.get_unique_entity(Vct, commonName = vct_name)
			self.__rp.start(vct)
			logger.debug("***********************started VCT: %s " % vct_name)
			return 1
		except NoEntityFound:
			logger.error("***********************No VCT found to start: %s " % vct_name)
			return 0

	def stop_slice (self, slice_urn, slice_hrn, creds):
		logger.debug("***********************stop_slice() called")
		logger.debug("***********************slice_urn: %s " % slice_urn)
		vct_name = slice_hrn.rpartition('.')[2]
		try:
			vct = self.repo.get_unique_entity(Vct, commonName = vct_name)
			self.__rp.stop(vct)
			logger.debug("***********************stopped VCT: %s " % vct_name)
			return 1
		except NoEntityFound:
			logger.error("***********************No VCT found to stop: %s " % vct_name)
			return 0

def main():
	logger.info("Main method started")
	d = TeagleDriver("http://root:r00t@teagle.av.tu-berlin.de:8080/repository/rest")
	
	rspec_string = """\
<?xml version="1.0"?>
<RSpec type="SFA">
  <network name="teagle">
	<site id="s1">
	  <name>Teagle Site 1</name>
	  <node id="n1">
		<hostname>testptm./pnode-0</hostname>
		<cpu>2</cpu>
		<memory>256</memory>
		<sliver />
	  </node>
	</site>
  </network>
</RSpec>"""
	d.create_sliver(rspec_string)
	
if __name__ == "__main__":
	main()
