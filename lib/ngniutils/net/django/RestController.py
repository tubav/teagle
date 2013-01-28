'''
Created on 22.07.2011

@author: kca
'''

from AbstractController import AbstractController
from django.conf.urls.defaults import patterns, url

class RestController(AbstractController):
	@property
	def urlpatterns(self):
		return patterns('', url(r'^%s(/.*?)/$' % (self.name, ), self))
	
	def _handle_request(self, request, path):
		try:
			f = getattr(self, "_handle_" + request.method.lower())
		except AttributeError:
			raise NotImplementedError("method %s is not supported" % (request.method, ))
		return self._execute_method(f, request, path)
		
	def _execute_method(self, f, request, path, *args, **kw):
		return f(request, path, *args, **kw)
	