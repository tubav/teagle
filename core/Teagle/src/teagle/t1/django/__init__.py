'''
Created on 26.07.2011

@author: kca
'''

from ngniutils.net.django.RestController import RestController
from teagle import Identifier
from ..T1Serializer import T1Serializer
from ngniutils.exc import errorstr
from django.http import HttpResponse
from teagle.t1.serializer import ResultSerializer
from teagle.t1.exc import T1ErrorResponse
from urlparse import urlparse


class AbstractT1Controller(RestController):
	def __init__(self, name, t1, *args, **kw):
		super(AbstractT1Controller, self).__init__(name = name, *args, **kw)
		self.__t1 = t1
		self.__serializer = T1Serializer(t1)
		self.__result_serializer = ResultSerializer()
	
	def _handle_request(self, request, path):
		try:
			path = self._mangle_path(request, path)
			result = super(AbstractT1Controller, self)._handle_request(request, path)
			xml = self.__serializer.serialize(result)
		except Exception, e:
			for t in request.META.get("HTTP_ACCEPT", "").split(","):
				if "html" in t:
					raise
			self.logger.exception("Error serving T1 request")
			if isinstance(e, T1ErrorResponse):
				return HttpResponse(content = self.__result_serializer.serialize_result(e), status = 500, content_type = "text/xml")
			return HttpResponse(content = errorstr(e), status = 500, content_type = "text/plain")
		return xml
	
	def _mangle_path(self, request, path, default_prefix = None):
		self.logger.debug("Mangling path: %s" % (path, ))
		method = request.method.lower()
		if path.endswith("/") and method in ("get", "post"):
			path = path[:-1]
			request.is_list = False
		else:
			request.is_list = path.endswith("/")
			
		return path and Identifier(path, default_prefix = default_prefix) or None

	def _handle_get(self, request, identifier):
		#raise Exception("bliu", request.is_list)
		if request.is_list:
			return self.__t1.list_resources(identifier)
		else:
			return self.__t1.get_resource(identifier)
		
	def _handle_post(self, request, identifier, force_update = False):
		typename, config, action = self.__serializer.unserialize(request)
		if force_update or action == "update":
			return self.__t1.update_resource(identifier, config)
		else:
			return self.__t1.add_resource(identifier, typename, config)
		
	def _handle_put(self, request, identifier):
		print " identifier %s and path %s "%(identifier,request.path,)
		urlparsed = urlparse(request.path) 
		if urlparsed.fragment:
			typename, config, action = self.__serializer.unserialize(request);			
		
			identifier =Identifier(config['identifier'])
			methodname = urlparsed.fragment
			return self.__t1.execute_method(identifier, methodname, config)
		
		return self._handle_post(request, identifier, True)
	
	def _handle_delete(self, request, identifier):
		self.__t1.delete(identifier)
