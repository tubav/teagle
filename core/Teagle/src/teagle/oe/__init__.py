from OrchestrationResult import OrchestrationResult
from ResultSerializer import ResultSerializer

from abc import ABCMeta, abstractmethod

class OEInterface(object):
	__metaclass__ = ABCMeta
	
	@abstractmethod
	def orchestrate(self, xml):
		raise NotImplementedError()