from ptm.Resource import Resource
from ptm.Identifier import Identifier
from SoftwareAdapter.Package import Daemon, StartStopDaemonController
from SoftwareAdapter.Step import *
from SoftwareAdapter.datatypes import Reference
from socket import gethostname
from path import path as Path

#__all = [ "MySQL", "MySQLManager" ]

class OmacoAgent(Daemon):
	__appname__ = "omacoagent"
	__ptm_slots__ = ( "port", )

	port = Reference(nullable = False)

	def __init__(self, port = None, *args, **kw):
		super(OmacoAgent, self).__init__(*args, **kw)

		if not isinstance(port, Resource):
			port = self.client.add_resource(Identifier(self.parent.identifier) / "ipv4interface_ANY", None, "tcpport", dict(number = port))

		self.port = port

	def get_port_num(self):
		return self.port.number
	port_num = property(get_port_num)
	port_number = port_num

	def get_ip(self):
		return self.parent.public_ip
	ip = property(get_ip)
	public_ip = ip

	def get_controller(self):
		return StartStopDaemonController(package = self, executable = "./start.sh", outfile=True, makepidfile=True, fork = True, sleep = 2)

