'''
Created on 01.09.2011

@author: kca
'''

from cms.plugin_base import CMSPluginBase
from cms.plugin_pool import plugin_pool
from cms.models import CMSPlugin
from cms.plugins.text.models import Text
from cms.plugins.text.forms import TextForm
from models import ChildrenTeaser
from django.db.models import Q;
from cms.utils.moderator import get_cmsplugin_queryset
from copy import copy
from django.utils.translation import ugettext as _

class PlainTextPlugin(CMSPluginBase):
	model = Text
	name = _("PlainText")
	form = TextForm
	render_template = "cms/plugins/text.html"

	def render(self, context, instance, placeholder):
		context.update({
			'body': instance.body, 
			'placeholder': placeholder,
			'object': instance
		})
		return context
	
	def save_model(self, request, obj, form, change):
		obj.clean_plugins()
		super(PlainTextPlugin, self).save_model(request, obj, form, change)

plugin_pool.register_plugin(PlainTextPlugin)

class ChildrenTeaserPlugin(CMSPluginBase):
	model = ChildrenTeaser
	name = _("ChildrenTeaser")
	render_template = "plugins/childteaser.html"
	admin_preview = False
	
	def render(self, context, instance, placeholder):
		assert(instance is not None and instance.placeholder_name)
		
		target_page = instance.target_page or context["current_page"]
		child_pages = target_page.get_children()
		
		if instance.max_items:
			child_pages = child_pages[:instance.max_items]
						
		plugins = []
		for page in child_pages:
			plugins += get_cmsplugin_queryset(context["request"]).filter(
				Q(placeholder__page = page), placeholder__slot__iexact= instance.placeholder_name).order_by('position').select_related()
		
		child_outputs = [ (page, plugin.get_plugin_instance()[0].render_plugin(copy(context), instance.placeholder_name)) for page, plugin in zip (child_pages, plugins) ] 
		
		context.update({"child_outputs": child_outputs})
		
		return context
	
plugin_pool.register_plugin(ChildrenTeaserPlugin)
