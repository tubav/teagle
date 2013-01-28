'''
Created on 21.07.2011

@author: kca
'''

import ngniutils
from ngniutils import Base
from urlparse import urlparse
from datetime import datetime
from httplib import HTTPConnection, HTTPSConnection
from ..httplib import HTTPResponseWrapper
from ngniutils.contextlib import closing
from cStringIO import StringIO
from exc import HTTPError, NetworkError
from socket import getservbyname

if ngniutils.THREADSAFE:
	"".encode("ascii")


class CachingHttplibResponseWrapper(Base):
	def __init__(self, response, path, tag, last_modified, cache, *args, **kw):
		super(CachingHttplibResponseWrapper, self).__init__(*args, **kw)
		self.__cache = cache
		self.__buffer = StringIO()
		self.__path = path
		self.__tag = tag
		self.__response = response
		self.__last_modified = last_modified
		self.__finalized = False
	
	def read(self, n = None):
		s = self.__response.read(n)
		self.__buffer.write(s)
		return s
	
	def readline(self):
		s = self.__response.readline()
		self.__buffer.write(s)
		return s
	
	def readlines(self, sizehint = None):
		lines = self.__response.readlines(sizehint)
		self.__buffer.write(''.join(lines))
		return lines
	
	def close(self):
		if self.__finalized:
			self.logger.warning("%s is already finalized" % (self, ))
			return 
		
		self.__finalized = True
		try:
			if not self.__response.isclosed():
				self.__buffer.write(self.__response.read())
			val = self.__buffer.getvalue()
			self.logger.debug("Putting to cache: %s -> %s, %s\n %s" % (self.__path, self.__tag, self.__last_modified, val))
			self.__cache[self.__path] = (self.__tag, self.__last_modified, val)
		except:
			self.logger.exception("Finalizing response failed")
		finally:
			self.__response.close()
				
		self.__buffer.close()

class HTTPErrorResponse(HTTPResponseWrapper, HTTPError):
	__httperrorbase__ = True

	def __init__(self, status, connection, response, msg = None, *args, **kw):
		super(HTTPErrorResponse, self).__init__(status = status, reason = response.reason, msg = msg, connection = connection, response = response, *args, **kw)
	
class RestClient(Base):
	def __init__(self, uri, username = None, password = None, content_type = "text/plain", headers = {}, cache = True, keepalive = True, certfile = None, keyfile = None, *args, **kw):			 
		super(RestClient, self).__init__(*args, **kw)
		self.logger.debug("Creating RESTClient for %s", uri)

		if cache is True:
			from ngniutils.caching import LRUCache
			cache = LRUCache()
		elif cache == False:
			cache = None
		self.__cache = cache
		self.__uri = uri
		self.__content_type = content_type
		
		if not "://" in uri:
			uri = "http://" + uri
		
		info = urlparse(uri)
		
		scheme = info.scheme.rsplit("+", 1)[-1] or "http"
		if scheme == "https":
			self._get_connection = self._get_secure_connection
		elif scheme != "http":
			raise ValueError("Unsupported URL scheme: %s", info.scheme)
		
		if not info.hostname:
			raise ValueError("Illegal URL: %s" % (uri, ))
		
		if bool(keyfile) ^ bool(certfile):
			raise ValueError("Both certfile and keyfile must be given if any.")
		
		self.__host = info.hostname
		self.__port = info.port and int(info.port) or getservbyname(scheme)
		self.__base = info.path and info.path.rstrip("/") or ""
		self.__certfile = certfile
		self.__keyfile = keyfile

		headers = headers.copy()			
		headers.setdefault("Accept", "*/*")
		headers["Accept-Encoding"] = "identity" #TODO: support compression
		
		if keepalive:
			headers.setdefault("Connection", "Keep-Alive")
		
		username = username or info.username
		if username:
			password = password or info.password or ""
			import base64
			headers["Authorization"] = "Basic " + base64.b64encode("%s:%s" % (username, password))
			
		self.__headers = headers
		
	@property
	def uri(self):
		return self.__uri
		
	def _get_connection(self):
		return HTTPConnection(self.__host, self.__port)
	
	def _get_secure_connection(self):
		#self.logger.debug("Opening secure connection to %s:%s with certfile=%s keyfile=%s", self.__host, self.__port, self.__certfile, self.__keyfile)
		return HTTPSConnection(self.__host, self.__port, self.__keyfile, self.__certfile)
			
	def request(self, method, path, data = None, headers = None):
		if not path.startswith("/"):
			path = "/" + path   
		if isinstance(data, unicode):
			data = data.encode("utf-8")
		fullpath = self.__base + path
		request_headers = self.__headers.copy()
		
		if headers:
			request_headers.update(headers)

		if self.__cache is not None and method == "GET":
			try:
				etag, modified, cached = self.__cache[fullpath]
				if etag:
					request_headers["If-None-Match"] = etag
				request_headers["If-Modified-Since"] = modified
			except KeyError:
				request_headers.pop("If-None-Match", None)
				request_headers.pop("If-Modified-Since", None)
		else:
			request_headers.setdefault("Content-Type", self.__content_type)
		
		if method in ("GET", "DELETE"):
			self.logger.debug("%s: %s (%s)" % (method, fullpath, request_headers))
		else:
			self.logger.debug("%s: %s (%s)\n%s" % (method, fullpath, request_headers, data))
		
		connection = self._get_connection()
#		print ("Requesting: %s:%s%s" % (self.__host, self.__port, fullpath))
		try:
			connection.request(method, fullpath, data, request_headers)
#			print ("request out")
			response = connection.getresponse()
		except Exception, e:
			self.logger.exception("Error during request")
			connection.close()
			if not str(e) or str(e) == "''":
				e = repr(e)
			raise NetworkError("An error occurred while contacting %s:%s: %s. Request was: %s %s" % (self.__host, self.__port, e, method, fullpath))
		
		self.logger.debug("%s %s result: %s" % (method, fullpath, response.status, ))
		if response.status == 304:
			try:
				self.logger.debug("Using cached answer for %s (%s, %s):\n %s" % (fullpath, etag, modified, cached))
				return closing(StringIO(cached))
			except NameError:
				raise NetworkError("Error: %s:%s returned 304 though no cached version is available. Request was: %s %s" % (self.__host, self.__port, e, method, fullpath))
		if response.status >= 300 and response.status < 400:
			raise NotImplementedError("HTTP redirect %s" % (response.status, ))
		if response.status < 200 or response.status >= 300:
			msg = "HTTP Error. %s:%s said: %s %s. Request was: %s %s" % (self.__host, self.__port, response.status, response.reason, method, fullpath)
			self.logger.exception(msg)
			raise HTTPErrorResponse(response.status, connection = connection, response = response, msg = msg)  
		
		if method == "DELETE" and self.__cache:
			self.__cache.pop(fullpath, None)
		else:
			etag = response.getheader("Etag")
			modified = response.getheader("Last-Modified")
			if etag or modified:
				modified = modified or datetime.utcnow().strftime("%a, %d %b %Y %X GMT")
				response = CachingHttplibResponseWrapper(response, fullpath, etag, modified, self.__cache)

		return HTTPResponseWrapper(connection, response)

	def get(self, path, headers = None):
		return self.request("GET", path, headers = headers)

	def post(self, path, data, headers = None):
		return self.request("POST", path, data, headers)
	add = post

	def put(self, path, data, headers = None):
		return self.request("PUT", path, data)
	update = put
		
	def delete(self, path, headers = None):
		self.request("DELETE", path, None, headers)
