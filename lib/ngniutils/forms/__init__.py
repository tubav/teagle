'''
Created on 02.04.2011

@author: kca
'''

import __builtin__
from abc import ABCMeta, abstractmethod
from django.utils.safestring import mark_safe
from cgi import escape
from copy import copy
from urllib2 import unquote
from ..logging import LoggerMixin
from operator import itemgetter

class FormError(Exception):
	pass

class ValidationError(FormError):
	pass

class SetupError(FormError):
	pass

class FormField(LoggerMixin):
	__metaclass__ = ABCMeta
	
	__logger = None
	
	def __init__(self, name, label = None, required = True, default = None, entity_class = None, wrapper_class = None, extraargs = None, usereditable = True, object_factory = None, unique = False, help="", *args, **kw):
		super(FormField, self).__init__(*args, **kw)
		
		if not name:
			raise ValueError(name)
		
		self.__name = name
		self.__required = required
		self.__default = default
		self.__value = None
		self.__error = None
		self.__label = label is not None and label or name
		self.__klass = entity_class
		self.__wrapper_klass = wrapper_class
		self.__extraargs = extraargs
		self.__usereditable = usereditable
		self.__object_factory = object_factory
		self.__unique = unique
		self.help = help
		
		if entity_class is not None and object_factory is None:
			raise SetupError("you must provide an object_factory when setting object_class.")
		
		assert(not (entity_class and unique))
		
		if unique and None in (object_factory, wrapper_class):
			raise SetupError("you must provide both wrapper_class and object_factory when demanding uniqueness.")
	
	def __copy__(self):
		o = self.__class__.__new__(self.__class__)
		o.__dict__ = self.__dict__.copy()
		return o
	
	def __getstate__(self):
		o = self.__object_factory
		self.__object_factory = None
		d = super(FormField, self).__getstate__()
		self.__object_factory = o
		return d
		
	def render(self):
		return mark_safe(self._render())
		
	@abstractmethod
	def _render(self):
		raise NotImplementedError()
	
	def validate(self, value, instance = None):
		self.logger.debug("Validating: %s - %s - %s" % (self.name, value, self.__klass))
		self.__value = value
		
		if self.name.endswith("id") and not value:
			return None
				
		try:	
			if not value and self.is_required:
				raise ValidationError("Missing value for %s" % (self.name, ))
			
			v = self._validate(value)			
			if self.__klass and not isinstance(v, self.__klass):
				if v:
					v = self.__object_factory.get_entity(self.__klass, v)
					if self.__wrapper_klass:
						v = self.__wrapper_klass(v)
						
					if self.__extraargs:
						for k, v2 in self.__extraargs.iteritems():
							setattr(v, k, v2)
				else:
					v = None
			elif self.__unique:
				existing = self.__object_factory.list_entities(self.__wrapper_klass, **{self.__name: v})
				for e in existing:
					if e != instance:
						raise ValidationError("A %s instance with %s=%s already exists." % (self.__wrapper_klass.get_display_typename(), self.__name, v))   
		except ValidationError, e:
			self.__error = e
			raise

		return v
	
	@abstractmethod
	def _validate(self, value):
		return value
		
	def get_name(self):
		return self.__name
	name = property(get_name)
	
	def _set_name(self, n):
		self.__name = n
	
	def get_label(self):
		return self.__label
	label = property(get_label)
	
	def get_value(self):
		return self.__value
	def set_value(self, value):
		self.__value = value
	value = property(get_value, set_value)
	
	def get_is_required(self):
		return self.__required
	is_required = property(get_is_required)
	
	def get_default(self):
		return self.__default
	default = property(get_default)
	
	def get_error(self):
		return self.__error is not None and self.__error or ''
	def set_error(self, error):
		self.__error = error
	error = property(get_error, set_error)
	
	def get_has_error(self):
		return self.__error is not None
	has_error = property(get_has_error)
	
	def get_is_usereditable(self):
		return self.__usereditable
	is_usereditable = property(get_is_usereditable)

	def get_rows(self):
		return self._get_rows
	rows = property(get_rows)
	
	def _get_rows(self):
		return 1
	
	@property
	def is_complex(self):
		return False

	def get_object_factory(self):
		return self.__object_factory
	def set_object_factory(self, o):
		self.__object_factory = o
	object_factory = property(get_object_factory, set_object_factory)
	
	@property
	def entity_class(self):
		return self.__klass

	def _make_attr(self, name, value):
		return value is not None and '%s="%s"' % (name, value) or ''

class BasicStringField(FormField):
	def __init__(self, name, required = True, default = None, maxlen = None, *args, **kw):
		super(BasicStringField, self).__init__(name = name, required = required, default = default, *args, **kw)
		
		self._check_param_gtz("maxlen", maxlen)
		
		self.__maxlen = maxlen
		
	def get_maxlen(self):
		return self.__maxlen
	maxlen = property(get_maxlen)
	
	def _validate(self, value):
		value = super(BasicStringField, self)._validate(value)

		if not value and self.is_required:
			raise ValidationError("Missing value")

		if self.__maxlen is not None and len(value) > self.__maxlen:
			raise ValidationError("Value is too long (max: %d)." % (self.__maxlen, ))

		return value
	
	def _check_param_gtz(self, name, value):
		if value is not None and value <= 0:
			raise ValueError("Value for %s must be >0 but is %d" % (name, value))

class StringField(BasicStringField):
	def __init__(self, name, required = True, default = None, size = None, maxlen = 255, type = 'text', *args, **kw):
		super(StringField, self).__init__(name = name, required = required, default = default, maxlen = maxlen, *args, **kw)
		
		self._check_param_gtz("size", size)
		
		self.__size = size
		self.__type = type
		
	def _render(self):
		size = self._make_attr("size", self.__size)
		maxlen = self._make_attr("maxlen", self.maxlen)
		if self.value is not None:
				value = self.value
				if isinstance(value, str):
						value = value.decode("utf-8")
				else:
						value = unicode(value)
				value = 'value="%s"' % (escape(value, True), ) or ''
		else:
				value = ""

		html = '<input type="%s" name="%s" %s %s %s %s/>' % (self.__type, self.name, size, maxlen, value, self._make_attr("title", self.help))
		return html

class TextField(BasicStringField):
	def __init__(self, name, required = True, default = None, maxlen = 255, rows = 4, cols = 80, *args, **kw):
		super(TextField, self).__init__(name = name, required = required, default = default, maxlen = maxlen, *args, **kw)

		self._check_param_gtz("cols", cols)
		self._check_param_gtz("rows", rows)
	
		self.__cols = cols
		self.__rows = rows		
	
	def _render(self):
		cols = self._make_attr("cols", self.__cols)
		rows = self._make_attr("rows", self.__rows)
		value = self.value is not None and escape(self.value, True) or ''
		#TODO check attributes of textarea element
		return '<textarea name="%s" cols="%s" rows="%s" %s>%s</textarea>' % (self.name, cols, rows, self._make_attr("title", self.help), value)

	def _get_rows(self):
		return self.__rows

class NumberField(StringField):
	def __init__(self, name, required = True, default = None, min = None, max = None, type = 'text', *args, **kw):
		self.__min = min
		self.__max = max
		
		if min is not None and max is not None and min > max:
			raise ValueError(self.__max)

		if min is not None and min is not None:
			maxlen = __builtin__.max(len(str(min)), len(str(max)))
		else:
			maxlen = None
				
		super(NumberField, self).__init__(name = name, required = required, default = default, maxlen = maxlen, type = type, *args, **kw)

	def _validate(self, value):
		value = super(NumberField, self)._validate(value)
		
		try:
			value = int(value)
		except ValueError, TypeError:
			raise ValidationError("Not a valid integer: %s" % (value, ))

		if self.__max is not None and value > self.__max:
			raise ValidationError("Value is too large (max: %d)." % (self.__max)) 

		if self.__min is not None and value < self.__min:
			raise ValidationError("Value is too small (max: %d)." % (self.__min)) 

		return value

class BooleanField(FormField):
	""" Field for boolean values. Renders as a CheckBox.
	"""
	def __init__(self, name, label = None, default = False, *args, **kw):
		super(BooleanField, self).__init__(name = name, label = label, default = default, required = False,  *args, **kw)
	
	def _validate(self, value):
		value = super(BooleanField, self)._validate(value)
		return bool(value)
	
	def _render(self):
		value = self.value and 'checked="checked"' or ""
		html = '<input type="checkbox" name="%s" %s %s />' % (self.name, value, self._make_attr("title", self.help))
		return html
	
class RegexField(StringField):
	def __init__(self, name, regex, reflags = 0, msg = "illegal value", label = None, required = True, default = None, size = None, maxlen = 255, type = 'text', *args, **kw):
		super(RegexField, self).__init__(name = name, label = label, required = required, default = default, maxlen = maxlen, *args, **kw)

		self.__regex = regex
		self.__reflags = reflags
		self.__msg = msg
		
	def _get_re(self):
		import re
		return re.compile(self.__regex, self.__reflags)

	def _validate(self, value):
		value = super(RegexField, self)._validate(value)
		
		result = self._get_re().match(value)
		
		if result is None:
			raise ValidationError(self.__msg)
		
		return value
	
class EmailField(RegexField):
	def __init__(self, name, label = None, required = True, default = None, size = None, maxlen = 255, type = 'text', *args, **kw):
		import re
		super(EmailField, self).__init__(name = name, regex = r'^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$', msg = "please enter a valid Email address", reflags = re.I, label = label, required = required, default = default, maxlen = maxlen, *args, **kw)

	
class IPField(StringField):
	pass

class ChoiceField(FormField):
	def __init__(self, name, choices, required = True, default = None, entity_class = None, object_factory = None, *args, **kw):
		super(ChoiceField, self).__init__(name = name, required = required, default = default, entity_class = entity_class, object_factory = object_factory, *args, **kw)
	
		self.__choices = choices
		if choices == entity_class == None: 
			raise SetupError("Must provide entity_class when not providing choices")

	def get_choices(self):
		if self.__choices is not None:
			return self.__choices
		
		choices = [ (str(e.id), e.commonName) for e in self.object_factory.list_entities(self.entity_class) ]
		if not self.is_required:
			choices = [ ("", "") ] + choices
		return choices
	def _set_choices(self, choices):
		#print("setting choices: %s" % (choices, ))
		self.__choices = choices
	choices = property(get_choices, _set_choices)

class SelectField(ChoiceField):
	
	def _render(self):
		val = (self.entity_class and isinstance(self.value, self.entity_class)) and str(self.value.id) or self.value
		options = ''.join([ '<option value="%s"%s>%s</option>' % (escape(str(v), True), v == val and ' selected="selected" ' or '', escape(str(l))) for v, l in self.choices ])
		return  '<select name="%s" %s>%s</select>' % (self.name, self._make_attr("title", self.help), options)


	def _validate(self, value):
		super(SelectField, self)._validate(value)
		
		if not self.choices or value in map(itemgetter(0), self.choices):
			return value
					
		raise ValidationError("Illegal value")

class MultipleCheckboxes(FormField):
	def __init__(self, name, choices, required = True, default = None, *args, **kw):
		super(MultipleCheckboxes, self).__init__(name = name, required = required, default = default, *args, **kw)
	
		self.__choices = choices
		
	def _render(self):
		return ''.join([ '<label for="%(id)s">%(label)s</label><input type="checkbox" name="%(name)s[]"%(selected)s />' % 
				dict(
					 id = escape("%s-%d" % (self.name, i) , True), 
					 selected = (v == self.value and ' selected="selected" ' or ''), 
					 label = escape(str(l)),
					 name = self.name) 
			for i, (v, l) in enumerate(self.choices) ])
		
class Form(LoggerMixin):
	__fields__ = [ ]
	__hidden_fields__ = [ ]
	
	def __init__(self, instance = None, strip_dots = False, *args, **kw):
		super(Form, self).__init__(*args, **kw)
		
		self.__instance = instance
		self.__fields = [ copy(f) for f in self._get_fields() ]
		self.__hidden_fields = [ copy(f) for f in self._get_hidden_fields() ]
		self.__strip_dots = strip_dots
		
	def validate(self, values, instance = None):
		errors = {}
		result = {}
		
		#fields = self.__get_field_dict()
		fields = self.all_fields
		self.logger.debug("Validating form: %s" % (self, ))
		
		for field in fields:
			#self.logger.debug("Validating field: %s" % (field.name, ))
			if not field.is_complex:
				v = unquote(values.get(field.name, ""))
			else:
				v = values
			try:
				result[field.name] = field.validate(v, self.__instance)
			except ValidationError, e:
				self.logger.exception("ValidationError for %s (%s): %s" % (field.name, v, repr(e)))
				errors[field.name] = str(e)
					
		self.logger.debug("all fields validated: %s %s" % (errors, result))

		return (not errors) and result or None
		
	def __get_field_dict(self):
		d = {}
		for f in self.fields + self.hidden_fields:
			d[f.name] = f
			
		return d
	
	def __getstate__(self):
		d = super(Form, self).__getstate__()
		d.pop("_Form__instance", None)
		#raise Exception(d)
		return d
	
	@property
	def all_fields(self):
		return self.__fields + self.__hidden_fields
			
	@classmethod
	def _get_fields(klass):
#		print("__get_fields")
		fields = Form.__fields__
		
		if klass is not Form:
			fields = fields + klass.__fields__
		
		#print ("Returning fields: %s" % (fields, ))
		
		return fields
	
	@classmethod
	def _get_hidden_fields(klass):
#		print("__get_hidden_fields")
		fields = Form.__hidden_fields__

		if klass is not Form:
			fields = fields + klass.__hidden_fields__
				
		return fields
	
	def get_hidden_fields(self):
#		print("get hidden fields")
		return self.__hidden_fields
	hidden_fields = property(get_hidden_fields)
	
	def get_fields(self):
#		print("get fields")
		return self.__fields
	fields = property(get_fields)
	
	def get_field(self, name):
		d = self.__get_field_dict()
		return d[name]
	
	def get_has_error(self):
		return any([ f.error for f in self.fields ])
	has_error = property(get_has_error)
	
	def set_values(self, instance):
		self.logger.debug("setting values: %s" % (self, ))
		for f in self.__fields + self.__hidden_fields:
			self.logger.debug("Setting field: %s " % (f.name, ))
			try:
				f.value = self._getvalue(instance, f.name)
			except AttributeError, e:
				self.logger.warn("Failed to get attribute %s from instance %s: %s" % (f.name, instance, repr(e)))
				
	def _getvalue(self, instance, name):
		assert name, "Name is empty"
		
		if self.__strip_dots:
			name = name.rpartition(".")[-1]
			return getattr(instance, name)
		
		value = instance
		for n in name.split("."):
			value = getattr(value, n)
			
		return value
		
	def get_field_value(self, name):
		return self.get_field(name).value
	
	def remove_field(self, name):
		self.__fields = [ f for f in self.__fields if f.name != name ]
		self.__hidden_fields = [ f for f in self.__hidden_fields if f.name != name ]
		
	def set_object_factory(self, o):
		for f in self.all_fields:
			f.object_factory = o
	object_factory = property(None, set_object_factory)

class ComplexField(FormField):
	def __init__(self, name, fields, hidden_fields, entity_class, object_factory, label = None, required = True, default = None, wrapper_class = None, extraargs = None, usereditable = True, *args, **kw):
		super(ComplexField, self).__init__(name = name, label = label, required = required, default = default, entity_class = entity_class, wrapper_class = wrapper_class, extraargs = extraargs, usereditable = usereditable, object_factory = object_factory, *args, **kw)
		for f in fields + hidden_fields:
			f._set_name(name + "." + f.name)
		self._form = create_form(name + "Form", fields = fields, hidden_fields = hidden_fields, klass = entity_class)
		self._form_instance = self._form(strip_dots = True)

	def _render(self):
		raise NotImplementedError()
	
	def _validate(self, value):
		values = self._form_instance.validate(value)
		
		if not values:
			raise ValidationError(" ")
		
		prefix = self.name + "."
		
		try: 
			id = int(values[prefix + "id"])
			value = self.object_factory.get_entity(self.entity_class, id)
			self.logger.debug("Altering value of complex field %s: %s - values: %s" % (self.name, value, values))
		except (KeyError, ValueError, TypeError): 
			value = self.entity_class()
			self.logger.debug("created value for complex field %s: %s - values: %s" % (self.name, value, values))
			
		prelen = len(prefix)
		for k, v in values.iteritems():
			k = k[prelen:]
			if k != "id":
				#self.logger.debug("Setting value: %s: %s" % (k, v))
				setattr(value, k, v)
		
		return value
	
	@property
	def is_complex(self):
		return True
	
	@property
	def fields(self):
		return self._form_instance.fields
	
	@property
	def hidden_fields(self):
		return self._form_instance.hidden_fields
	
	def __set_value(self, v):
		self._form_instance.set_values(v)
	def __get_value(self):
		raise NotImplementedError()
	value = property(__get_value, __set_value)
	
	"""
	def __getstate__(self):
		d = self.__dict__.copy()
		d["_form_instance"] = self._form(strip_dots = True)
		return d 
	"""
		
class ListField(FormField):
	def __init__(self, name, entity_class, object_factory, label = None, required = True, default = None, wrapper_class = None, extraargs = None, usereditable = True, *args, **kw):
		super(ComplexField, self).__init__(name = name, label = label, required = required, default = default, entity_class = entity_class, wrapper_class = wrapper_class, extraargs = extraargs, usereditable = usereditable, object_factory = object_factory, *args, **kw)
		
def create_form(name, fields, hidden_fields, klass = None):  
	t = type(name, (Form, ), {"__fields__": fields, "__hidden_fields__": hidden_fields, "__klass__": klass})
	globals()[name] = t
	return t
