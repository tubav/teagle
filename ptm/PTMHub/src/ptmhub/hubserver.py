#! /usr/bin/env python
'''
Created on 11.08.2010

@author: kca
'''

from ptm.ManagerServer import BasicManagerWSGIApplication, BasicManagerServer
from PTMHub import PTMHub
from frontend import FrontendApplication

class HubWSGIApplication(BasicManagerWSGIApplication):
	def __init__(self, encoding = None, *args, **kw):
		manager = PTMHub()
		BasicManagerWSGIApplication.__init__(self, encoding = encoding, manager = manager, *args, **kw)

class TestHubServer(BasicManagerServer):
	def __init__(self, bind_address = None, port = None, *args, **kw):
		application = HubWSGIApplication()
		if port is None:
			port = 8000
		super(TestHubServer, self).__init__(application = application, bind_address = bind_address, port = port, *args, **kw)
		
class HubServerApplication(FrontendApplication):	
	def __init__(self, port, prefix = None, tgw_url = None, encoding = None, *args, **kw):
		super(HubServerApplication, self).__init__(huburl="http://localhost:%s" % (port, ), prefix = prefix, *args, **kw)
		
		self.__hub = HubWSGIApplication(encoding = encoding)
		
		if tgw_url:
			from ptmhub.ra.TGWAdapter import TGWAdapter
			TGWAdapter(self.__hub.manager, tgw_url, prefix = prefix)

	def _get_application(self, environ, method, path):
		app = super(HubServerApplication, self)._get_application(environ, method, path)
		return app or self.__hub
	
	@property
	def manager(self):
		return self.__hub.manager
	
class HubServer(BasicManagerServer):
	def __init__(self, bind_address = None, port = None, prefix = None, tgw_url = None, *args, **kw):
		port = port or 8000
		prefix = prefix or "test"
		application = HubServerApplication(port = port, prefix = prefix, tgw_url = tgw_url)
		super(HubServer, self).__init__(application = application, bind_address = bind_address, port = port, *args, **kw)
		
	@property
	def manager(self):
		return self.application.manager
		
def main():
	import sys
	import logging
	from optparse import OptionParser

	parser = OptionParser()
	parser.add_option("-x", "--prefix", dest="prefix", help="Set prefix for PTM", default="ptmtest")
	parser.add_option("-p", "--port", type="int", dest="port", help="Set port for PTM")
	parser.add_option("-g", "--tgw", dest="tgw_url", help = "URL of the Teagle gateway")
	(options, _args) = parser.parse_args()
	
	logger = logging.getLogger("ptm")
	console = logging.StreamHandler()
	formatter = logging.Formatter('Manager: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
	console.setFormatter(formatter)
	console.setLevel(logging.DEBUG)
	logger.setLevel(logging.DEBUG)
	logger.addHandler(console)
	
	#raise Exception(options)

	#TestHubServer().serve_forever()
	HubServer("0.0.0.0", options.port, options.prefix, options.tgw_url).serve_forever()
	sys.exit(0)
	
	
if __name__ == "__main__":
	main()
	