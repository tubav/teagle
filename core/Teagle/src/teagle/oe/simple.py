'''
Created on 29.08.2011

@author: kca 
'''

from . import OEInterface
from ngniutils.net.httplib.RestClient import RestClient
from teagle.oe.ResultSerializer import ResultSerializer

class SimpleOEClient(RestClient, OEInterface):
	def __init__(self, uri, username = None, password = None, certfile = None, keyfile = None, *args, **kw):			 
		super(SimpleOEClient, self).__init__(uri = uri, username = username, password = password, content_type = "text/plain", cache = False, certfile = certfile, keyfile = keyfile, *args, **kw)
		self.serializer = ResultSerializer()
		
	def orchestrate(self, xml, operation):
		with self.post("/%s"%(operation,), xml) as answer:
			return self.serializer.load(answer)