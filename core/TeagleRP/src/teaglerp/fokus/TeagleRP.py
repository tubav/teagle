'''
Created on 28.08.2011

@author: kca
'''

from teagle.TeagleModule import TeagleModule
from teagle.repository.entities import Vct, Person, ResourceInstance, Ptm
from ngniutils import uc
from ngniutils.exc import raise_error, errorstr
from teaglerp.serializer import LegacyTestbedSerializer
from teagle.repository.exc import NoEntityFound
from ngniutils.logging.logtap import BufferingLogTap
from teagle.oe.OrchestrationResult import OrchestrationResult
from ngniutils.logging.logbook import Logbook
from teaglerp.exc import NoPtmFound

class TeagleRP(TeagleModule):
	def __init__(self, repo, oe, *args, **kw):
		super(TeagleRP, self).__init__(*args, **kw)
		
		self.serializer = LegacyTestbedSerializer()
		
		if isinstance(repo, basestring):
			from teagle.repository import make_repoclient
			repo = make_repoclient(repo)
			
		if isinstance(oe, basestring):
			#from teagleoe.django.teagleoe.controller import OEController
			#oe = OEController()
			from teagle.oe.simple import SimpleOEClient
			oe = SimpleOEClient(oe)
			
		self.repo = repo
		self.oe = oe
		
	def _find_ptm(self, rs):
		ptms = self.repo.list_entities(Ptm, owns = {"resourceSpecs": rs} )
		if not ptms:
			raise NoPtmFound("No PTM found where type %s can be instantiated" % (rs.commonName, ))
		return ptms[0]
		
	def _prep_vct(self, vct):
		for i in vct.instances:
			if not i.provisioned and not "." in i.commonName:
				self.logger.info("PTM missing for instance %s" % (i.commonName, ))
				ptm = self._find_ptm(i.resourceSpec)
				self.logger.info("Chose PTM %s for instance %s" % (ptm.name, i.commonName))
				i.commonName = ptm.name + "." + i.commonName
				
		
	def book(self, vct):
		return self.handle_request(vct.user.userName, vct.commonName, vct)
			
			

		
		
	def handle_request(self, user, vct,operation):
		opname = "operation %s with request: vct=%s user=%s" % (operation, vct, user)
		try:
			with BufferingLogTap(name = "Request Processor") as tap:
				self.repo.refresh()
				username = user and uc(user) or raise_error(ValueError("No user given"))
				vctname = vct and uc(vct) or raise_error(ValueError("No vct name given"))
					
				self.logger.debug("Processing %s", opname)
				
				user = self.repo.get_unique_entity(Person, userName = username)
				vct = self.repo.get_unique_entity(Vct, commonName = vctname, user = user)
				
				self._prep_vct(vct)
				
				xml = self.serializer.serialize(vct)
				
				result = self.oe.orchestrate(xml,operation)
				tap.emit(result.log)
							
				if result.successful:
					self.logger.info("Orchestration succeeded")
					vct.state = self.repo.VCT_STATE_BOOKED
					updated = set([vct])
					instances = {}
					
					for i in vct.instances:
						instances[i.identifier] = i
						for c in i.get_reference_configurations():
							if c.is_array or c.is_dict:
								raise NotImplementedError()
							try:
								c.paramValue = result.idmapping[c.paramValue]
							except KeyError:
								pass
							else:
								updated.add(c)
							
					replace_instances = False
					for designid, runtimeid in result.idmapping.iteritems():
						if designid != runtimeid:
							design_instance = self.repo.get_unique_entity(ResourceInstance, commonName = designid)
							try:
								existing = self.repo.get_unique_entity(ResourceInstance, commonName = runtimeid)
							except NoEntityFound:
								self.logger.debug("Renaming instance %s: %s => %s" % (repr(design_instance), design_instance.commonName, runtimeid))
								design_instance.commonName = runtimeid
								design_instance.state = self.repo.RESOURCE_INSTANCE_STATE_PROVISIONED
								updated.add(design_instance)
							else:
								existing.copy_config(instances[designid]) 
								del instances[designid]
								instances[runtimeid] = existing
								replace_instances = True
								updated.add(existing)
												
					if replace_instances:
						vct.providesResources = instances.values()
								
					self.repo.persist(updated)
				else:
					self.logger.info("Orchestration not successful.")
		except Exception, e:
			return OrchestrationResult(2, errorstr(e), log = Logbook(opname, "Request Processor", tap.log))

		return OrchestrationResult(*result[:4], log = Logbook(opname, "Request Processor", tap.log))
		