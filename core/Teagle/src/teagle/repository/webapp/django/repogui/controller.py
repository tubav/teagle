'''
Created on 01.09.2011

@author: kca
'''
from ngniutils.net.django.AbstractController import AbstractController
from teagle.repository.webapp.django.context import ContextFactory
from django.conf.urls.defaults import patterns, url
from django.http import Http404
from django.template.loader import get_template
from django.template.context import RequestContext
from django.shortcuts import redirect

class RepoGuiController(AbstractController):
	def __init__(self, name = "repogui", *args, **kw):
		super(RepoGuiController, self).__init__(name = name, *args, **kw)
		self.context_factory = ContextFactory()
	
	def _handle_request(self, request, method = None, *args, **kw):	
		if not method:
			return redirect("/%s/list?entity_type=ResourceSpec" % (self.name, ))
		if method.startswith("_"):
			raise Http404()
		
		methodname = method.replace("-", "_")
		try:
			factory = getattr(self.context_factory, methodname)
		except AttributeError:
			raise Http404()

		context = RequestContext(request, factory(request))
		template = get_template("repogui-" + methodname + ".html")
		
		return template.render(context)
	
	@property
	def urlpatterns(self):
		return patterns('', url(r'^%s/(.+?)(?:/(.*))?$' % (self.name, ), self, name = self.name),
					url('^$', self))
	