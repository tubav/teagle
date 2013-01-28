'''
Created on 27.01.2012

@author: sha
'''
from abc import ABCMeta, abstractmethod
from teagle.repository.tssg.TSSGRepository import TSSGRepository

class AbstractRspecParser(object):
	__metaclass__ = ABCMeta
	
	@abstractmethod
	def parse_rspec(self, rspec):
		raise NotImplementedError()
	
	@property
	def repo(self):
		try:
			return self.__repo
		except AttributeError:
			TEAGLE_REPOSITORY_URI = "http://root:r00t@193.175.132.168:8080/repository/rest"
			self.__repo =  TSSGRepository(uri = TEAGLE_REPOSITORY_URI)
			return self.__repo
