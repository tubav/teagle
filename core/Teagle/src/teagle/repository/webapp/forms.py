'''
Created on 31.08.2011

@author: kca
'''

from ngniutils.forms import Form, StringField, ComplexField, NumberField, EmailField, SelectField, BooleanField
from teagle.repository import Entity, Password
from teagle.repository.entities import Ptm, ConfigParamAtomic, ConfigParamComposite
from ngniutils.types import AbstractTypeManager
from ngniutils import issubclass

class FormManager(AbstractTypeManager):
	def __init__(self, object_factory, applicant_class, module_name, *args, **kw):
		super(FormManager, self).__init__(name = module_name, *args, **kw)
		self.object_factory = object_factory
		self.forms = {}
		self.Applicant = applicant_class

	def _make_complex_field(self, attribute):
		fields = []
		assert issubclass(attribute.type, Entity)
		for a in attribute.type.get_fields():
			f = self._create_field(a, attribute.type, doinlines = False)
			if f is not None:
				fields.append(f)
		return ComplexField(attribute.name, fields = fields, hidden_fields = [ NumberField("id", type = "hidden") ], entity_class = attribute.type, object_factory = self.object_factory)

	def _get_field_args(self, attribute, cls):
		args = {
			"required": not attribute.nullable,
			"label": attribute.label,
			"help": attribute.description
		}
		
		if attribute.unique and cls not in (ConfigParamAtomic, ConfigParamComposite):
			args["unique"] = True
			args["object_factory"] = self.object_factory
			args["wrapper_class"] = cls
	
		return args

	def _create_field(self, attribute, parent_class, doinlines = True, prefix = None):
		"""
		Creates a HTML form field for the given attribute.
		Always returns a List.
		
		@param attribute: the attribute for which a FormField shall be created
		@param parent_class: class of the parent-Resource of the attribute
		@param doinlines:  do inlines? NOT doin' lines ...
		@param prefix: prefix
		@return: a list of FormField+ or []
		@raise NotImplementedError: if the attribute type is not supported yet.
		"""
		name = attribute.name
		label = attribute.label
		if prefix:
			name = prefix + "." + name
		# check the type
		if isinstance(attribute.type, type):
			# PASSWORD
			if issubclass(attribute.type, Password) or (parent_class is Ptm and attribute.name in ("provider", "owner")):
				return []
			# ENUM
			if attribute.enum:
				return [ SelectField(name, choices = [ (x, x) for x in attribute.enum ], label = label ) ]
			# STRING
			if issubclass(attribute.type, basestring):
				if name == "email":
					return [ EmailField(name, **self._get_field_args(attribute, parent_class)) ]
				return [ StringField(name, **self._get_field_args(attribute, parent_class)) ]
			# BOOLEAN
			if issubclass(attribute.type, bool):
				return [ BooleanField(name, label = attribute.label) ]
			# INT, LONG
			if issubclass(attribute.type, (int, long)):
				return [ NumberField(name, **self._get_field_args(attribute, parent_class)) ]
			# ENTITY
			if issubclass(attribute.type, Entity):
				if attribute.wrapper_for:
					a_type = attribute.type
					f_dict = a_type.get_field_dict()
					if isinstance(attribute.wrapper_for, (tuple, list, set, frozenset)):
						return [ self._create_field(f_dict[a], a_type, prefix = name)  for a in attribute.wrapper_for ]
					return self._create_field(f_dict[attribute.wrapper_for], a_type, prefix = name)
				if attribute.shared:
					return [ SelectField(name, choices = None, object_factory = self.object_factory, entity_class = attribute.type, **self._get_field_args(attribute, parent_class)) ]
				if attribute.display_inline and doinlines:
					return [ self._make_complex_field(attribute) ]
				return []
		else:
			#return StringField(name)
			return []
		# probably wont happen because of the else				
		raise NotImplementedError(attribute.type)

#This dynamically creates a form class for an entity class
	def _create_entity_form(self, klass):	
		
		d = {
			"__klass__": klass,
			"__hidden_fields__": [ NumberField("id", type = "hidden", usereditable = False) ],
			"__fields__":  filter( lambda l: l != [], reduce(list.__add__,  [ self._create_field(a, klass) for a in klass.get_fields() ], [])),
		}

		return self.create_type(klass.__name__+ "Form", Form, d)

	def get_form(self, klass):
		try:
			return self.forms[klass]
		except KeyError:
			form = self._create_entity_form(klass)
			self.forms[klass] = form
			return form
	
	def get_register_form(self):
		class RegisterForm(Form):
			__klass__ = self.Applicant
			__fields__ = [
			        StringField("firstname", label = "First Name", required = True),
			        StringField("lastname", label = "Last Name", required = True),
			        EmailField("email", label = "Email Address", required = True),
			        StringField("organisation", label = "Organisation", required = True),
			        StringField("username", label = "Desired Username", required = True),
			        StringField("password", label = "Password", required = True, type="password"),
			        StringField("password2", label = "Repeat Password", required = True, type="password"),
			]
		return RegisterForm
	RegisterForm = property(get_register_form)
	
	