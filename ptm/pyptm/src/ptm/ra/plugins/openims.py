from SoftwareAdapter.Package import Daemon, StartStopDaemonController, DummyController, SoftwarePackage
from SoftwareAdapter.Step import *
from SoftwareAdapter.datatypes import Reference, String, Boolean, List, Integer
from SoftwareAdapter.util import DummyPort
import logging as logger

class DiameterPeer(object):
	def __init__(self, ims_domain, peers = None, diameter_port = None, *args, **kw):
		super(DiameterPeer, self).__init__(*args, **kw)

		self.ims_domain = ims_domain
		self.diameter_port = DummyPort(20000)
		if peers is not None:
			for p in peers:
				self.peers.append(p)
		#raise Exception(self.diameter_port.number)
		#raise Exception(self.diameter_port)

	def get_configurator(self, target, **kw):
		kw["%%DIAMETER_PORT%%"] = 20000
		kw["%%HSS_ACCEPTOR_PORT%%"] = 20001
		peers = tuple(self.peers)
		logger.debug("!!!!peers: %s" % str(peers))
		kw["%%PEERS%%"] = "\n".join([ '<Peer FQDN="%s" Realm="%s" port="%d" />' % (p.typename + "." + self.ims_domain, self.ims_domain, p.get_attribute("diameter_port_number")) for p in peers])
		logger.debug("!!!!peers: %s" % kw["%%PEERS%%"])
		kw["%%IMS_DOMAIN%%"] = self.ims_domain
		return super(DiameterPeer, self).get_configurator(target, **kw)

	def get_ims_fqdn(self):
		return self.parent.public_ip
	ims_fqdn = property(get_ims_fqdn)

	def _get_configuration(self):
		c = super(DiameterPeer, self)._get_configuration()
		c["diameter_port_number"] = 20000
		return c

	def _set_configuration(self, config):
		config.pop("diameter_port", None)
		if "ims_domain" in config and config["ims_domain"] == self.ims_domain:
			config.pop("ims_domain")
			
		super(DiameterPeer, self).set_configuration(config)

#	def postdeploy(self):
#		pass
#		for p in self.peers:
#			peers = p.getAttribute("peers")
#			peers.append(self)
#			p.setAttribute("peers", peers)
#		return super(DiameterPeer, self).postdeploy()


class Ser(DiameterPeer, Daemon):
	port = Reference(nullable = False)
	ims_domain = String(nullable = False)
	diameter_port = Reference(nullable = False)
	peers = List(Reference(nullable = False))

	def __init__(self, port = None, diameter_port = None, *args, **kw):
		super(Ser, self).__init__(diameter_port = diameter_port, *args, **kw)

		self.port = self.adapter.need_port(port, "udp")

	def _get_controller(self, configfile):
		args = [ "-f", configfile, "-P", self.installdir / "pid", "-m", "75" ]
		return StartStopDaemonController(package = self, executable = Ser.get_shareddir(self.adapter) / "sbin/ser", daemonargs = args, ldpath = Ser.get_shareddir(self.adapter) / "lib/ser", pidfile = "pid", outfile = self.installdir / "ser.log")

	def get_configurator(self, target, **kw):
		kw["%%SER_SHARED%%"] = Ser.get_shareddir(self.adapter)
		kw["%%PORT%%"] = 20002
		return super(Ser, self).get_configurator(target, **kw)

	def _get_configuration(self):
		c = super(Ser, self).get_configuration()
		c["port_number"] = 20002
		return c

class PCSCF(Ser):
	rtpproxy = Reference()
	rtpproxy_enable = Boolean(nullable = False)

	def __init__(self, ims_domain, rtpproxy = None, rtpproxy_enable = False, *args, **kw):
		super(PCSCF, self).__init__(ims_domain = ims_domain, *args, **kw)

		if rtpproxy is None:
			rtpproxy_enable = False

		self.rtpproxy = rtpproxy
		self.rtpproxy_enable = rtpproxy_enable

	def get_controller(self):
		return self._get_controller(self.installdir / "etc/pcscf.cfg")

	def get_configurator(self, target, **kw):
		kw["%%RTPPROXY_ENABLE%%"] = self.rtpproxy_enable and 1 or 0
		if self.rtpproxy is not None:
			kw["%%RTPPROXY_IP%%"] = self.rtpproxy.parent.get_attribute("public_ip")
			kw["%%RTPPROXY_PORT%%"] = self.rtpproxy.port.get_attribute("number")
		else:
			kw["%%RTPPROXY_IP%%"] = "127.0.0.1"
			kw["%%RTPPROXY_PORT%%"] = 65500
		return super(PCSCF, self).get_configurator(target, **kw)

class SCSCF(Ser):
	registration_default_expires = Integer(nullable = False)
	default_hss = Reference(nullable = False)

	def __init__(self, ims_domain, hss, registration_default_expires = 3600, *args, **kw):
		super(SCSCF, self).__init__(ims_domain = ims_domain, *args, **kw)

		self.registration_default_expires = registration_default_expires
		self.default_hss = hss
	
	def get_controller(self):
		return self._get_controller(self.installdir / "etc/scscf.cfg")

	def get_configurator(self, target, **kw):
		kw["%%HSS_HOST%%"] = self.default_hss.parent.get_attribute("public_ip")
		kw["%%HSS_PORT%%"] = self.default_hss.get_attribute("diameter_port_number")
		return super(SCSCF, self).get_configurator(target, **kw)


#	def postdeploy(self):
#		p = self.default_hss
#		peers = p.getAttribute("peers")
#		peers = list(tuple(peers))
#		logger.debug("!!!Old peers: %s" % peers)
#	#	peers = []
#		peers.append(self)
#		logger.debug("!!!New peers: %s" % peers)
#		p.setAttribute("peers", peers)
#
#		super(SCSCF, self).postdeploy()

class ICSCF(Ser):
	db = Reference(nullable = False)
	dbuser = Reference(nullable = False)
	scscf = Reference(nullable = False)
	default_hss = Reference(nullable = False)

	def __init__(self, ims_domain, scscf, hss, db = None, rdbms = None, dbuser = None, *args, **kw):
		super(ICSCF, self).__init__(ims_domain = ims_domain, *args, **kw)

		db, dbuser = self.adapter.need_db(db = db, dbuser = dbuser, rdbms = rdbms)

		self.db = db
		self.dbuser = dbuser
		self.scscf = scscf
		self.default_hss = hss

	def get_controller(self):
		return self._get_controller(self.installdir / "etc/icscf.cfg")

	def get_configurator(self, target, **kw):
#		if self.scscf is not None:
		kw["%%SCSCF_HOST%%"] = self.scscf.parent.get_attribute("public_ip")
		kw["%%SCSCF_PORT%%"] = self.scscf.get_attribute("port_number")
		kw["%%HSS_HOST%%"] = self.default_hss.parent.get_attribute("public_ip")
		kw["%%HSS_PORT%%"] = self.default_hss.get_attribute("diameter_port_number")
		kw["%%DB_NAME%%"] = self.db.name
		kw["%%DB_USER%%"] = self.dbuser.name
		kw["%%DB_PASSWORD%%"] = self.dbuser.get_attribute("password")
		kw["%%DB_HOST%%"] = self.db.parent.get_attribute("public_ip")
		kw["%%DB_PORT%%"] = self.db.parent.get_attribute("port_number")
		return super(ICSCF, self).get_configurator(target, **kw)

	def postdeploy(self):
		schema = open(self.repodir / "schema.sql").read()
		self.db.execute(schema)

		self.__dbstuff()
		self.__set_peers()
		super(ICSCF, self).postdeploy()

	def __set_peers(self):
		logger.debug("stopping scscf")
		self.scscf.set_attribute("started", False)
		self.default_hss.set_attribute("peers", (self, self.scscf))
		logger.debug("starting scscf")
		self.scscf.set_attribute("started", True)

	def _set_configuration(self, config):
		if "hss" in config:
			config["default_hss"] = config.pop("hss")
		if "scscf" in config and config["scscf"].identifier != self.scscf.identifier:
			self.scscf.set_attribute("started", False)
		if unicode(config.get("started")).lower() == "restart":
			self.scscf.set_attribute("started", False)
			self.stop(True)
			self.default_hss.set_attribute("started", "restart")
			self.scscf.set_attribute("started", True)
			
		super(ICSCF, self)._set_configuration(config)


	def _do_set_configuration(self):
		super(ICSCF, self)._set_configuration()
		self.__dbstuff()
		#if "default_hss" in config:
		self.__set_peers()

	def __dbstuff(self):
#		if self.scscf is not None:	
		data = open(self.installdir / "etc/icscf.sql").read()
#		else:
#			data = open(self.installdir / "etc/empty.sql").read()
			
		self.db.execute(data)

class FHoSS(DiameterPeer, Daemon):
	peers = List(Reference(nullable = False))
	db = Reference(nullable = False)
	dbuser = Reference(nullable = False)
	ims_domain = String(nullable = False)

	def __init__(self, ims_domain, port = None, db = None, dbuser = None, rdbms = None, *args, **kw):
		super(FHoSS, self).__init__(ims_domain = ims_domain, *args, **kw)

		self.db, self.dbuser = self.adapter.need_db(db = db, dbuser = dbuser, rdbms = rdbms)
	#	self.port = self.adapter.need_port(port, "tcp")
		self.port = DummyPort(20003)

	def postdeploy(self):
		schema = open(self.repodir / "hss_db.sql").read()
		data = open(self.installdir / "userdata.sql").read()

		self.db.execute_method("execute", schema)
		self.db.execute_method("execute", data)

		super(FHoSS, self).postdeploy()

	def get_controller(self):
		return StartStopDaemonController(package = self, executable = "startup.sh", pidfile = "pid", makepidfile = True, fork = True, sleep=15)

	def get_configurator(self, target, **kw):
		kw["%%DB_HOST%%"] = self.db.parent.get_attribute("public_ip")
		kw["%%DB_PORT%%"] = self.db.parent.get_attribute("port_number")
		kw["%%DB_NAME%%"] = self.db.name
		kw["%%DB_USER%%"] = self.dbuser.name
		kw["%%DB_PASSWORD%%"] = self.dbuser.get_attribute("password")
		kw["%%PORT%%"] = "20003"
		return super(FHoSS, self).get_configurator(target, **kw)

	def _set_configuration(self, config):
		super(FHoSS, self)._set_configuration(config)
#		if "peers" in config:
#			self.db.execute("UPDATE imsu SET scscf_name=NULL, diameter_name=NULL")

class RTPProxy(Daemon):
	port = Reference(nullable = False)

	def __init__(self, port = None, *args, **kw):
		super(RTPProxy, self).__init__(*args, **kw)

		self.port = self.adapter.need_port(port, "udp")
	
	def get_controller(self):
		return StartStopDaemonController(package = self, executable = self.shareddir / "bin/rtpproxy", pidfile = self.installdir / "rtpproxy.pid", outfile = True, daemonargs = [ "-l", self.parent.public_ip , "-s", "udp:" + self.parent.public_ip + ":" + str(self.port.number), "-F", "-m", "35000", "-M", "40000", "-p", self.installdir / "rtpproxy.pid" ])

class OpenIMSCore(Daemon):
	pcscf = Reference(nullable = False)
	icscf = Reference(nullable = False)
	scscf = Reference(nullable = False)
	hss = Reference(nullable = False)

	def __init__(self, ims_domain, pcscf_sip_port = None, pcscf_diameter_port = None, icscf_sip_port = None, icscf_diameter_port = None, scscf_sip_port = None, scscf_diameter_port = None, hss_port = None, hss_diameter_port = None, *args, **kw):
		super(OpenIMSCore, self).__init__(*args, **kw)
		self.pcscf = self.adapter.add_resource(self.parent, None, "pcscf", dict(ims_domain = ims_domain, port = pcscf_sip_port, diameter_port = pcscf_diameter_port))
		self.hss = self.adapter.add_resource(self.parent, None, "fhoss", dict(ims_domain = ims_domain, port = hss_port, diameter_port = hss_diameter_port))
		self.icscf = self.adapter.add_resource(self.parent, None, "icscf", dict(ims_domain = ims_domain, port = icscf_sip_port, diameter_port = icscf_diameter_port, hss = self.hss))
		self.scscf = self.adapter.add_resource(self.parent, None, "scscf", dict(ims_domain = ims_domain, port = scscf_sip_port, diameter_port = scscf_diameter_port, hss = self.hss))
		
	def start(self, oknodo = False):
		self.started = True
		self.pcscf.start(True)
		self.scscf.start(True)
		self.icscf.start(True)
		self.hss.start(True)

	def stop(self, oknodo = False):
		self.started = False
		self.pcscf.stop(True)
		self.scscf.stop(True)
		self.icscf.stop(True)
		self.hss.stop(True)




