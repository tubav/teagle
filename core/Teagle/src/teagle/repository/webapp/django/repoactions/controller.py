'''
Created on 31.08.2011

@author: kca
'''
from ngniutils.net.django.Controller import Controller
from django.shortcuts import redirect
from django.contrib.auth import authenticate, login as django_login, logout as django_logout
from teagle.repository.entities import Ptm, Organisation, Person, PersonRole, get_entity_class,	ResourceInstance
from teagle.repository import Password
from hashlib import md5
from models import Applicant
from teagle.repository.webapp.django import get_repo, get_form_manager, PermissionsHelper
from django.core.urlresolvers import reverse
from teagle import get_teagle_flavour 
import string
from urllib import unquote

def get_controller_class():
	if get_teagle_flavour() in ("p2", "tefis"):
		from legacy import LegacyActionsController
		return LegacyActionsController
	return RepoActionsController

class RepoActionsController(Controller):
	LEGAL_CHARS = string.letters + string.digits + "_"
	
	def __init__(self, name = "repoactions", *args, **kw):
		super(RepoActionsController, self).__init__(name = name, *args, **kw)
		self.repo = get_repo()
		self.__forms = get_form_manager() 
		self.perms = PermissionsHelper()
	
	def _get_add_target(self, request):
		try:
			val = request.REQUEST["add_to"]
		except KeyError:
			return (None, None, None)
		
		val, _, attr = val.partition(".")
		
		typename, _, id = val.rpartition("-")
		
		klass = get_entity_class(typename)
		
		assert id and attr
	
		return (klass, id, attr)
		
	def register(self, request):	
		form = self.__forms.get_register_form()()
		
		vals = form.validate(request.REQUEST)
		
		if vals:
			repo = self.repo
			
			username = vals["username"]
			
			try:
				users = Applicant.objects.get(username = username)
			except Applicant.DoesNotExist: 
				users = repo.list_entities(Person, commonName = username)
					
			if users or vals["username"] == "admin":
				form.get_field("username").error = "This username is already taken"
			elif vals["password"] != vals["password2"]:
				form.get_field("password").error = "Passwords do not match"
			else:		
				applicant = Applicant(
					username = vals["username"],
					fullname = vals["firstname"] + " " + vals["lastname"],
					password = md5(vals["password"]).hexdigest(), 
					email = vals["email"],
					organisation = vals["organisation"]
				)
		
				applicant.save()
				
			return redirect(reverse("repogui", args = ("register_done", )))
	
		return redirect(reverse("repogui", args = ("register", )))
	
	def approve(self, request):
		self.perms.enforce_admin(request)
		
		vals = request.REQUEST
		
		id = int(vals["id"])
		
		user = Applicant.objects.get(id = id)
			
		if vals.get("do") != "approve":
			user.delete()
			request.notifications.success("Account '%s' has been denied" % (user.username, ))
			extra = ""
		else:
			repo = self.repo
			organisation = None
			roles = map(int, vals.getlist("roles[]"))
			
			try:
				organisation = int(vals["organisation"])
			except (KeyError, ValueError, TypeError):
				msg = "Please choose an organization for " + user.username
			else:
				if not roles:
					msg = "Please choose at least one role for " + user.username
				else:
					organisation = repo.get_entity(Organisation, organisation)
					
					rs = [ repo.get_entity(PersonRole, rid) for rid in roles ]
									
					p = Person(fields = dict(userName = user.username, fullName = user.fullname, password = Password(user.password), personRoles = rs, email = user.email))				
					organisation.people.append(p)
					
					repo.persist(organisation)
				
					user.delete()
					msg  = extra = ""
					
			if msg:
				extra = "?id=%d%s" % (id, organisation is not None and "&organisation=" + str(organisation) or '')
				for r in roles:
					extra += "&roles[]=" + str(r)
				request.notifications.error(msg)
			else:
				request.notifications.success("Account %s has been approved." % (user.username, ))
		
		return redirect("/en/accounts/approve/" + extra)
	
	def _parse_listinfo(self, request):
			addclass, addid, addattr = self._get_add_target(request)
	
			if not addclass:
				return (None, None, None)
			
			addentity = self.repo.get_entity(addclass, addid)
			return_entity = addentity
			
			add_to = addentity
			for attr in addattr.split("."):
				addentity = add_to
				add_to = getattr(addentity, attr)
	
			self.perms.enforce_can_edit(request, addentity, attr)
			
			assert isinstance(add_to, list)
				
			return (addentity, add_to, return_entity)
	
	def _list_op(self, request, operation):
#		try:
		entity = self.repo.get_entity(get_entity_class(self._get_param("entity_type")), int(self._get_param("id")))
	
		addentity, add_to, return_entity = self._parse_listinfo(request)
			
		getattr(add_to, operation)(entity)
			
		self.repo.persist(addentity)	
#		except ValueError:
#			if self._get_param("id") == None:
#				pass
		
		try:
			retpath = unquote(request.REQUEST["return_path"])
		except KeyError:
			retpath = reverse("repogui", args = ("show", )) + "?id=%d&entity_type=%s" % (return_entity.id, return_entity.__class__.__name__)
		
		return redirect(retpath)
	
	def remove_from_list(self, request):
		return self._list_op(request, "remove")
		
	
	def add_to_list(self, request):
		return self._list_op(request, "append")
	
	def _make_entity(self, klass, values):
		return klass(fields=values)
	
	def _check_name(self, name):
		if name[0] not in string.letters:
			return False 
		for c in name:
			if c not in self.LEGAL_CHARS:
				return False
			
		return True
	
	def _validate_form(self, form, input, klass):
		values = form.validate(input)
		if values and not issubclass(klass, ResourceInstance):
			try:
				name = values["commonName"]
			except KeyError:
				pass
			else:
				if not self._check_name(name):
					form.get_field("commonName").error = "Name must begin with a letter and only contain letters, digits and the underscore character"
					return None

		return values

	def form(self, request):
		type = self._get_param("entity_type")
		klass = get_entity_class(type)
		add_to = None		
		form = self.__forms.get_form(klass)
	
		try:
			id = int(request.REQUEST["id"])
		except KeyError:
			form = form()
			form.remove_field("id")
			entity = None
		else:
			entity = self.repo.get_entity(klass, id)
			form = form(instance = entity)
		
		values = self._validate_form(form, request.REQUEST, klass)
		if values:
			if entity is None:
				addentity, add_to, return_entity = self._parse_listinfo(request)
								
				self.perms.enforce_can_add(request, klass)
					
				if issubclass(klass, Ptm):
					try:
						owner = request.user.teagleuser.organisations[0]
					except (AttributeError, IndexError):
						from django.conf import settings
#						if not getattr(settings, "TEAGLE_PERMISSIVE"):
#							raise
						owner = self.repo.get_entity(Organisation, 1)
					values["provider"] = owner
					values["owner"] = self.repo.get_entity(Person, 1)#root
#				if issubclass(klass, ResourceSpec):
#					values["isInstantiable"] = "true"
				
				entity = self._make_entity(klass, values)
			else:
				self.perms.enforce_can_edit(request, entity)
				for k, v in values.iteritems():
					if k != "id":
						self.logger.debug("Setting value: %s -> %s" % (k, v))
						setattr(entity, k, v)
			
			if add_to is not None:
				add_to.append(entity)
				self.repo.persist(addentity)
			else:
				return_entity = entity
				self.repo.persist(entity)
				
			try:
				return_path = unquote(request.REQUEST["return_path"])
			except KeyError:				
				return_path = reverse("repogui", args = ("show", )) + "?id=%d&entity_type=%s" % (return_entity.id, return_entity.__class__.__name__)
			
			return redirect(return_path)
		
		self.logger.debug("form did not validate")
		request.session["validated_form"] = form
		request.session["add_to"] = request.REQUEST.get("add_to")
		
		try:
			return_path = unquote(request.REQUEST["form_path"])
		except KeyError:
			return_path = reverse("repogui", args = ("edit", ))
		return redirect(return_path)
	edit = add = form
	
	def _do_set(self, request, id):		
		user = self.repo.get_entity(Person, id)
		
		self.perms.enforce_can_edit(request, user)
		
		password = request.REQUEST.get("oldpassword", "")
		newpassword = request.REQUEST.get("newpassword", "")
		newpassword2 = request.REQUEST.get("newpassword2")
		
		if request.user.username != "admin":
			teagleuser = request.user.teagleuser
			
			if not (self.repo.check_password(password, user) or (teagleuser.is_admin and self.repo.check_password(password, teagleuser))):
				return "incorrect password"
			
		if len(newpassword) < 4:
			return "New password must be at least four characters long"
		
		if newpassword != newpassword2:
			return "Passwords do not match"
	
		user.password = self.repo.make_password(newpassword)
		
		self.repo.persist(user)
		
		return None
	
	def delete(self, request):
		type = self._get_param("entity_type")
		klass = get_entity_class(type)
		id = self._get_param("id")
		entity = self.repo.get_entity(klass, id)
		
		self.perms.enforce_can_delete(request, entity)
		
		self.repo.delete_entity(entity)
		
		return redirect(reverse("repogui", args= ["list"]) + "?entity_type=" + type)
	
	def set_password(self, request):
		id = int(self._get_param("id"))
		error = self._do_set(request, id)
		if error:
			request.notifications.error(error)
			return redirect("/en/accounts/set-password?entity_type=Person&id=" + str(id))
		
		request.notifications.success("Password changed")
		
		return redirect("/en/accounts/details/?id=" + str(id))
	
	def login(self, request):
		username = self._get_param("username")
		password = self._get_param("password")
		user = authenticate(username=username, password=password)
		if user is not None:
			django_login(request, user)
			return redirect(request.REQUEST.get("next", "/"))
		else:
			return redirect("/en/accounts/login/")
		
	def logout(self, request):
		django_logout(request)
		return redirect("/")
