from django.conf import settings
from ngniutils.logging import get_logger, LoggerMixin
from teagle.repository.webapp.exc import CantHaz

def get_repo():
	try:
		return get_repo.__repo__
	except AttributeError:
		from teagle.repository import DEFAULT_URI, make_repoclient
		repo = getattr(settings, "TEAGLE_REPO", DEFAULT_URI)
		classes = getattr(settings, "TEAGLE_REPO_CLASSES", None)
		get_logger().info("Using %s as Teagle Repository.", repo)
		get_repo.__repo__ = make_repoclient(repo, classes = classes)
		return get_repo.__repo__

def get_form_manager():
	try:
		return get_form_manager.__form_manager__
	except AttributeError:
		from teagle.repository.webapp.forms import FormManager
		from teagle.repository.webapp.django.repoactions.models import Applicant
		get_form_manager.__form_manager__ = FormManager(get_repo(), Applicant, "teagle")
		return get_form_manager.__form_manager__

def _enforce_wrapper(f):
	def _f(self, request, *args, **kw):
		try:
			if not request.user.is_staff:
				f(self, request, *args, **kw)
		except AttributeError:
			if not getattr(settings, "TEAGLE_PERMISSIVE", False):
				raise CantHaz("Missing credentials")
	_f.__name__ = f.__name__
	return _f

def _perm_wrapper(f):
	def _f(self, request, *args, **kw):
		try:
			return request.user.is_staff or f(self, request, *args, **kw)
		except AttributeError:
			return  getattr(settings, "TEAGLE_PERMISSIVE", False)
	_f.__name__ = f.__name__
	return _f
			
class PermissionsHelper(LoggerMixin):
	@_enforce_wrapper
	def enforce_can_list(self, request, klass):
		request.user.teagleuser.enforce_can_list(klass)
		
	@_enforce_wrapper
	def enforce_can_add(self, request, klass):
		request.user.teagleuser.enforce_can_add(klass)
		
	@_enforce_wrapper
	def enforce_can_view(self, request, entity):
		request.user.teagleuser.enforce_can_list(entity)
	
	@_enforce_wrapper
	def enforce_can_edit(self, request, entity, member = None):
		request.user.teagleuser.enforce_can_edit(entity, member)
		
	@_enforce_wrapper
	def enforce_admin(self, request):
		if not request.teagleuser.is_admin:
			raise CantHaz()
		
	@_enforce_wrapper
	def enforce_can_delete(self, request, entity):
		request.user.teagleuser.enforce_can_delete(entity)
		
	@_perm_wrapper
	def can_add(self, request, klass):
		return request.user.teagleuser.can_add(klass)
	
	@_perm_wrapper
	def can_edit(self, request, entity, member = None):
		return request.user.teagleuser.can_edit(entity, member)
	
	@_perm_wrapper
	def can_delete(self, request, entity):
		return request.user.teagleuser.can_delete(entity)
		
		
			