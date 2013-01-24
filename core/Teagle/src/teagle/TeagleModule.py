'''
Created on 25.07.2011

@author: kca
'''

from . import get_teagle_home
from ngniutils import LoggerMixin
		
class TeagleModule(LoggerMixin):
	@classmethod 
	def _ensure_dir(cls, dir):
		if not dir.exists():
			cls.get_class_logger().debug("Creating directory %s", dir)
			dir.makedirs()
		return dir
	
	@classmethod
	def get_homedir(cls):
		dir = get_teagle_home() / cls.__name__
		return cls._ensure_dir(dir)
	
	@classmethod
	def get_datadir(cls):
		dir = cls.get_homedir() / "var"
		return cls._ensure_dir(dir)
	
	@classmethod
	def get_resourcedir(cls):
		dir = cls.get_homedir() / "res"
		return cls._ensure_dir(dir)
	
	def __get_resourcedir(self):
		return self.get_home_dir()
	resourcedir = property(__get_resourcedir)
	
	def __get_homedir(self):
		return self.get_home_dir()
	homedir = property(__get_homedir)
	
	def __get_datadir(self):
		return self.get_datadir()
	datadir = property(__get_datadir)
	