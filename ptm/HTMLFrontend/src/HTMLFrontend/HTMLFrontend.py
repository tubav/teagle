#!/usr/bin/env python
import sys

print sys.path

from os import path
from werkzeug import Request, Response, redirect 
from ngniutils.exc import errorstr
#from werkzeug.routing import Map, Rule
from werkzeug.exceptions import HTTPException
from mako.lookup import TemplateLookup
from ptm.exc import NoAdapterFoundError, IdentifierException
from ptm.t1client import T1Client, T1Entity
from ptm.t1client.GlobalIdentifier import GlobalIdentifier
from ngniutils.logging import LoggerMixin
from cgi import escape
#root_path = path.abspath(path.dirname(__file__))
types = ("string", "integer", "float", "boolean", "reference")
#t1C = T1Client("10.147.67.94", "8001")
t1C = None
update = 0

def parse_value(type, v):
	if type not in types:
		raise Exception("Unknown Type: %s" % (type))
	if type != "string" and not v:
		v = None
	elif type == "boolean":
		v = v.lower().startswith("t")
	elif type == "integer":
		v = int(v)
	elif type == "float":
		v = float(v)
	elif type == "reference":
		v = t1C.get_entity(v)
	return v

class ParamChecker(object):
	def __init__(self, *args, **kw):
		super(ParamChecker, self).__init__(*args, **kw)
		self.__seen = []
	def doparam(self, name, type, value):
		err = None
		
		if not name:
			err = "name is empty"
		elif name in self.__seen:
			err = "duplicate name"
		else:
			self.__seen.append(name)
			try:
				value = parse_value(type, value)
			except Exception, e:
				err = str(e)
		
		return (name, type, value, err)

import string
letters = string.letters + "_"
legal = letters + string.digits

def check_typename(typename):
	if not typename:
		return "no typename given"
	if typename[0] not in letters:
		return "typename must start with letter or underscore"
	for c in typename:
		if c not in legal:
			return "Illegal character in typename: %s" % (c, )
	return None

class HTMLFrontend(LoggerMixin):
	def __init__(self, rest_url = None, homedir = None, webcontext = '/', prefix = None, *args, **kw):
		super(HTMLFrontend, self).__init__(*args, **kw)
		self.webcontext = webcontext
		
		if rest_url:
			global t1C
			t1C = T1Client(rest_url, prefix = prefix)
			
		if not homedir:
			import ptm.util
			homedir = path.join(ptm.util.get_ptm_home(), "lib/HTMLFrontend")
			
		self.template_lookup = TemplateLookup(directories=[path.join(homedir, 'templates')], input_encoding='utf-8')

	def __make_url(self, url):
		if not url.startswith("/") and not url.startswith(self.prefix + GlobalIdentifier.PREFIX_SEPARATOR):
			return url
		if not url.startswith("/"):
			url = "/" + url
		return self.webcontext + url
		
	@property
	def prefix(self):
		return t1C.prefix
	
	def _get_id_from_path(self, req_url):
		if req_url.startswith("/"):
			pspos = req_url.find("./")
			if pspos >= 0 and pspos <= req_url[1:].find("/"):
				req_url = req_url[1:]
		return req_url

	@Request.application
	def __call__(self, request):
		try:
	
			response = Response(mimetype='text/html')
			req_url = request.path
			msg = ""
	
			if request.method == "POST":
				if req_url.startswith("/add"):
					goterror = False
					id = self._get_id_from_path(req_url[4:])
					params = []
					config = {}
					form = request.form				
					
					typename = form.get("typename", '')
					typeerror = check_typename(typename)
					if typeerror:
						goterror = True
	
					n = 0
					checker = ParamChecker()
					while True:
						k = "param%d_" % (n, )
						nk = k + "name" 
						vk = k + "value"
						if nk not in form and vk not in form:
							break
						
						name = form.get(nk, '')
						value = form.get(vk, '')
						
						if name or value:		
							type = form.get(k + "type", 'string')
							params.append(checker.doparam(name, type, value))
							if params[-1][-1]:
								goterror = True
							else:
								config[name] = params[-1][2]
						n += 1
					if goterror:
						msg = "Illegal input"
					else:
						try:
							t1C.add(id, config.get("name", None), typename, config)
							return redirect(id)
						except Exception, e:
							self.logger.exception("error")
							msg = "An error occured: %s" % (e, )
				else:
					req_url = self._get_id_from_path(req_url)
					#raise Exception(req_url)
					entity = t1C.get_entity(req_url)
		
					old = entity.config
					config = {}
		
					for k, v in request.form.iteritems():
						print "%s - %s" % (k, v)
						if "-" not in k:
							continue
		
						type, _, name = k.partition("-")
						if type == "boolean":
							v = v.lower().startswith("t")
						elif type == "integer":
							v = int(v)
						elif type == "float":
							v = float(v)
						elif type == "reference":
							raise NotImplementedError()
						if name not in old or old[name] != v:
							config[name] = v
		
					if config:
						entity.update(config)
						msg = "Update successful"
					else:
						msg = "No values changed, nothing to do"
	
			if req_url.startswith("/add/"):
				id = req_url[5:]
				if request.method != "POST":
					params = [ ('', '', '', None) for _ in range(10) ]
					typename = ''
					typeerror = None
				t_test = self.template_lookup.get_template("add.html")
				response.data = t_test.render_unicode(url = self.__make_url, request = request, params = params, id = id, typename = typename, types = types, typeerror = typeerror, msg = escape(msg), log = ())
			elif req_url.startswith("/del/"):
				id = req_url[5:]
				t1C.delete(id)
				parent = id.rpartition("/")
	
				return redirect(parent[0] + parent[1])
			else:
				if req_url.startswith("/show"):
					req_url = req_url[5:]
				if req_url.startswith("/"):
					pspos = req_url.find("./")
					if pspos >= 0 and pspos <= req_url[1:].find("/"):
						req_url = req_url[1:]
						#raise Exception("huhu", req_url)	
				elif not req_url:
					req_url = "/"
				if req_url.endswith("/"):
					try:
						self.logger.debug("listing: " + req_url)
						instances = t1C.list_entities(req_url)
					except NoAdapterFoundError:
						instances = False 
					except Exception, e:
						instances = escape(errorstr(e))
					template = self.template_lookup.get_template("list.html")
					add_url = not req_url.startswith("/") and req_url or req_url[1:]
					response.data = template.render_unicode(url = self.__make_url, request = request, list = instances, req_url = req_url, msg = escape(msg), add_url = add_url)
				else:
					template = self.template_lookup.get_template("instance.html")
					self.logger.debug("Getting: " + req_url)
					config = None
					try:
						entity = t1C.get_entity(req_url)
						config = entity.config
						config["identifier"] = entity.identifier
						if config["identifier"].startswith("/"):
							config["identifier"] = config["identifier"].partition("/")[2]
						for k, v in config.items():
							t = "string"
                                                        if "password" in k:
                                                                v = "*********"
                                                        if isinstance(v, bool):
                                                                t = "boolean"
                                                        elif isinstance(v, int):
                                                                t = "integer"
                                                        elif isinstance(v, float):
                                                                t = "float"
                                                        elif isinstance(v, T1Entity):
                                                                t = "reference"
							config[k] = (v, t)
						p_id = entity.parent_id
						#raise Exception(entity.identifier)
					except Exception, e:
						self.logger.exception("Error while rendering")
						entity = None
						msg = errorstr(e)
						try:
							p_id = GlobalIdentifier(req_url, default_prefix = self.prefix).parent
						except IdentifierException:
							self.logger.exception("Error deriving parent url")
							p_id = GlobalIdentifier.SEPARATOR
					response.data = template.render_unicode(url = self.__make_url, request = request, p_id = p_id, id = req_url, entity = entity, config = config, req_url = req_url, msg = escape(msg))
	
			return response
		except HTTPException, e:
			self.logger.exception("Error while rendering")
			return e

def main():
	from wsgiref.simple_server import make_server
	s = make_server('', 8080, HTMLFrontend())
	s.serve_forever()

if __name__ == "__main__":
	main()
