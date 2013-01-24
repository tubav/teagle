'''
Created on 23.07.2011

@author: kca
'''

from ngniutils.net.django.ParameterBasedController import ParameterBasedController
from teagleoe.fokus.TeagleOE import TeagleOE
from teagle.oe import ResultSerializer
from django.conf import settings
from ngniutils.net.django.AbstractController import AbstractController
from django.conf.urls.defaults import patterns, url



class LegacyOEController(ParameterBasedController):
	def __init__(self, name = "teagleoe", *args, **kw):
		super(LegacyOEController, self).__init__(name = name, *args, **kw)
		tgwurl = getattr(settings, "TGW_URL", "http://localhost:8000/teaglegw")
		self.logger.info("Using %s as TGW URL.", tgwurl)
		self.__oe = TeagleOE(tgwurl)
		self.__serializer = ResultSerializer()
		
	def putVCTSpec(self, request):
		vctid = self._get_param("v_vctid")
		self.__oe.put_vct_spec(vctid, request.FILES["v_vctfile"])
		#raise Exception(result, xml)
		return '<html><head><title>Orchestration Result</title></head><body><textarea name="result">OK</textarea></body></html>'
	
	def deployVCT(self, request):
		return "OK"
	
	def orchestrate(self, request):	
		vctid = self._get_param("serviceid")
		result = self.__oe.orchestrate_legacy(vctid)
		return self.__serializer.serialize_result(result)
	
class OEController(AbstractController):
	def __init__(self, name = "teagleoe", *args, **kw):
		super(OEController, self).__init__(name = name, *args, **kw)
		tgwurl = getattr(settings, "TGW_URL", "http://localhost:8000/teaglegw")
		self.logger.info("Using %s as TGW URL.", tgwurl)
		self.__oe = TeagleOE(tgwurl)
		self.__serializer = ResultSerializer()
	
	def orchestrate(self, request, operation):
		result = self.__oe.orchestrate(request, operation)
		return self.__serializer.serialize_result(result)
	
	_handle_request = orchestrate

	@property
	def urlpatterns(self):
		return patterns('', url(r'^%s/(.*?)(?:/.*?)?$' % (self.name, ), self, name = self.name))
	