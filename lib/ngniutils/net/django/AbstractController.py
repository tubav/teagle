'''
Created on 22.07.2011

@author: kca
'''

from ngniutils import Base
from exc import MissingParameter
from threading import local
from abc import ABCMeta, abstractmethod
from django.conf.urls.defaults import patterns, url
from django.http import HttpResponse

class AbstractController(Base):
	__metaclass__ = ABCMeta
	
	mimetype = "text/html"
	
	def __init__(self, name, mimetype = None, *args, **kw):
		super(AbstractController, self).__init__(*args, **kw)
		self.__local = local()
		self.__name = name
		if mimetype:
			self.mimetype = mimetype
	
	def __call__(self, request, *args, **kw):		
		self.__local.request = request
		try:
			result = self._handle_request(request, *args, **kw)
		except:
			self.logger.exception("Error handling request")
			raise
		finally:
			del self.__local.request
			
		if not isinstance(result, HttpResponse):
			result = HttpResponse(result, mimetype = self.mimetype)
			
		return result
			
	@abstractmethod
	def _handle_request(self, request, *args, **kw):
		raise NotImplementedError()
			
	@property
	def _request(self):
		return self.__local.request
	
	def _get_param(self, name):
		try:
			return self._request.REQUEST[name]
		except KeyError:
			raise MissingParameter(name)
		
	@property
	def name(self):
		return self.__name
	
	@property
	def urlpatterns(self):
		return patterns('', url(r'^%s(?:/.*?)?$' % (self.name, ), self, name = self.name))
