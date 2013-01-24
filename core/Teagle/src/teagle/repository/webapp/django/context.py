'''
Created on 01.09.2011

@author: kca
'''
from ngniutils.logging import LoggerMixin
from ngniutils.net.django.exc import MissingParameter
from teagle.repository.webapp.django import get_repo, get_form_manager,	PermissionsHelper
from django.template.loader import get_template
from teagle.repository.Entity import Entity, Password
from django.template.context import RequestContext, Context
from teagle.repository.entities import get_entity_class, ResourceSpec, Ptm
from django.core.urlresolvers import reverse
from django.utils.safestring import mark_safe
from operator import attrgetter
from ngniutils.webapp.js import js_var, js_assign_call
from teagle.repository.webapp.django.grid import get_grid_fields
from urllib2 import quote
from cgi import escape

class ContextFactory(LoggerMixin):
	perms = PermissionsHelper()
	
	def __init__(self, viewurl = None, *args, **kw):
		super(ContextFactory, self).__init__(*args, **kw)
		if viewurl:
			self.__viewurl = viewurl
			
	@property
	def viewurl(self):
		try:
			return self.__viewurl
		except AttributeError:
			u = self.__viewurl = reverse("repogui", args = ("show", ))
			return u
	
	@property
	def repo(self):
		return get_repo()
	
	@property
	def forms(self):
		return get_form_manager()
	
	def _get_param(self, request, name):
		try:
			return request.REQUEST[name]
		except KeyError:
			raise MissingParameter(name)
	
	def edit(self, request):
		try:
			form = request.session.pop("validated_form")
		except KeyError:
			entity_type = self._get_param(request, "entity_type")
			add_to = request.REQUEST.get("add_to")
			klass = get_entity_class(entity_type)
			form = self.forms.get_form(klass)()
			try:
				id = int(request.REQUEST["id"])
			except (KeyError, TypeError, ValueError):
				entity = id = None
				form.remove_field("id")
			else:
				entity = self.repo.get_entity(klass, id)

#				if not entity.is_persistent:
#					self.logger.warn("Trying to edit non-peristent entity %s. Will persist it first." % (entity, ))
				self.repo.persist(entity)
				self.perms.enforce_can_edit(request, entity)
				
				form.set_values(entity)
		else:
			form.object_factory = self.repo
			klass = form.__klass__
			add_to = request.session.pop("add_to", None)
			
			#entity_type = hasattr(klass, "__display_typename__") and klass.__display_typename__ or klass.__name__
			entity_type = klass.__display_typename__
			try:
				id = form.get_field_value("id")
			except KeyError:
				id = entity = None
			else:
				entity = self.repo.get_entity(klass, id)
			
		cname = hasattr(klass, "__display_typename__") and klass.__display_typename__ or klass.__name__
					
		header = "%s %s %s" % (id is None and "Add" or "Edit", cname, id is not None and id or "")
		
		return {
			"entity": entity,
			"form": form,
			"entity_type": klass.__name__,
			"header": header,
			"add_to": add_to,
			"action": reverse("repoactions", args = ("form", ))
		}
	add = edit
	
	def _render_grid(self, klass, id = "grid"):
		fields = get_grid_fields(klass)
		labels = map(attrgetter("label"), fields)	 
		colModel = [ {"name":  name, "id": name} for name in map(attrgetter("name"), fields) ]
		
		names = js_var("colNames", labels)
		model = js_var("colModel", colModel)
		before = js_var("before", "before")
		create = js_assign_call("create_grid", "divid",  
				id, reverse("grid") + "?entity_type=" + klass.__name__, 
				names, model, klass.get_display_typename() + "s",
				self.viewurl + "?entity_type=" + klass.__name__,
				None, before)
		
		#raise Exception(str(create))
		extra = ""
		if klass == ResourceSpec:
			extra =	'$(divid + " .ui-jqgrid-titlebar").append($(\'<span id="filter" class="ui-jqgrid-title gridfilter" style="float: right; margin-right: 20px">Filter by PTM: <select style="background-color: white"><option value="">&lt;all&gt;</option>';
			ptms = self.repo.list_entities(Ptm)
			for p in ptms:
				extra += '<option value="%s">%s</option>' % (p.id, escape(p.commonName))
			extra += '</select></span>\'));'
			extra += '$(divid + " .ui-jqgrid-titlebar select").change(function() {$(\'#' + id + '\').trigger("reloadGrid");});'
			

#		raise Exception(self.viewurl)
		return """
			<script type="text/javascript">
				 var before = function() {
					var grid = $("#grid");
					var url = "/grid?entity_type=ResourceSpec";
					if(!url) {
					        return;
					}
					var filter = $("#filter" + " select option:selected").val();
					if(filter) {
					        url += "&ptm=" + filter;
					        grid.setGridParam({url: url});
					} else if(filter == "") {
					        grid.setGridParam({url: url});
					}
				}
				$(document).ready(function(){
					%(names)s
					%(model)s
					%(create)s
					%(extra)s
				});
			</script>
		""" % dict(
				names = names,
				model = model,
				create = create,
				extra = extra
		)

	
	def list(self, request, typename = None):
		typename = typename or self._get_param(request, "entity_type")
		klass = get_entity_class(typename)
		self.perms.enforce_can_list(request, klass)

		return {
			"typename": typename,
			"can_add": self.perms.can_add(request, klass),
			"grid": mark_safe(self._render_grid(klass)),
			"entities": self.repo.list_entities(klass, order_by = "commonName"),
		}
		
	def __render_list(self, entity, field, l, request, wrapper_for = None):
		template = get_template("repogui-list-details.html")
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
			
		can_edit = self.perms.can_edit(entity, field.name)
			
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
								"action_add": reverse("repoactions", args = ("add-to-list", )),
								"action_remove": reverse("repoactions", args = ("remove-from-list", )),
								"return_path": request.path
							}))
	
	def __render_short(self, entity):
		template = get_template("repogui-entity-details-short.html")
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
			#complex_values.append((field, self.__render_details(typename, value, template, request, True)))
			complex_values.append((field, self.__render_short(value)))
		elif not is_inner:
			lists.append((f, self.__render_list(entity, field, value, request, wrapper_for)))
			
	def __render_details(self, typename, entity, template, request, is_inner = False):
		complex_values = []
		simple_values = []
		lists = []
		have_password = False
		
		for field, value in entity.values:
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
			"allow_edit": self.perms.can_edit(request, entity),
			"allow_delete": self.perms.can_delete(request, entity),
			"have_password": have_password,
			"return_path": request.path
		})
		
		return template.render(context)
	
	def show(self, request):
		typename = self._get_param(request, "entity_type")			
		entity = self.repo.get_entity(get_entity_class(typename), int(self._get_param(request, "id")))
		template = get_template("repogui-entity-details.html")
		
		self.perms.enforce_can_view(request, entity)
		
		details = self.__render_details(entity.__class__.__name__, entity, template, request)
		
		#self.logger.debug("complex: %s %s" % (values, complex_values))
		
		return {
			"entity": entity,
			"typename": entity.__class__.__name__,
			"details": details, 
			"header_action": "Details"
		}
		
	def register(self, request):
		try:
			form = request.session.pop("validated_form")
		except KeyError:
			form = self.forms.get_register_form()()
			
		return { 
			"form": form,
			"header": "Account Registration",
			"action": reverse("repoactions", args = ("register", )) 
		}
				