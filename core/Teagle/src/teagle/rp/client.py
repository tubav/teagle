'''
Created on 31.01.2012

@author: kca
'''

from ngniutils.logging import LoggerMixin
from serializer import LegacyRPRequestSerializer
from abc import ABCMeta, abstractmethod
from ngniutils.net.httplib.RestClient import RestClient
from teagle.rp.serializer import LegagyRPRequest

class AbstractRequestProcessorClient(LoggerMixin):
	__metaclass__ = ABCMeta
	
	@abstractmethod
	def book_vct(self, vct):
		raise NotImplementedError()
	
	def book(self, vct):
		return self.book_vct(vct.user.userName, vct.commonName)
	
	def start(self, vct):
		return self.start_vct(vct.user.userName, vct.commonName)
	
	def stop(self, vct):
                return self.stop_vct(vct.user.userName, vct.commonName)

class LegacyRequestProcessorClient(AbstractRequestProcessorClient):
	def __init__(self, url, *args, **kw):
		super(LegacyRequestProcessorClient, self).__init__(*args, **kw)
		self.__rp = RestClient(url, cache = False)
		self.serializer = LegacyRPRequestSerializer()
	
	def book_vct(self, user, vct):
		request = LegagyRPRequest("booking", "setVct", user, vct)
		with self.__rp.post("/", self.serializer.dumps(request)) as result:
			return result.read()
		
	def start_vct(self, user, vct):
		request = LegagyRPRequest("booking", "startVct", user, vct)
		with self.__rp.post("/", self.serializer.dumps(request)) as result:
			return result.read()
	
	def stop_vct(self, user, vct):
                request = LegagyRPRequest("booking", "stopVct", user, vct)
                with self.__rp.post("/", self.serializer.dumps(request)) as result:
                        return result.read()
