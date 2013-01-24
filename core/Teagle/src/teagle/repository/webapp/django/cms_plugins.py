'''
Created on 01.09.2011

@author: kca
'''
from teagle.repository.webapp.django import get_repo, get_form_manager
from cms.plugin_base import CMSPluginBase
from cms.plugin_pool import plugin_pool
from cms.models import CMSPlugin
from cms.plugins.text.models import Text
from cms.plugins.text.forms import TextForm
from django.utils.translation import ugettext as _
from teagle.repository.entities import get_entity_classes
from teagle.repository import Password
from django.template.loader import get_template
from django.template import Context, RequestContext
from ngniutils.logging import LoggerMixin
from ngniutils.net.django.exc import MissingParameter
from teagle.repository.exc import UnknownEntityType
from teagle.repository.webapp.django.context import ContextFactory
from urllib2 import quote

class TeagleRepoPlugin(CMSPluginBase, LoggerMixin):
	model = CMSPlugin
	admin_preview = False
	context_factory = ContextFactory(viewurl = "./show/")
	
	def render(self, context, instance, placeholder):
		try:
			return self._render(context, instance, placeholder)
		except:
			self.logger.exception("Error in CMS plugin")
			raise
	
	def _render(self, context, instance, placeholder):
		raise NotImplementedError()
	
	@property
	def repo(self):
		return get_repo()
	
	@property
	def forms(self):
		return get_form_manager()

	def _get_param(self, context, name):
		try:
			return context["request"].REQUEST[name]
		except KeyError:
			raise MissingParameter(name)

	@staticmethod
	def _get_entity_class(name):
		classes = get_entity_classes()
		for c in classes:
			if c.__name__ == name:
				return c
		raise UnknownEntityType(name)
	
class EntityPlugin(TeagleRepoPlugin):
	model = Text
	form = TextForm	

class ListEntitiesPlugin(EntityPlugin):
	name = _("ListEntities")
	render_template = "plugins/list_entities.html"
	
	def _render(self, context, instance, placeholder):
		try:
			typename = self._get_param(context, "entity_type")
		except MissingParameter:
			if not (instance and instance.body):
				raise
			typename = instance.body
		
		context.update(self.context_factory.list(context["request"], typename))
		klass  =  self._get_entity_class(typename)
		helptext = "This is a list of %s. " % (klass.__display_typename__ + "s")
		helptext += klass.__helptext__ or ""
		helptext += "\nYou can click on a row to view further details."
		context["help_text"] = helptext.replace("\n", "<br/>")
		
		return context
	
plugin_pool.register_plugin(ListEntitiesPlugin)

from teagle.repository import Entity

class ShowEntityPlugin(EntityPlugin):
	""" CMSPlugin to present an Entity on the Portal.
	"""
	name = _("ShowEntity")
	render_template = "plugins/show_entity.html"
	
	def __render_list(self, entity, field, l, request, wrapper_for = None):
		template = get_template("plugins/list_details.html")
		#raise Exception(entity.describedByPtmInfo.supportedResources)
		assert entity.is_persistent
		
		add_to = "%s-%d.%s" % (entity.__class__.__name__, entity.id, field.name)
		if wrapper_for:
			add_to += ("." + wrapper_for)
		
		items = []
		for value in l:
			if isinstance(value, Entity):
				items.append((value, self.__render_short(value)))
			else:
				items.append((value, value))
				
		if wrapper_for:
			wrapper = getattr(entity, field.name)
			f = wrapper.get_field(wrapper_for)
		else:
			f = field
			
		can_edit = request.user.is_staff or request.user.teagleuser.can_edit(entity, field.name)
			
		if can_edit and (f.shared or f.type[0].__const__):
			assert f.type and isinstance(f.type[0], type) and issubclass(f.type[0], Entity), "shared not implemented for %s" % f.type
			selection = [ (e.id, e.commonName) for e in self.repo.list_entities(f.type[0]) if e not in l ]
		else:
			selection = None
			
		k = f.type
		if isinstance(k, (list, tuple, set, frozenset)):
			k = k[0]
		typename = k.__name__
		
#		if field.name == "personRoles":
#			raise Exception((not f.type[0].__const__) and (request.user.is_staff or request.user.teagleuser.can_edit(entity, field.name)))
				
		return template.render(RequestContext(request, {
								"items": items, 
								"add_to": add_to, 
								"typename": typename, 
								"prettytype": k.get_display_typename(),
								"selection": selection,
								"allow_edit": can_edit and not f.type[0].__const__,
								"allow_remove": can_edit,
								"parent_id": entity.id,
								"parent_type": entity.__class__.__name__,
								"return_path": quote(request.get_full_path())
							}))
	
	def __render_short(self, entity):
		template = get_template("plugins/entity_details_short.html")
		return template.render(Context({
			"entity": entity,
		}))
		
	def __add_value(self, request, typename, entity, field, value, template, is_inner, wrapper_for, lists, complex_values):
		if wrapper_for:
			f = value.get_field(wrapper_for)
			value = getattr(value, wrapper_for)
		else:
			f = field

		if not isinstance(value, (tuple, list, set, frozenset)):
			complex_values.append((field, self.__render_details(typename, value, template, request, True)))
		elif not is_inner:
			lists.append((f, self.__render_list(entity, field, value, request, wrapper_for)))
			
	def __render_details(self, typename, entity, template, request, is_inner = False):
		values = entity.values
		complex_values = []
		simple_values = []
		lists = []
		have_password = False
		
		for field, value in values:
			if not field.usereditable:
				# dont list attributes that shant be changed
				# only applies to Ptm.owner ?
				pass
			else:
				if isinstance(field.type, type) and issubclass(field.type, Password):
					have_password = True
					continue
				if not field.display_inline:
					if field.wrapper_for:
						if not isinstance(field.wrapper_for, (tuple, list, set, frozenset)):
							self.__add_value(request, typename, entity, field, value, template, is_inner, field.wrapper_for, lists, complex_values)
						else:
							for wrapper_for in field.wrapper_for:
								self.__add_value(request, typename, entity, field, value, template, is_inner, wrapper_for, lists, complex_values)
					else:
						self.__add_value(request, typename, entity, field, value, template, is_inner, None, lists, complex_values)
				else:
					simple_values.append((field, value))
				
		context = Context({
			"entity": entity,
			"values": simple_values,
			"complex_values": complex_values,
			"lists": lists,
			"typename": typename,
			"is_inner": is_inner,
			"allow_edit": request.user.is_staff or request.user.teagleuser.can_edit(entity),
			"have_password": have_password,
			"return_path": quote(request.get_full_path())
		})
		
		return template.render(context)
	
	def _render(self, context, instance, placeholder):
		try:
			typename = self._get_param(context, "entity_type")
		except MissingParameter:
			if instance and instance.body:
				typename = instance.body
			else:
				raise
		
		entity_class = self._get_entity_class(typename)
		entity_id = self._get_param(context, "id")
		entity = self.repo.get_entity(entity_class, int(entity_id))
		template = get_template("plugins/entity_details.html")
		user = context["request"].user
		
		if not user.is_staff:
			user.teagleuser.enforce_can_view(entity)
		
		details = self.__render_details(entity.__class__.__name__, entity, template, context["request"])
		
		#self.logger.debug("complex: %s %s" % (values, complex_values))
		helptext = "Here You can see details of a %s. " %(entity.__class__.__display_typename__)
		helptext += entity.__class__.__helptext__ or ""
		helptext += "Click on Edit %s to change it's configuration." %(entity.__class__.__display_typename__)
		
		context.update({
			"entity": entity,
			"typename": entity.__class__.__name__,
			"details": details, 
			"header_action": "Details",
			"help_text": helptext.replace("\n", "<br/>"),
		})
		
		return context
	
plugin_pool.register_plugin(ShowEntityPlugin)

class SetPasswordPlugin(TeagleRepoPlugin):
	name = _("SetPassword")
	render_template = "plugins/set_password.html"
	
	def _render(self, context, instance, placeholder):
		typename = self._get_param(context, "entity_type")
		assert typename == "Person"
		entity = self.repo.get_entity(self._get_entity_class(typename), int(self._get_param(context, "id")))
		
		context.update({
			"entity": entity,
			"typename": entity.__class__.__name__,
		})
		
		return context
		
plugin_pool.register_plugin(SetPasswordPlugin)

class EntityFormPlugin(TeagleRepoPlugin):
	""" Plugin for editing and/or adding Entities.
	"""
	name = _("EditEntity")
	render_template = "plugins/entity_form.html"
	
	def _render(self, context, instance, placeholder):
		request = context["request"]
		#logger.debug("looking for form: %s %s" % (request.session.session_key, request.session.items(), ))
		
		try:
			form = request.session.pop("validated_form")
		except KeyError:
			entity_type = self._get_param(context, "entity_type")
			add_to = request.REQUEST.get("add_to")
			klass = self._get_entity_class(entity_type)
			form = self.forms.get_form(klass)()
			try:
				id = int(context["request"].REQUEST["id"])
			except (KeyError, TypeError, ValueError):
				entity = id = None
				form.remove_field("id")
			else:
				entity = self.repo.get_entity(klass, id)
				
#				if not entity.is_persistent:
#					self.logger.warn("Trying to edit non-peristent entity %s. Will persist it first." % (entity, ))
				self.repo.persist(entity)
				
				form.set_values(entity)
		else:
			klass = form.__klass__
			add_to = request.session.pop("add_to", None)
			entity_type = klass.__display_typename__
			try:
				id = form.get_field_value("id")
			except KeyError:
				id = entity = None
			else:
				entity = self.repo.get_entity(klass, id)
			
		cname = hasattr(klass, "__display_typename__") and klass.__display_typename__ or klass.__name__
					
		header = "%s %s %s" % (id is None and "Add" or "Edit", cname, entity is not None and str(entity) or "")
		helptext = "On this page You can edit a %s. " % klass.__display_typename__
		helptext += klass.__helptext__ or ""
		helptext += "Use the form's fields to adjust it's configuration. "
		
		context.update({
			"entity": entity,
			"form": form,
			"entity_type": klass.__name__,
			"header": header,
			"add_to": add_to,
			"return_path": request.REQUEST.get("return_path"),
			"form_path": quote(request.get_full_path()),
			"help_text": helptext.replace("\n", "<br/>"),
		})
		
		return context
	
plugin_pool.register_plugin(EntityFormPlugin)

class LoginPlugin(TeagleRepoPlugin):
	model = CMSPlugin
	name = _("login")
	render_template = "plugins/login.html"
	
	def _render(self, context, instance, placeholder):
		context["next"] = context["request"].REQUEST.get("next", "/")
		context["help_text"] = "This is the TEFIS Portal login. Fill out the form with Your user name and password to log in."
		return context
	
plugin_pool.register_plugin(LoginPlugin)

class RegisterPlugin(TeagleRepoPlugin):
	name = _("register")
	render_template = "plugins/register.html"
	
	def _render(self, context, instance, placeholder):
		request=context["request"]
		try:
			form = request.session.pop("validated_form")
		except KeyError:
			form = self.forms.RegisterForm()
			
		context["form"] = form
		helptext = "This is the TEFIS Portal registration form. If You want to register an account, "
		helptext += "please fill out the fields and submit the form. "
		helptext += "After successful submission, You will have to wait for Your account to be approved by an administrator."
		helptext += "Thank You for Your interest in the TEFIS Project."
		context["help_text"] = helptext
				
		return context
	
plugin_pool.register_plugin(RegisterPlugin)	

class RowHack(object):
	def __init__(self):
		self.i = 0
		
	@property
	def hack(self):
		self.i+=1
		if self.i % 2 == 1:
			return "white"
		else:
			return "#efefef"#"lightgray"

from Portal.models import Applicant# workaround
#from .repoactions.models import Applicant
from teagle.repository.entities import Organisation, PersonRole

class ApproveUsersPlugin(TeagleRepoPlugin):
	name = _("approveUsers")
	render_template = "plugins/approve.html"

	def _render(self, context, instance, placeholder):
		request = context["request"]
		users = Applicant.objects.all()
		
		context["users"] = users	
		context["hack"] = RowHack()
		context["organisations"] = self.repo.list_entities(Organisation)
		context["organisation"] = int(request.REQUEST.get("organisation", -1))
		context["roles"] = self.repo.list_entities(PersonRole)
		context["roles_selected"] = map(int, request.REQUEST.getlist("roles[]"))
		context["help_text"] = "Approve Users"
		
		return context
	
plugin_pool.register_plugin(ApproveUsersPlugin)
#
#class UserHomePlugin(TeagleRepoPlugin):
#	""" PlugIn that renders the "home"-view on the Portal for different kinds of users.
#	"""
#	name = _("UserHome")
#	render_template = "plugins/home_user.html"
#	
#	def _render(self, context, instance, placeholder):
#		if  context["is_logged_in"] and not context["request"].user.is_staff:#request.REQUEST.get("is_logged_in")
#			# logged in users
#			context["userhome"] = self._render_home(context)
#		else:
#			# default
#			template = get_template("plugins/home_default.html")
#			context["userhome"] = template.render(context)
#		
#		context["help_text"] = "Welcome to TEFIS home."
#		
#		return context
#	
#	def _render_home(self, context):
#		""" Chooses which templates will be rendered based on user credentials.
#		"""
#		# TODO
#		teagleuser = context["request"].user.teagleuser
#		
##		if teagleuser.is_admin:
##			user_home = "admin home"
#		if teagleuser.is_partner:
#			user_home = self.__render_partner_home(context)
#		else:
#			user_home = self.__render_user_home(context)
#		
#		return user_home
#	
#	def __render_partner_home(self,context):
#		""" Renders the partner specific template.
#		"""
#		# TODO
#		template = get_template("plugins/home_user_partner.html")
#		return template.render(context)
#	
#	def __render_user_home(self, context):
#		""" Renders the normal user specific template.
#		"""
#		# TODO
#		template = get_template("plugins/home_user_user.html")
#		return template.render(context)
#	
#plugin_pool.register_plugin(UserHomePlugin)

