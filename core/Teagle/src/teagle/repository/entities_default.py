'''
Created on 01.04.2011

@author: kca
'''

from Entity import Entity, Field, Password, Self
from teagle.exc import PermissionDenied
from teagle import Testbed, Resource
from teagle.repository.Entity import AbstractEntityType
from ngniutils.operator import attrproperty
from ngniutils import not_implemented
from operator import attrgetter
from teagle.repository.exc import UnknownEntityType

class TeagleRepositoryEntity(Entity):	
	__const__ = False
	__abstract__ = True
	
	def __init__(self, id = None, fields = None, repo = None, *args, **kw):
		super(TeagleRepositoryEntity, self).__init__(id = id, fields = fields, *args, **kw)
		
		self.__repo = repo		
	
	def get_repository(self):
		return self.__repo
	def _set_repository(self, repo):
		self.__repo = repo
	repository = property(get_repository)
	
class NamedEntity(TeagleRepositoryEntity):
	__fields__ = (
		Field("name", unicode, unique = True, displayname = "Name"), 
	)
	
	@property
	def commonName(self):
		return self.name
	
	@property
	def display_name(self):
		return unicode(self.name)

class DescribedEntity(TeagleRepositoryEntity):
	__fields__ = ( 
		Field("commonName", unicode, unique = True, displayname="Name"), 
		Field("description", unicode, default = "", coerce_none = True, displayname = "Description") 
	)
	
	@property
	def display_name(self):
		return self.commonName
	
	def __str__(self):
		return self.commonName# + ": " + self.description
	
	name = attrproperty("commonName")
	
class State(DescribedEntity):
	__const__ = True
	__abstract__ = True

class PersonRole(NamedEntity):
	__const__ = True
	__display_typename__ = "Role"
	
class Person(TeagleRepositoryEntity):
	__fields__ = (
		Field("userName", unicode, displayname = "Username", unique = True),
		Field("fullName", unicode, displayname = "Full name"),
		Field("password", Password, nullable = False),
		Field("email", unicode, inline_hint = True, nullable = False, default = "a@bc.de", coerce_none = True, displayname="E-Mail"),
		Field("personRoles", [ PersonRole ], default = [], displayname = "Roles")
	)   
	
	__display_typename__ = "User Account"
	
	def __str__(self):
		return "%s - %s" % (self.userName, self.fullName)
	
	@property
	def display_name(self):
		return unicode(self.userName)
	
	@property
	def commonName(self):
		return self.userName
	
	@property
	def real_description(self):
		return self.fullName
	
	@property
	def is_admin(self):
		if not self.is_persistent:
			return False
		
		return self.userName == "root" or self.repository.ROLE_ADMIN in self.personRoles

	@property
	def is_partner(self):
		if not self.is_persistent:
			return False
		
		return self.repository.ROLE_PARTNER in self.personRoles
	
	def __check_perms(self):
		if not self.is_persistent:
			self.logger.warn("Refusing operation for non persistent user %s" % (self.userName, ))
			return False
		
		if self.is_admin:
			return True
		
		return None
	
	def can_view(self, entity):
		p = self.__check_perms()
		if p is not None:
			return p
		return self is entity or not isinstance(entity, Person) 
	
	def can_list(self, klass):
		p = self.__check_perms()
		if p is not None:
			return p	  
		return klass is not Person
		
	def can_add(self, klass):
		p = self.__check_perms()
		if p is not None:
			return p	
		return self.is_partner and klass in (Ptm, ResourceSpec, Keyword, ConfigParamAtomic, ConfigParamComposite)

	def can_edit(self, entity, member = None):
		p = self.__check_perms()
		if p is not None:
			return p
		
		if member == "personRoles":
			return False
		if self is entity:
			return True
		
		if self.is_partner:
			if isinstance(entity, (ResourceSpec, ConfigParamComposite)) or (isinstance(entity, Organisation) and self in entity.people):
				return True
			if isinstance(entity, Ptm):
				for o in self.organisations:
					if o is entity.provider:
						return True
					
		return False
	
	def can_delete(self, entity):
		return self.can_add(entity.__class__) and self.can_edit(entity, None)
	
	def enforce_can_edit(self, entity, member = None):
		if not self.can_edit(entity, member):
			raise PermissionDenied(entity, member)
		
	def enforce_can_add(self, klass):
		if not self.can_add(klass):
			raise PermissionDenied(klass)
		
	def enforce_can_list(self, klass):
		if not self.can_list(klass):
			raise PermissionDenied(klass)
		
	def enforce_can_view(self, entity):
		if not self.can_view(entity):
			raise PermissionDenied(entity)
	
	def enforce_can_delete(self, entity):
		if not self.can_delete(entity):
			raise PermissionDenied(entity)

class ConfigParamAtomic(DescribedEntity):
	__fields__ = ( 
			Field("configParamType", unicode, enum = ("string", "integer", "float", "boolean", "reference", "reference-array"), displayname = "Type", default = "string"), 
			Field("defaultParamValue", unicode, nullable = True, displayname = "Default Value"),  
		)
	
	__types = {
		"integer": int,
		"string": unicode,
		"float": float
	}
	
	__display_typename__ = "Configuration Parameter"
	
	@property
	def is_array(self):
		return self.configParamType.lower().endswith("array")

	@property
	def is_dict(self):
		return self.configParamType.lower().endswith("dict")
	
	@property
	def is_reference(self):
		return self.configParamType.lower() == "reference"
	
	@property
	def is_reference_array(self):
		return self.configParamType.lower() == "reference-array"
	
	@property
	def is_reference_dict(self):
		return self.configParamType.lower() == "reference-dict"
	
	@property
	def holds_reference(self):
		return self.is_reference or self.is_reference_array or self.is_reference_dict
	
	def _get_reference(self, id):
		if not self.repository:
			raise NotImplementedError()
		return self.repository.get_unique_entity(ResourceInstance, commonName = id)
	
	def parse_value(self, value):		
		type = self.configParamType.lower()
		if not value:
			if type == "string":
				return ""
			return None
		
		if type.endswith(("dict", "array")):
			raise NotImplementedError()
		
		if type == "reference":
			return self._get_reference(value)
		
		if type.startswith("bool"):
			try:
				return bool(int(value))
			except ValueError: 
				return not value.lower() not in ("f", "false", "no", "n")

		try:
			return self.__types[type](value)
		except KeyError:
			raise TypeError(value, type)
	
class ConfigParamComposite(DescribedEntity):
	__fields__ = ( Field("configParams", [ ConfigParamAtomic ], default = [], displayname = "Configuration Parameters"), )
	
class Keyword(DescribedEntity):
	__fields__ = (Field("type", unicode, default = "default", nullable = False, coerce_none = True),)
	__display_typename__ = "Keyword"
	
class ResourceSpec(DescribedEntity):
	__fields__ = ( 
		Field("inherits", Self, nullable = True, shared = True, inline_hint = True, displayname = "Inherits from"), 
		Field("configurationParameters", ConfigParamComposite, wrapper_for = "configParams", default = ConfigParamComposite, coerce_none = True, displayname = "configuration parameters"),
		Field("keywords", [ Keyword ], default = [], coerce_none = True, shared = True, displayname = "Keywords"), 
	)
	__display_typename__ = "Resource Type"
	
	def __get_description_parts(self):
		if not self.description:
			return ()
		return self.description.split("|")
	
	@property
	def real_description(self):
		parts = self.__get_description_parts()
		return parts and parts[-1] or "<no description available>"
	
	@property
	def url(self):
		parts = self.__get_description_parts()
		return len(parts) > 1 and parts[-2] or ""
	
	@property
	def is_used(self):
		repo = self.repository
		
		if repo is None:
			return False
		
		return False
	
	def get_config_declaration(self):
		result = {}
		for cpa in self.configurationParameters.configParams:
			result[cpa.commonName] = cpa.configParamType
		return result

class Organisation(NamedEntity):
	__fields__ = (
		Field("people", [ Person ], nullable = False, default = [], coerce_none = True, shared = True, backref = "organisations", displayname = "People"),
	)
	
	def __str__(self):
		return self.name
	
class Ptm(DescribedEntity):
	__fields__ = (
		Field("provider", Organisation, inline_hint = True, displayname = "Provider"),
		Field("url", unicode, nullable = False, default = "", coerce_none = True, displayname = "URL"),
		Field("legacyUrl", unicode, nullable = True, displayname = "Legacy URL"),
		Field("resourceSpecs", [ ResourceSpec ], nullable = False, default = [], coerce_none = True, shared = True, backref = "ptms", displayname = "Instantiable Resources"),
		Field("supportedResources", [ ResourceSpec ], nullable = False, default = [], coerce_none = True, shared = True, backref = "supported_by_ptms", displayname = "Supported Resources"),
	)
	
	__display_typename__ = "Connector"
	
class Configlet(DescribedEntity):
	__fields__ = (
		Field("paramValue", unicode, nullable = True),
		Field("configParamAtomic", ConfigParamAtomic, nullable = False, shared = True)
	)
	
	@property
	def value(self):
		#print ("Parsing param %s: name=%s value=%s" % (self.id, self.name, self.paramValue))
		return self.configParamAtomic.parse_value(self.paramValue)
	
	@property
	def is_array(self):
		return self.configParamAtomic.is_array

	@property
	def is_dict(self):
		return self.configParamAtomic.is_dict
	
	@property
	def is_reference(self):
		return self.configParamAtomic.is_reference
	
	@property
	def is_reference_dict(self):
		return self.configParamAtomic.is_reference_dict
	
	@property
	def is_reference_array(self):
		return self.configParamAtomic.is_reference_array

	@property
	def holds_reference(self):
		return self.configParamAtomic.holds_reference

class ResourceInstanceState(State):
	pass

class ResourceInstance(DescribedEntity, Resource):
	__metaclass__ = AbstractEntityType
	
	__fields__ = (
		Field("configurationData", [ Configlet ], nullable = False, default = [], coerce_none = True),
		Field("parentInstance", Self, nullable = True, shared = True, inline_hint = True),
		Field("resourceSpec", ResourceSpec, shared = True, inline_hint = True),
		Field("state", ResourceInstanceState, shared = True, inline_hint = True),
	)
	
	identifier = attrproperty("commonName")
	parent = attrproperty("parentInstance")
	
	delete = update = not_implemented
	
	@property
	def specname(self):
		return self.resourceSpec.name
	
	@property
	def config(self):
		#print ("Getting config for: " + str(self.name))
		result = {}
		for cl in self.configurationData:
			result[cl.name] = cl.value
		return result
	
	def __get_type_info(self):
		result = set()
		for cl in self.configurationData:
			result.add( (cl.name, cl.configParamAtomic.configParamType))
		return result
	
	def copy_config(self, instance):
		if (self.__get_type_info() != instance.__get_type_info()):
			raise ValueError("Configuration specs do not match (%s != %s)" % (self.__get_type_info(), instance.__get_type_info()))
		
		config = {}
		for cl in self.configurationData:
			config[cl.name] = cl
		
		for cl in instance.configurationData:
			config[cl.name].paramValue = cl.paramValue
			
		self.set_is_updated(True)
	
	@property
	def provisioned(self):
		return self.state.name.lower() == "provisioned"
	
	def get_references(self):
		references = set()
		for cl in self.configurationData:
			if cl.paramValue:
				if cl.is_reference:
					references.add(cl.value)
				elif cl.is_reference_array:
					references.update(filter(None, cl.value))
					
		return references
	
	def get_reference_configurations(self):
		return filter(attrgetter("holds_reference"), self.configurationData)
	
class VctState(State):
	pass

class Vct(DescribedEntity, Testbed):
	__metaclass__ = AbstractEntityType
	
	__fields__ = (
		Field("providesResources", [ ResourceInstance ], default = [], coerce_none = True, backref = "vcts", shared = True),
		Field("shared", bool, default = False, coerce_none = True),
		Field("state", VctState, shared = True, inline_hint = True),
		Field("user", Person)
	)

	instances = attrproperty("providesResources")
	
	#TODO: make abstract propertes work automatically
	@property
	def state(self):
		return self.__getattr__("state")
	
_classes = None
	
def _get_subs(klass):
	result = []
	subs = klass.__subclasses__()
	if subs:
		result += reduce(list.__add__, map(_get_subs, subs))
	else:
		result.append(klass)
	
	return result
	
def get_entity_classes():
	global _classes
	
	if _classes is None:
		_classes = tuple(_get_subs(Entity))
	
	return _classes

def get_entity_class(name):
	for c in get_entity_classes():
		if c.__name__ == name:
			return c
	raise UnknownEntityType(name)
