'''
Created on 25.07.2011

@author: kca
'''

from .util import get_storage_dir
from ngniutils import LoggerMixin
		
class PTMModule(LoggerMixin):
	@classmethod
	def get_datadir(cls):
		dir = get_storage_dir() / cls.__name__
		if not dir.exists():
			dir.makedirs()
		return dir
	
	def __get_datadir(self):
		return self.get_datadir()
	datadir = property(__get_datadir)