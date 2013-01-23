'''
Created on 23.08.2011

@author: kca
'''
from ptm.PTMModule import PTMModule
from ngniutils.net.httplib.server.wsgi.ssl import SecureThreadingWSGIServer
from restfrontend import RESTFrontendApplication
from ssl import CERT_REQUIRED
from ptmhub.soap import SOAPFrontend

class FrontendApplication(PTMModule):
	def __init__(self, huburl, prefix = None, *args, **kw):
		super(FrontendApplication, self).__init__(*args, **kw)
		
		from HTMLFrontend.HTMLFrontend import HTMLFrontend

		if not prefix:
			prefix = "test"
		
		self.__rest = RESTFrontendApplication(huburl = huburl, prefix = prefix)
		self.__html = HTMLFrontend(rest_url = huburl + "/rest", webcontext = "/html", prefix = prefix)
		self.__soap = SOAPFrontend(huburl, prefix = prefix)
	
	def __call__(self, environ, start_response): 
		method = environ["REQUEST_METHOD"]
		path = environ["PATH_INFO"]
		#print("processing request: %s" % (path, ))
		if method == "GET" and ("/.hg/" in path or path.endswith("/favicon.ico")):
			start_response("404 Not found", [("Content-Type", "text/plain"), ])
			return ()
		
		app = self._get_application(environ, method, path)
		
		if not app:
			start_response("404 Not found", [("Content-Type", "text/plain"), ])
			return ()
		
		print app
		return app(environ, start_response)
		
	def _get_application(self, environ, method, path):
		if method not in ("GET", "POST") or path.startswith("/rest"):
			if path.startswith("/rest"):
				environ["PATH_INFO"] = path[5:]
			return self.__rest
		
		if path.startswith("/html"):
			environ["PATH_INFO"]= path[5:]
			return self.__html
		
		if path.startswith("/soap"):
			environ["PATH_INFO"]= path[5:]
			return self.__soap
			
		if method == "GET":
			return self.__html

		return None
	
class SecureFrontendServer(SecureThreadingWSGIServer, PTMModule):
	def __init__(self, huburl, bind_address = None, port = None, prefix = None, certfile = None, keyfile = None, ca_certs = None, *args, **kw):
		port = port or 8443
		prefix = prefix or "test"
		bind_address = bind_address or "0.0.0.0"
			
		if not certfile:
			certfile = self.datadir / "ptmfrontendcert.pem"
			keyfile = self.datadir / "ptmfrontendkey.pem"
		ca_certs = ca_certs or self.datadir / "cacert.pem"
			
		application = FrontendApplication(huburl = huburl, prefix = prefix)
		SecureThreadingWSGIServer.__init__(self, (bind_address, port), app = application, certfile = certfile, keyfile = keyfile, ca_certs = ca_certs, cert_reqs = CERT_REQUIRED)

	def get_request(self):
		print("huhuh")
		s = SecureThreadingWSGIServer.get_request(self)
		print("huhuh")
		print s
		print s[0].getpeercert()
		return s

def main():
	import logging
	from optparse import OptionParser

	parser = OptionParser()
	parser.add_option("-x", "--prefix", dest="prefix", help="Set prefix for PTM", default="ptmtest")
	parser.add_option("-p", "--port", type="int", dest="port", help="Set port for PTM")
	parser.add_option("-H", "--hub", dest="huburl", help = "URL of the PTM Hub", default="http://localhost:8000")
	(options, _args) = parser.parse_args()
	
	logger = logging.getLogger("ptm")
	#console = logging.StreamHandler()
	#formatter = logging.Formatter('Manager: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
	#console.setFormatter(formatter)
	#console.setLevel(logging.DEBUG)
	#logger.setLevel(logging.DEBUG)
	#logger.addHandler(console)
	
	#raise Exception(options)

	#TestHubServer().serve_forever()
	SecureFrontendServer(options.huburl, "0.0.0.0", options.port, options.prefix).serve_forever()
	
if __name__ == "__main__":
	main()