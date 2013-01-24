'''
Created on 26.07.2011

@author: kca
'''

from teagle.TeagleModule import TeagleModule
from teagle.repository.entities import Ptm
from teagle.repository.exc import NoEntityFound
from teagle.t1.T1Client import T1Client
from teaglegw.exc import UnknownPTM
from soap import T1SoapClient

class TGWT1Client(T1Client):
	def _format_error(self, opname, r):
		return "Error in %s: %s" % (opname, r.message)


class TeagleGW(TeagleModule):
	__ptms = {}
	
	def __init__(self, repo, certfile = None, keyfile = None, *args, **kw):
		super(TeagleGW, self).__init__(*args, **kw)
		
		if isinstance(repo, basestring):
			from teagle.repository import make_repoclient
			repo = make_repoclient(repo)
			
		self.__repo = repo
		self.certfile = certfile
		self.keyfile = keyfile
		
	def add_resource(self, parent, typename, config):
		ptm = self.get_ptm(parent)
		return ptm.add_resource(parent, typename, config)
	
	def get_resource(self, identifier):
		ptm = self.get_ptm(identifier)
		return ptm.get_resource(identifier)
	
	def list_resources(self, parent, typename = None):
		ptm = self.get_ptm(parent)
		return ptm.list_resources(parent, typename)
	
	def update_resource(self, identifier, config):
		ptm = self.get_ptm(identifier)
		return ptm.update_resource(identifier, config)
	
	def delete_resource(self, identifier):
		ptm = self.get_ptm(identifier)
		ptm.delete_resource(identifier)
	
	def execute_method(self, identifier, name, config):
		ptm = self.get_ptm(identifier)
		return ptm.execute_method(identifier, name, config)
	
	
	def get_ptm(self, ptmname):

		try:
			ptmname = ptmname.prefix

		except AttributeError:
			pass
		
		try:
			return self.__ptms[ptmname]
		except KeyError:
			try:
				ptm = self.__repo.get_unique_entity(Ptm, commonName = ptmname)
			except NoEntityFound:
				raise UnknownPTM(ptmname)
			if ptm.url.startswith("soap+"):
				ptm = T1SoapClient(ptm.url[5:], ptmname, certfile = self.certfile, keyfile = self.keyfile)
			else:
				ptm = TGWT1Client(ptm.url, ptmname, certfile = self.certfile, keyfile = self.keyfile)
			self.__ptms[ptmname] = ptm
			return ptm

	