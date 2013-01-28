'''
Created on 17.05.2012

@author: sha
'''
from teagle.repository.tssg.TSSGRepository import TSSGRepository
from teagle.repository.entities_default import Person, Vct	

class Register(object):
	def __init__(self, repo):
		self.repo = repo

	def register_slice(self, sfa_record, hrn, pub_key):
		owner = self.repo.get_entity(Person, 1)
		vct = Vct(commonName = hrn, shared = True, user = owner, state = self.repo.VCT_STATE_BOOKED)
		self.repo.persist(vct)
		return hrn