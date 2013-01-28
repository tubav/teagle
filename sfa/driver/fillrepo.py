#!/usr/bin/env python

from teagle.repository.tssg.TSSGRepository import TSSGRepository
from teagle.repository.entities_default import ResourceSpec, ResourceInstance, Configlet, ConfigParamComposite, ConfigParamAtomic

def main():
	TEAGLE_REPOSITORY_URI = "http://root:r00t@localhost:8080/repository/rest"
	repo =  TSSGRepository(uri = TEAGLE_REPOSITORY_URI)
	
	#create PNode resource type
	pnodeconfigParams = []	
	pnodeconfigParam1 = ConfigParamAtomic(commonName = 'cpu', description = 'number of CPUs', configParamType = 'integer', defaultParamValue = '1')
	pnodeconfigParams.append(pnodeconfigParam1)
	pnodeconfigParam2 = ConfigParamAtomic(commonName = 'memory', description = 'amount of memory', configParamType = 'integer', defaultParamValue = '128')
	pnodeconfigParams.append(pnodeconfigParam2)
	pnodeconfigParamComposite = ConfigParamComposite(configParams = pnodeconfigParams)
	
	pnoderesourceSpec = ResourceSpec(commonName = 'pnode', description = 'This is a physical node', configurationParameters = pnodeconfigParamComposite)
	repo.persist(pnoderesourceSpec)
		
	#create VNode resource type
	vnodeconfigParams = []
	vnodeconfigParam1 = ConfigParamAtomic(commonName = 'cpu', description = 'number of CPUs', configParamType = 'integer', defaultParamValue = '1')
	vnodeconfigParams.append(vnodeconfigParam1)
	vnodeconfigParam2 = ConfigParamAtomic(commonName = 'memory', description = 'amount of memory', configParamType = 'integer', defaultParamValue = '128')
	vnodeconfigParams.append(vnodeconfigParam2)
	vnodeconfigParamComposite = ConfigParamComposite(configParams = vnodeconfigParams)
	
	vnoderesourceSpec = ResourceSpec(commonName = 'vnode', description = 'This is a virtual node', configurationParameters = vnodeconfigParamComposite)
	repo.persist(vnoderesourceSpec)
		
	#create PNode resource instance
	rtype = repo.get_unique_entity(ResourceSpec, commonName = "pnode")
	commonName = 'testptm./pnode-0'
	state = repo.RESOURCE_INSTANCE_STATE_PROVISIONED
	
	config = {}
	config['cpu'] = str(2)
	config['memory'] = str(256)
	
	configLets = []
	configParamAtList = rtype.configurationParameters.configParams
	
	for configParamAtomic in configParamAtList:
		newCL = Configlet(configParamAtomic=configParamAtomic)
		newCL.commonName = configParamAtomic.commonName
		newCL.description = configParamAtomic.description
		try:
				newCL.paramValue = unicode(config[newCL.commonName])
		except KeyError:
				newCL.paramValue = configParamAtomic.defaultParamValue
		configLets.append(newCL)
	
	instance = ResourceInstance(resourceSpec=rtype, configurationData=configLets, parentInstance=None , state=state, commonName = commonName)
	repo.persist(instance)
	
if __name__=="__main__":
	main()