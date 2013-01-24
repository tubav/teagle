'''
Created on 28.08.2011

@author: kca
'''
from ngniutils.net.django.AbstractController import AbstractController
from django.conf import settings
from teaglerp.fokus.TeagleRP import TeagleRP
from teagle.oe.ResultSerializer import ResultSerializer
from teagle.rp.serializer import LegacyRPRequestSerializer

class LegacyRPController(AbstractController):
	def __init__(self, name = "reqproc", *args, **kw):
		super(LegacyRPController, self).__init__(name = name, *args, **kw)

		repo = getattr(settings, "TEAGLE_REPO", "http://localhost:8080/repository/rest")
		self.logger.info("Using %s as Repo.", repo)
		oe = getattr(settings, "TEAGLE_OE", "http://localhost:8000/teagleoe")
		tgwurl = getattr(settings, "TGW_URL", "http://localhost:8000/teaglegw")
		#oe = TeagleOE(tgwurl)

		self.logger.info("Using %s as OE.", oe)
		self.__rp = TeagleRP(repo, oe)
		self.serializer = LegacyRPRequestSerializer()
		self.result_serializer = ResultSerializer()
	
	def _handle_request(self, request, *args, **kw):
		rprequest = self.serializer.load(request)
		result = self.__rp.handle_request(rprequest.user, rprequest.vct,rprequest.operation)
		return self.result_serializer.serialize_result(result)