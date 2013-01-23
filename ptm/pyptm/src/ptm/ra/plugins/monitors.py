from ptm.Resource import Resource
from ptm.Identifier import Identifier
from SoftwareAdapter.Package import Daemon, StartStopDaemonController
from SoftwareAdapter.Step import *
from SoftwareAdapter.datatypes import Integer, Reference, List
from path import path as Path

#__all = [ "MySQL", "MySQLManager" ]

class BasicStatsMonitor(Daemon):
	__appname__ = "basicstatsmonitor"

	interval = Integer(nullable = False)

	def __init__(self, interval = 1, *args, **kw):
		super(BasicStatsMonitor, self).__init__(*args, **kw)
		self.interval = int(interval)

	def get_configurator(self, target, **kw):
		kw["%%INTERVAL%%"] = str(self.interval)
		return super(BasicStatsMonitor, self).get_configurator(target, **kw)

	def get_controller(self):
		return StartStopDaemonController(package = self, executable = "./stats.py", outfile=True, makepidfile=True, fork = True, sleep = 2)

class Monitor(Daemon):
	interval = Integer(nullable = False)
	def __init__(self, interval = 1, *args, **kw):
		super(Monitor, self).__init__(*args, **kw)
		self.interval = int(interval)

	def get_configurator(self, target, **kw):
		kw["%%INTERVAL%%"] = str(self.interval)
		return super(Monitor, self).get_configurator(target, **kw)

class PingMonitor(Monitor):
	destinations = List(Reference(nullable = False))

	def __init__(self, targetResource, interval = 1, *args, **kw):
		super(PingMonitor, self).__init__(interval = interval, *args, **kw)
		if not targetResource:
			raise Exception("No targets given")
		for r in targetResource:
			self.destinations.append(r)
		logger.debug("dests: %s" % self.destinations)
		#TODO: Make this happen:
		#self.destinations = targetResource

	def get_configurator(self, target, **kw):
		kw["%%DESTINATIONS%%"] = ', '.join([ '"%s": "%s"' % (d.public_ip, d.identifier) for d in self.destinations ])
		kw["%%SRC_IP%%"] = str(self.parent.public_ip)
		return super(PingMonitor, self).get_configurator(target, **kw)
		
	def get_controller(self):
		return StartStopDaemonController(package = self, executable = "./pingmonitor.py", outfile=True, makepidfile=True, fork = True, sleep = 2)
