'''
Created on 03.09.2011

@author: kca
'''
from teagle.repository.webapp.django.repoactions.controller import RepoActionsController
from teagle.repository.entities import Ptm
from teagle.oe.legacy import LocalOEClient
from teagle import is_testrun

class LegacyActionsController(RepoActionsController):
	try:
		oeclient = LocalOEClient()
	except:
		if not is_testrun():
			raise
	
	def _make_entity(self, klass, values):
		e = super(LegacyActionsController, self)._make_entity(klass, values)
		if isinstance(e, Ptm) and not is_testrun():
			self.oeclient.register_ptm(e)
		return e