'''
Created on 26.07.2011

@author: kca
'''

from teagle.t1.django import AbstractT1Controller
from django.conf.urls.defaults import patterns, url
from ngniutils.net.httplib.exc import HTTPError406
from teaglegw.fokus.TeagleGW import TeagleGW
from django.conf import settings

class TGWLegacyController(AbstractT1Controller):
	def __init__(self, name = "teaglegw", *args, **kw):
		certfile = getattr(settings, "TGW_CERT_FILE", None)
		keyfile = getattr(settings, "TGW_KEY_FILE", None)
		repo = getattr(settings, "TEAGLE_REPO", "http://localhost:8080/repository/rest")
		self.logger.info("Using %s as Repo.", repo)
		self.logger.debug("Creating TGW with certfile=%s keyfile=%s", certfile, keyfile)
		t1 = TeagleGW(repo, certfile = certfile, keyfile = keyfile)
		super(TGWLegacyController, self).__init__(name = name, t1 = t1, *args, **kw)
		
	def _handle_request(self, request, ptm, path):
		return super(TGWLegacyController, self)._handle_request(request, (ptm, path))
	
	@property
	def urlpatterns(self):
		return patterns('', url(r'^%s/(.+?)/(.*?)$' % (self.name, ), self))
	
	def _mangle_path(self, request, path, default_prefix = None):
		id = super(TGWLegacyController, self)._mangle_path(request, path[1], path[0])
		if id and id.prefix != path[0]:
			raise HTTPError406("Prefix mismatch: %s != %s" % (id.prefix, path[0]))
		return id
