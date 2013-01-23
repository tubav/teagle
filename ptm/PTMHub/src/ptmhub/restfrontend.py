#! /usr/bin/env python


from ptm import Identifier
from ptm.exc import IllegalInputError
from ptm.exc import LookupError, NoAdapterFoundError, IdentifierException, InstanceNotFound
from FrontendModule import T1FrontendModule
from ngniutils import encstr
from collections import namedtuple
from ngniutils.etree.impl import Element, SubElement, tostring
from ngniutils.serializer.xml import AbstractXMLSerializer
from ngniutils.logging.logbook import Logbook

class ProvisioningResult(namedtuple("ProvisioningResult", ("status", "message", "log"))):
	def __new__(cls, status, message, log = None, *args, **kw):
		return super(ProvisioningResult, cls).__new__(cls, status = int(status), message = message, log = log)

	@property
	def successful(self):
		return self.status == 0

class ResultSerializer(AbstractXMLSerializer):
	def _dump_object(*args, **kw):
		pass

	def serialize_result(self, result):
		root = Element("return")
		
		SubElement(root, "status").text = str(result.status)
		SubElement(root, "message").text = result.message
		self._drop_log(root, result.log)
		
		return tostring(root, pretty_print = True)
	
	def _drop_log(self, node, logbook):
		logbookelem = SubElement(node, "logbook")
		if logbook is not None:
			SubElement(logbookelem, "name").text = logbook.name
			SubElement(logbookelem, "component").text = logbook.component
			entries = SubElement(logbookelem, "entries")
			for entry in logbook.entries:
				if isinstance(entry, Logbook):
					self._drop_log(entries, entry)
				else:
					SubElement(entries, "logentry").text = entry
					
	def _parse_input(self, *args, **kw):
		raise NotImplementedError()

	def _dump_object(self, *args, **kw):
		raise NotImplementedError()

class HTTPError(Exception):
	def __init__(self, code, msg, *args, **kw):
		Exception.__init__(self, int(code), msg, *args, **kw)
	
	@property
	def code(self):
		return self.args[0]
	
	@property
	def msg(self):
		return self.args[1]

class RestFrontend(T1FrontendModule):
	def _get_identifier(self, identifier):
		self.logger.debug("_get_identifier: %s %s" % (identifier, self.prefix))
		if identifier == "/" + self.prefix + ".":
			identifier = "/"
		elif identifier.startswith("/"):
			pspos = identifier.find("./")
			if pspos >= 0 and pspos <= identifier[1:].find("/"):
				identifier = identifier[1:]
		return super(RestFrontend, self)._get_identifier(identifier)

	def do_GET(self, path, rfile, headers):
		try:
			identifier = self._get_identifier(path)
			self.logger.debug("GET %s" % identifier)
			if identifier.is_adapter:
				resources = self.client.list_resources(identifier, None)
				self.logger.debug("ents: " + str(resources))
				return self.serializer.dumps([ Identifier(u.identifier) for u in resources ])
				
			return self._get_resource(identifier)	
		except (NoAdapterFoundError, InstanceNotFound),  e:
			raise HTTPError(404, str(e))
		except IdentifierException, e:
			self.logger.exception("Illegal Identifier: %s" % (path, ))
			raise HTTPError(406, str(e))
		except Exception, e:
			self.logger.exception("failed to get resource")
			raise HTTPError(500, repr(e))

	def do_PUT(self, path, rfile, headers):
		cl = headers["CONTENT_LENGTH"]
		request = rfile.read(int(cl))
		self.logger.debug("got request: %s\n%s" % (path, request))
		pathonly,sep,name = path.rpartition("#")
		try:
			typename, params, action = self.serializer.unserialize(request)
		except IllegalInputError, e:
			raise HTTPError(400, str(e))
		print "path %s and action %s with params %s"%(path,name,params,)
		params.pop("identifier", None)

		if sep:
			return self.execute_method_resource(pathonly, name, **params)
		return self.update_resource(path, params)

	def do_POST(self, path, rfile, headers):
		cl = headers["CONTENT_LENGTH"]
		request = rfile.read(int(cl))
		self.logger.debug("got request: %s\n%s" % (path, request))
		#path,_,action = path.rpartition("#")
		
		try:
			typename, config, action = self.serializer.unserialize(request)
		except IllegalInputError, e:
			raise HTTPError(400, str(e))
		
		try:
			#if action:
			#	return self.update_resource(path, config)
			#else:
			if path.endswith("/"):
				path = path[:-1]
			return self.add_resource(path, typename, config)
		except LookupError:
			self.logger.debug("Failed to resolve: " + path)
			raise HTTPError(404)
		except Exception, e:
			self.logger.exception("error during POST")
			raise HTTPError(500, str(e))
		
	def do_DELETE(self, path, rfile, headers):
		self.delete_resource(path)

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer

class RESTServer(SocketServer.ThreadingMixIn, HTTPServer, RestFrontend):
	class RequestHandler(BaseHTTPRequestHandler):
		def __getattr__(self, a):
			self.logger.debug("getattr: " + a)
			if not a.startswith("do_"):
				raise AttributeError(a)
			m = getattr(self.server, a)
			def handler():
				try:
					self.logger.debug("Executing")
					content = m(self.path, self.rfile, self.headers)
					self.logger.debug("Restfrontend response: %s", content)
					self.send_response(200)
					self.send_header("Content-Type", "application/xml")
					self.send_header("Content-Length", str(len(content)))
					self.end_headers()
					self.wfile.write(content)
				except HTTPError, e:
					msg = str(e) or None
					self.send_error(e.code, msg)
				except Exception, e:
					self.logger.exception("Error during request")
					self.send_error(500, str(e))

			return handler	

	def __init__(self, address, registry_url, prefix):
		HTTPServer.__init__(self, address, RESTServer.RequestHandler)
		RestFrontend.__init__(self, registry_url, prefix)

class RESTFrontendApplication(RestFrontend):
	error_serializer = ResultSerializer()
	
	def _handle_error(self, code, msg, e, environ):
		self.logger.exception("Error in RESTFrontend: %s" % (e, ))
		resp = "%s %s" % (code, msg,)
		if "html" in environ.get("HTTP_ACCEPT", ""):
			content = "<html><head><title>%s</title></head><body><h1>%s</h1></body></html>" % (resp, repr(e), )
			ct = "text/html"
		else:
			content = self.error_serializer.serialize_result(ProvisioningResult(code, msg))
			ct = "text/xml"
		return resp, content, ct

	
	def __call__(self, environ, start_response):
		rfile = environ["wsgi.input"]
		method = environ["REQUEST_METHOD"]
		path = environ["PATH_INFO"]
		
		f = getattr(self, "do_" + method)
		try:
			content = f(path, rfile, environ)
			resp = "200 OK"
			ct = "application/xml"
		except HTTPError, e:
			resp, content, ct = self._handle_error(e.code, e.msg, e, environ)
		except Exception, e:
			resp, content, ct = self._handle_error(500, str(e), e, environ)
		
		headers = [ ("Content-Type", ct), ("Content-Length", str(len(content))) ]
		self.logger.debug("Returning response: %s %s %s %s" % (headers, resp, content, type(content)))
		start_response(resp, headers)
		return [ encstr(content) ]
	
def main():
	import sys
	"""
	console = logging.StreamHandler()
	formatter = logging.Formatter('REST: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
	console.setFormatter(formatter)
	console.setLevel(logging.DEBUG)
	self.logger.setLevel(logging.DEBUG)
	self.logger.addHandler(console)
"""
	if len(sys.argv) > 1:
		port = int(sys.argv[1])
	else:
		port = 8001

	#self.logger.debug("Starting")
	r = RESTServer(("0.0.0.0", port), "http://127.0.0.1:8000", "test")
	r.serve_forever()
	return 0

if __name__ == "__main__":
	import sys
	sys.exit(main())
