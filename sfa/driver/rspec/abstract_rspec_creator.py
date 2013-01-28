'''
Created on 27.01.2012

@author: sha
'''
from abc import ABCMeta, abstractmethod
from teagle.repository.tssg.TSSGRepository import TSSGRepository

class AbstractRspecCreator(object):
	__metaclass__ = ABCMeta

	@abstractmethod
	def create_rspec(self, slice_xrn = None, options={}):
		raise NotImplementedError()	
	
	@property
	def repo(self):
		try:
			return self.__repo
		except AttributeError:
			TEAGLE_REPOSITORY_URI = "http://root:r00t@193.175.132.168:8080/repository/rest"
			self.__repo =  TSSGRepository(uri = TEAGLE_REPOSITORY_URI)
			return self.__repo
