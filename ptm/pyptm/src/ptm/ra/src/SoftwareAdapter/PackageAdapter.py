import logging
import imp
import datatypes
import tables
import inspect
from datatypes import Datatype
from path import path as  Path
from Package import Package, SoftwarePackage, Daemon
from ptm.ResourceAdapter import ReflectiveConstructorAddAdapter, ReflectiveGetAdapter, ReflectiveListAdapter
from ptm.Identifier import Identifier
from ptm.Resource import Resource
import sqlalchemy
from datatypes import Reference
from sqlalchemy.orm import mapper, relation
from exc import *

logger = logging.getLogger("ptm")
#logging.basicConfig(level=logging.DEBUG, format = "SDC: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s")

mapper(Package, tables.package_table, polymorphic_on = tables.package_table.c.application)

def _init_listitem(self, owner, payload):
	self.owner = owner
	self.payload = payload


class ScalarList(object):
	__emulates__ = list

	def __init__(self):
		self.__data = []

	def append(self, item):
		raise Exception(item)

	def remove(self, item):
		raise Exception(item)

	def extend(self, items):
		raise Exception(self, items)

	def __iter__(self):
		logger.debug("getting iterator for: " + str(self.__data))
		return iter(self.__data)

class ListWrapper(object):
	def __init__(self, package, type, attribute, pack = None, unpack = None, *args, **kw):
		super(ListWrapper, self).__init__(*args, **kw)
		self.__package = package
		self.__type = type
		self.__attribute = attribute
		self.__l = None
		if pack is not None:
			self.__packer = pack
		if unpack is not None:
			self.__unpacker = unpack

		self.c = 0

	def __get_list(self):
#		self.c += 1
#		if self.c > 15:
#			raise Exception()
		if self.__l is not None:
			return self.__l
		logger.debug("Getlist for: %s" % self.__package)
#		if self.__package.session is None:
#			s = self.__package.adapter.make_session()
#			self.__package = s.merge(self.__package)
#		assert(self.__package.session is not None)
		l = getattr(self.__package, self.__attribute)
		self.__l = l
		return l
	__list = property(__get_list)

	def append(self, o):
		logger.debug("list append: %s" % o)
		packed = self.__pack(o)
		self.__list.append(packed)
		assert(packed in self.__list)

	def __getitem__(self, i):
		r = self.__list.__getitem__(i)
		if isinstance(i, slice):
			return ( self.__unpack(i) for i in r )
		return self.__unpack(r)

	def __delitem__(self, index):
		del self.__list[index]

#	def __iter__(self):
#		return tuple(self.__list).__iter__()

	def __setitem__(self, index, value):
		logger.debug("!!listr assign: %s %s" % (index, value))
		if isinstance(value, (tuple, list, set, ListWrapper)):
			logger.debug(len(value))
			value = tuple(value)
			logger.debug("!!listr assign: %s" % str(value))
			value = [ self.__pack(v) for v in value ]
		else:
			value = self.__pack(value)
		if isinstance(index, slice):
			if index.start is None:
				index = slice(0, index.stop, index.step)
			if index.stop is None:
				index = slice(index.start, len(self), index.step)

		self.__list.__setitem__(index, value)
		logger.debug("!!!list now: %s" % str(self.__list))

	def __pack(self, o):
		logger.debug("Packing: %s" % o)
		return self.__type(self.__package, self.__packer(o))

	def __unpack(self, o):
		return self.__unpacker(o.payload)

	def __packer(self, o):
		return o
	__unpacker = __pack

	def __str__(self):
		return str(self.__list)

	def __len__(self):
		return len(self.__list)

class PackageAdapter(ReflectiveConstructorAddAdapter, ReflectiveGetAdapter, ReflectiveListAdapter):
	def __init__(self, manager, parent, engine, plugindir = None, repodir = None, baseclass = Package, paranoid = False, *args, **kw):
		super(PackageAdapter, self).__init__(manager = manager, parent = parent, *args, **kw)

		if not plugindir:
			plugindir = self.get_homedir() / "plugins"
		else:
			plugindir = Path(plugindir)
		
		if not repodir:
			repodir = self.get_homedir() / "repository"
		else:
			repodir = Path(repodir)
			
		for d in (plugindir, repodir):
			if not d.isdir():
				raise Exception("Not a directory: " + d)

		self.__engine = sqlalchemy.create_engine(engine, echo = False, connect_args = dict(check_same_thread = False))

		if not tables.package_table.exists(self.__engine):
			if paranoid:
				raise Exception("Database is not initialized")
			logger.info("Creating basic table structure")
			tables.md.create_all(self.__engine)


		self.make_session = sqlalchemy.orm.sessionmaker(bind=self.__engine, autoflush=True)
		self.__repodir = repodir
		self.__plugins = {}
		self.__classes = {}
		self.__fields = {}
		self.__baseclass = baseclass



		self.load_plugins(plugindir, True)

		for n, p in self.__plugins.iteritems():
			self.add_type(p, n, None)

	def merge(self, *args, **kw):
		raise NotImplementedError()

	def get_repodir(self):
		return self.__repodir
	repodir = property(get_repodir)

	def load_plugins(self, path, autocreate = False):
		path = Path(path)
		logger.debug ("Loadplugins: " + path)
		if path.isdir():
			logger.debug("Adding dir: " + path)
			for p in path.files("*.py"):
				self.load_plugins(p, autocreate)
		else:
			logger.debug("Examining file: " + path)
			m = imp.load_source(path.namebase, path)
			self.add_plugins(m, autocreate)

	def add_plugins(self, module, autocreate = False):
		try:
			keys = module.__all
		except AttributeError:
			keys = module.__dict__.keys()

		for k in keys:
			v = module.__getattribute__(k)
			if inspect.isclass(v) and v is not Package and issubclass(v, Package):
				logger.debug("Adding plugin: " + k)
				self.add_plugin(v, autocreate)

	def get_plugins(self):
		return self.__plugins.values()

	def get_fields(self, klass):
		return self.__fields[klass]


	def __add_class(self, klass, name, autocreate = False):
		logger.debug("add class: %s %s" % (klass, name))

		if "_sa_class_manager" in klass.__dict__ and klass._sa_class_manager.mapper is not None:
			return list(self.__fields.get(klass, []))

		for base in klass.__bases__:
			if issubclass(base, Package):
				break

		parent_fields = self.__add_class(base, base.__name__.lower(), autocreate)

		properties = {}
		fields = {}
		lists = {}
		for k in klass.__dict__.keys():
			v = klass.__dict__[k]
			if inspect.isclass(v) and issubclass(v, Datatype) and v is not Datatype:
				v = v()
			if isinstance(v, datatypes.Datatype):
				if isinstance(v, datatypes.List):
					lists[k] = v
				elif isinstance(v, datatypes.Datatype):
					fields[k] = v
				delattr(klass, k)

		m = base._sa_class_manager.mapper
		if len(m.tables) < 1:
			raise Exception("Wrong number of tables (%d) for class '%s'" % (len(m.tables), name))
		logger.debug(base._sa_class_manager.mapper.tables)
		basetable = base._sa_class_manager.mapper.local_table

		if not fields and not lists:
			table = basetable
			if not table.exists(self.__engine):
				raise Exception("parent table does not exist")
		else:
			cols = [ sqlalchemy.Column('id', sqlalchemy.ForeignKey(basetable.c.id, onupdate="CASCADE", ondelete="CASCADE"), primary_key = True) ]
			for k, v in fields.iteritems():
				cols.append(v.make_column(k))

			table = sqlalchemy.Table(name, basetable.metadata, *cols)
			self.__check_table(table, autocreate)

			for field in fields.values():
				if isinstance(field, Reference):
					attr = field.get_attribute_name()
					properties[attr] = getattr(table.c, field.name)
					setattr(klass, field.name, self.__make_reference_property(attr))

		for k, l in lists.iteritems():
			list_name = "__" + name + "_" + k
			cols = [
				sqlalchemy.Column('item_id', sqlalchemy.Integer, primary_key = True),
				sqlalchemy.Column('id', sqlalchemy.Integer(), sqlalchemy.ForeignKey(table.c.id, onupdate="CASCADE", ondelete="CASCADE"), nullable = False),
				l.payload.make_column('payload')
			]

		#	logger.debug(cols)

			list_table = sqlalchemy.Table(list_name, table.metadata, *cols)
			self.__check_table(list_table, autocreate)

			item_type = type(list_name, (), dict(__init__ = _init_listitem))

			mapper(item_type, list_table)
			
			mangled_name = "__" + k
			pack = unpack = None

			if isinstance(l.payload, Reference):
				pack = self.__pack_reference
				unpack = self.__unpack_reference

			setattr(klass, k, self.__make_list_property(item_type, mangled_name, pack = pack, unpack = unpack))
			properties[mangled_name] = relation(item_type, backref = "owner", cascade="all, delete-orphan", passive_deletes=True, lazy = False)
			#properties[mangled_name] = relation(item_type, cascade="all, delete-orphan", passive_deletes=True, lazy = False)
			parent_fields.append((k, True))

		logger.debug("mapping: %s %s %s %s" % (klass, table, base, name))
		mapper(klass, table, inherits = base, polymorphic_identity = name, properties = properties)
					
		parent_fields += [ ( k, False ) for k in fields.keys() ]
		logger.debug("fields: %s %s" % (name, parent_fields))
		self.__fields[klass] = tuple(set(parent_fields))
		return parent_fields

	def __make_reference_property(self, attr):
		return property(lambda o: self.__get_reference(o, attr), lambda o, v: self.__set_reference(o, attr, v))

	def __make_list_property(self, type, attribute, pack, unpack):
		return property(lambda o: ListWrapper(o, type, attribute, pack = pack, unpack = unpack))

	def __check_table(self, table, autocreate):
		if not table.exists(self.__engine):
			if not autocreate:
				raise InternalError("Table for application " + table.name + " does not exist")
			table.create(self.__engine)

	def add_plugin(self, klass, autocreate = False):
		#name = klass.get_typename()
		name = klass.__name__.lower()

		if name in self.__plugins:
			if self.__plugins[name] is klass:
				logger.debug("Plugin " + name + " already loaded")
				return
			raise InternalError("Plugin with name " + name + " already exists")

		self.__add_class(klass, name, autocreate)

		self.__plugins[name] = klass

	def __get_reference(self, o, k):
		if o.session is None:
			o = self.make_session().merge(o)
		uuid = getattr(o, k)
		return self.__unpack_reference(uuid)

	def __unpack_reference(self, uuid):
		if uuid is None:
			return None
		uuid = Identifier(uuid, need_full = True)
		return self.client.get_resource(uuid)

	def __pack_reference(self, v):
		if v is not None:
			v = unicode(Identifier(v, need_full = True))
		return v

	def __set_reference(self, o, k, v):
		setattr(o, k, self.__pack_reference(v))

	def _add_resource(self, parent, name, typename, config):
		kw = config
		logger.debug(kw)
		config.pop("port", None)
		config.pop("diameter_port", None)
		if "hss" in kw:
			kw["default_hss"] = kw["hss"]
		elif "default_hss" in kw:
			kw["hss"] = kw["default_hss"]
		logger.debug(kw)

		e = super(PackageAdapter, self)._add_resource(parent, name, typename, config)

		session = self.make_session()

		session.add(e)
		session.commit()
		
		try:
			self._deploy_entity(e, session)
		except:
			logger.exception("Error deploying")
		#	session.rollback()
			session.delete(e)
			session.commit()
			raise

		return e

	def _deploy_entity(self, e, session):
		e.deploy()
		try:
			e.postdeploy()
		except:
			logger.exception("Error in postdeploy()")
			try:
				e.undeploy()
			except:
				logger.exception("Error while undeploying after failed deployment")
			raise

class SoftwareAdapter(PackageAdapter):
	def __init__(self, basedir = None, installdir = "installed", shareddir = "shared", *args, **kw):
		super(SoftwareAdapter, self).__init__(baseclass = SoftwarePackage, *args, **kw)
		
		if not basedir:
			basedir = self.get_homedir()
		else:
			basedir = Path(basedir)

		if not basedir.isabs():
			raise ValueError("Need an absolute path for basedir, not " + str(basedir))

		installdir = Path(installdir)
		shareddir = Path(shareddir)

		if not installdir.isabs():
			installdir = basedir / installdir

		if not shareddir.isabs():
			shareddir = basedir / shareddir

		self.basedir = basedir
		self.installdir = installdir
		self.shareddir = shareddir

		basedir.checkdir()
		shareddir.checkdir()
		installdir.checkdir()

	def merge(self, package, source):
		typedir = self.installdir / package.typename

		if typedir.exists():
			typedir.checkdir()

		instancedir = typedir / package.name

		if instancedir.exists():
			raise Exception(instancedir + " already exists")

		source = source / "image"
		source.checkdir()

		source.move(instancedir)
		
	def _deploy_shared(self, klass, session, done):
		if klass is SoftwarePackage or klass is Daemon:
			return 

		done.append(klass)

		for b in klass.__bases__:
			if issubclass(b, SoftwarePackage) and b not in done:
				self._deploy_shared(b, session, done)

		logger.debug("query: " + str(klass))
		logger.debug("mappe: " + str(klass._sa_class_manager.mapper))

		count = session.query(klass).count()
		assert(count > 0)

		if count == 1:
			klass.deploy_shared(self)
		

	def _deploy_entity(self, e, session):
		self._deploy_shared(e.__class__, session, [])
		super(SoftwareAdapter, self)._deploy_entity(e, session)

	def need_port(self, port, type):
		if type not in ("tcp", "udp"):
			raise ValueError("Illegal port type: " + type)

		if isinstance(port, Resource):
			return port

		return self.client.add_entity(Identifier(self.parent.uuid) / "ipv4interface_ANY", None, type + "port", dict(number = port))

	def need_db(self, db = None, dbuser = None, rdbms = None):
		if (db is None or dbuser is None) and db is not dbuser:
			raise Exception("I need both, db and dbuser")

		if db is None:
			if rdbms is None:
				rdbms = self.add_resource(self.parent, None, "mysql", dict())

			dbuser = self.client.add_resource(rdbms, None, "dbuser", {})
			db = self.client.add_resource(rdbms, None, "database", dict(owner = dbuser))

		return (db, dbuser)
	
	def _set_attribute(self, *args, **kw):
		#raise NotImplementedError()
		pass

