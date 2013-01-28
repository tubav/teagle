from sqlalchemy.ext.declarative import declarative_base, declared_attr
from ngniutils.logging import get_logger

class DBMixin(object):
	@declared_attr
	def __tablename__(self):
		return self.__name__.lower()
	
	def get_logger(self):
		try:
			return self.__logger
		except AttributeError:
			self.__logger = get_logger(self)
			return self.__logger
	def set_logger(self, logger):
		self.__logger = logger
	logger = property(get_logger, set_logger)

DBObject = declarative_base(cls = DBMixin)