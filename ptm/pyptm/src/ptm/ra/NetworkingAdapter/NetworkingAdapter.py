#! /usr/bin/env python
from ptm.Resource import Resource
import logging

logger = logging.getLogger("ptm")

class Port(Resource):
	def get_number(self):
		return int(self.name)
	number = property(get_number)

	def get_type(self):
		return self.typename[:3].lower()
	type = property(get_type)


	def _get_configuration(self):
		return dict(number = self.number)


class TCPPort(Port):
	
	def __init__(self, *args, **kw):
		super(TCPPort, self).__init__(type = "tcpport", *args, **kw)
	
	@classmethod
	def get_instance(klass, parent, name, adapter):
		return adapter.get_port(parent, "tcp", name)

	@classmethod
	def list_instances(klass, parent, adapter):
		if parent.typename != 'ipv4interface':
			return ()
		return adapter.list_ports(parent, "tcp")

class UDPPort(Port):
	
	def __init__(self, *args, **kw):
		super(UDPPort, self).__init__(type = "udpport", *args, **kw)
	
	@classmethod
	def get_instance(klass, parent, name, adapter):
		return adapter.get_port(parent, "udp", name)

	@classmethod
	def list_instances(klass, parent, adapter):
		if parent.typename != 'ipv4interface':
			return ()
		return adapter.list_ports(parent, "udp")

class SqliteConnectionFactory(object):
	__connect = None

	def __init__(self, filename):
		self.__filename = filename

		if SqliteConnectionFactory.__connect is None:
			import sqlite3
			SqliteConnectionFactory.__connect = sqlite3.connect
			SqliteConnectionFactory.IntegrityError = sqlite3.IntegrityError

	def __call__(self):
		c = self.__connect(self.__filename)
		if c is None:
			raise Exception("Failed to connect to database %s" % (self.__filename, ))
		return c

import netifaces

class IPV4Address(dict):
	def __init__(self, data):
		self.update(data)

	def get_address(self):
		self["addr"]
	address = property(get_address)

	def get_netmask(self):
		return self["netmask"]
	netmask = property(get_netmask)

	def get_broadcast(self):
		return self["broadcast"]
	broadcast = property(get_broadcast)


class IPV4Interface(Resource):
	ANY = u"ANY"

	def __init__(self, *args, **kw):
		super(IPV4Interface, self).__init__(type = "ipv4interface", *args, **kw)

	def get_hwaddress(self, data = None):
		data = self.__get_data("AF_LINK", data)
		assert(len(data) == 1)
		return data[0]["addr"]
	hwaddress = property(get_hwaddress)

	def get_ipv4address(self, data = None):
		data = self.__get_data("AF_INET", data)
		assert(len(data) == 1)
		return IPV4Address(data[0])
	ipv4address = property(get_ipv4address)

	def _get_configuration(self):
		data = self.__get_data()
		return dict(
			name = self.name,
			hwaddress = self.get_hwaddress(data),
#			ipv4address = self.get_ipv4address(data),
		)

	def _set_attribute(self, *args, **kw):
		raise Exception("Setting attribute is not supported")

	def __get_data(self, family = None, data = None):
		if data is None:
			if self.name == self.ANY:
				data = {
					netifaces.AF_LINK: [ dict(addr = None) ],
					netifaces.AF_INET: [ dict(addr = "0.0.0.0", netmask = "0.0.0.0", broadcast="255.255.255.255") ]
				}
			else:
				try:
					data = netifaces.ifaddresses(self.name)
				except ValueError:
					raise Exception("No such interface: " + self.name)

		if family is not None:
			family = str(family)
			try:
				data = data[getattr(netifaces, family)]
			except (KeyError, AttributeError):
				raise ValueError("No such address family: " + family)

		return data

	@classmethod
	def get_instance(self, parent, name, adapter):
		return adapter.get_interface(name)

	@classmethod
	def add_instance(self, *args, **kw):
		raise Exception("Adding interfaces is not supported")

	@classmethod
	def list_instances(self, parent, adapter):
		return adapter.list_interfaces()
		
		
from ptm.Identifier import Identifier
from ptm.ResourceAdapter import ReflectiveGetAdapter, ReflectiveListAdapter

class NetworkingAdapter(ReflectiveGetAdapter, ReflectiveListAdapter):
	def __init__(self, manager, parent, connection_factory = None):
		super(NetworkingAdapter, self).__init__(manager = manager, parent = parent)

		self.add_type(IPV4Interface)
		self.add_type(TCPPort)
		self.add_type(UDPPort)

		self.register("ipv4interface*")

		if connection_factory is None:
			connection_factory = SqliteConnectionFactory("/var/lib/ptm/networking.sqlite")
		self.__connect = connection_factory

	def _set_attribute(self, key, value):
		pass


	def _add_resource(self, parentId, name, typename, config):
		if typename not in ("tcpport", "udpport"):
			raise Exception("Cannot add type: %s", typename)

		logger.debug("add: " + str(parentId) + " " + str(name) + "  " + str(config))

		iface = self.get_interface(parentId)
		typename = typename[0:3]
		name = config.get("number", name)

		if name is None or "min" in config or "max" in config:
			return self.add_port_dynamic(iface, typename, name, config.get(min), config.get(max))

		try:
			return self.add_port(iface, typename, name)
		except self.__connect.IntegrityError:
			raise ValueError("Sorry, port %s (%s) already taken" % (name, typename))

	def list_interfaces(self, conn = None, real = False):
		if conn is None:
			with self.__connect() as conn:
				return self.list_interfaces(conn)

		if real:
			where = " WHERE name != '%s'" % (IPV4Interface.ANY, )
		else:
			where = ''

		cursor = conn.cursor()
		cursor.execute("SELECT name FROM interface" + where);
		return [ IPV4Interface(adapter = self, identifier = self.parent_id.make_child_identifier("ipv4interface", r[0])) for r in cursor ] 

	def check_interface(self, name, conn = None):
		if conn is None:
			with self.__connect() as conn:
				logger.debug("CONN: %s" %(conn, ))
				return self.check_interface(name, conn)

		name = unicode(name)
		cursor = conn.cursor()
		cursor.execute("SELECT COUNT(name) FROM interface WHERE name=?", (name, ));
		if int(cursor.fetchone()[0]) != 1:
			raise Exception("No such interface: " + name)

	def get_interface(self, identifier, conn = None):
		logger.debug("Get iface: " + str(identifier))
		if isinstance(identifier, IPV4Interface):
			logger.debug("IPV4Interface")
			self.check_interface(identifier, conn)
			return identifier
		if isinstance(identifier, Identifier):
			logger.debug("Identifier")
			logger.debug("conn: %s" %(conn, ))
			identifier = identifier.resourcename
		if not identifier:
			raise ValueError("No interface given")
		#self.check_interface(identifier, conn)
		return IPV4Interface(adapter = self, name = identifier, parent = self.parent)

	def add_port(self, iface, type, num, conn = None):
		if conn is None:
			with self.__connect() as conn:
				return self.add_port(iface, type, num, conn)

		logger.debug("add_port: " + str(iface))
		type = unicode(type).lower()

		if not isinstance(iface, IPV4Interface):
			iface = self.get_interface(iface, conn)

		if type not in ("tcp", "udp"):
			raise ValueError("Illegal port type: " + type)

		try:
			num = int(num)
		except ValueError:
			raise ValueError("Illegal value for port number: %d " % ( num, ))

		if num < 0 or num > 65535:
			raise ValueError("port out of range: %d" % (num, ))

		cursor = conn.cursor()
		cursor.execute("INSERT INTO port (name, num, port_type) VALUES ('%s', %d, '%s')" % (iface.name, num, type))

		if iface.name == IPV4Interface.ANY:
			for i in self.list_interfaces(conn, True):
				self.add_port(i, type, num, conn)

		if type == "tcp":
			return TCPPort(adapter = self, parent = iface, name = num)
		return UDPPort(adapter = self, parent = iface, name = num)

	def __check_num(self, num):
		num = int(num)
		if num < 0 or num > 65535:
			raise ValueError("port out of range: " + unicode(num))

		return num

	def add_port_dynamic(self, iface, type, num = None, min = None, max = None, conn = None):
		if num is not None:
			num = self.__check_num(num)

			try:
				return self.add_port(iface, type, num, conn)
			except self.__connect.IntegrityError:
				pass

		if min is None:
			if num is not None:
				min = num
			if max is None:
				min = 20000
				if type == "tcp":
					min = 30000
			else:
				min = max
		min = self.__check_num(min)


		if max is None:
			max = 40000
		else:
			max = self.__check_num(max)

		if min > max:
			t = min
			min = max
			max = min

		for n in xrange(min, max + 1):
			if n != num:
				try:
					return self.add_port(iface, type, n, None)
				except self.__connect.IntegrityError:
					pass

		raise Exception("no free port found")

	def get_port(self, iface, type, num, conn = None):
		logger.debug("Get port for iface: " + str(iface))

		if conn is None:
			with self.__connect() as conn:
				return self.get_port(iface, type, num, conn)

		type = unicode(type)
		num = int(num)
		iface = self.get_interface(iface, conn)

		cursor = conn.cursor()
		cursor.execute("SELECT COUNT(num) FROM port WHERE name=? AND port_type=? AND num=?", (iface.name, type, num))
		count = int(cursor.fetchone()[0])
		assert(count <= 1)
		if count < 1:
			raise Exception("No such port: %s-%d on %s", type, num, iface)

		if type == "tcp":
			return TCPPort(adapter = self, name = num, parent = iface)
		return UDPPort(adapter = self, name = num, parent = iface)

	def list_ports(self, iface, type, conn = None):
		logger.debug("Get port for iface: " + str(iface))
		if conn is None:
			with self.__connect() as conn:
				return self.list_ports(iface, type, conn)
	
		type = unicode(type)
		iface = self.get_interface(iface, conn)

		cursor = conn.cursor()
		cursor.execute("SELECT num FROM port WHERE name=? AND port_type=?", (iface.name, type))

		klass = TCPPort
		if (type == u"udp"):
			klass = UDPPort

		ports =  [ klass(adapter = self, name = row[0], parent = iface) for row in cursor ]
		return ports
			

		
