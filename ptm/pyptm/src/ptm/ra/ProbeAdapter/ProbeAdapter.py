'''
Created on 14.02.2011

@author: jkr
'''

'''
This RA should deploy a ns-3 component as a regular instance with a given ns-3 script (currently the script has to be written in Python).
'''

from ptm.Resource import Resource

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import InstanceNotFound, DuplicateNameError, ConfigurationAttributeError, NoSuchMethodError
import logging

from path import path as Path

import os, subprocess, shlex, paramiko, sys
from subprocess import Popen
from subprocess import PIPE
from geoip import GeoIP

logger = logging.getLogger("ptm")

IPAddress = "empty"

# --- install commands ----

# Create temporary folder
cmd_mkdir = "mkdir probe ; cd probe"

# Install command for screen
cmd_install_screen = "-y install screen"

# Install command for git-core
cmd_install_git_core = "-y install git-core"

# Install command for libpcap
cmd_install_libpcap = "-y install libpcap-dev"

# Install commands for libev
cmd_download_libev = "wget http://dist.schmorp.de/libev/libev-4.04.tar.gz"
cmd_extract_libev = "tar -xf libev-4.04.tar.gz ; cd libev-4.04"
cmd_install_libev = "./configure ; make ; sudo make install ; cd .."

# Install commands for libipfix
cmd_download_libipfix = "git clone git://libipfix.git.sourceforge.net/gitroot/libipfix/libipfix ; cd libipfix"
cmd_install_libipfix = "./configure ; make ; sudo make install ; cd .."

# Install commands for impd4e
#cmd_install_software_properties = "sudo apt-get install python-software-properties"
#cmd_add_repo = "sudo add-apt-repository ppa:pt-team/pt"
#cmd_update = "sudo apt-get update"
#cmd_install_impd4e = "sudo apt-get install impd4e ; cd .."
cmd_download_impd4e = "git clone git://impd4e.git.sourceforge.net/gitroot/impd4e/impd4e ; cd impd4e"
cmd_install_impd4e = "./configure ; make ; sudo make install ; cd .."

cmd_install_ntp = "-y install ntp"
cmd_configure_ntp = ""

class ProbeAdapter(AbstractResourceAdapter):
	'''
	classdocs
	'''
	geoip = GeoIP("/usr/share/GeoIP")

	def __init__(self, manager, *args, **kw):
		super(ProbeAdapter, self).__init__(*args, **kw)
		self.__instances = set("0")
		manager.register_adapter("/proberesource", self)
		#manager.register_adapter("/pnode-0/proberesource", self)
		# Print the IP-Address of this machine
		output = Popen(["ifconfig"], stdout=PIPE).communicate()[0]
		indexIPStart = output.find("inet addr")+10
		indexIPEnd = output.find("Bcast")
		global IPAddress
		IPAddress = output[indexIPStart:indexIPEnd].strip(' ')
		logger.debug("--- The IP-Address of this machine is: "+IPAddress+" ---")

	def list_resources(self, parent, typename):
		assert(typename == "proberesource" or not typename)
		assert(parent == None)
		#assert(parent == "/pnode-0")
		return [ Identifier("/proberesource-" + i) for i in self.__instances ]

	def add_resource(self, parent_id, name, typename, config, owner = None):
	
		assert(typename == "proberesource")

		global IPAddress
		interface = "eth0" # default interface
		oid = "empty"
		location = None
		collector = IPAddress+":"+"4739"
		packetFilter = ""
		samplingRatio = "10.0"
		install = False
		hostname = "empty"
		public_ip = "empty"
		password = "empty"
		resource = "empty"
		username = "root" # default username

		configLength = len(config)


		# Reading out the config parameters
		if config.has_key("interface"):
			interface = config["interface"]

		if config.has_key("oid"):
			oid = config["oid"].strip('s')

		if config.has_key("location") and config["location"]:
			location = config["location"].strip('s')

		if config.has_key("collector"):
			collector = config["collector"].strip('s')

		if config.has_key("packetFilter"):
			packetFilter = config["packetFilter"].strip('s')
			
		if config.has_key("samplingRatio"):
			samplingRatio = config["samplingRatio"].strip('s')

		if config.has_key("install"):
			install = True

		if config.has_key("resource"):
			resource = config["resource"]

		if config.has_key("username"):
			username = config["username"]

		if config.has_key("target"):
			if resource != "empty":
				target = config["target"]
				configuration = target.get_adapter().get_configuration(resource)
				#hostname = configuration["hostname"]
				public_ip = configuration["public_ip"]
				if not password:
					password = None
				else:
					password = configuration['password']
					

		if not location:
			if public_ip and public_ip != "empty":
				location = "%.2f:%.2f:2" % self.geoip.lat_lon(public_ip)
			else:
				location = "52:13:2" # default location (Berlin)
		
				

		# Print the entered parameters
		logger.debug("--------------------------------------------------")
		logger.debug("--> Number of entered parameters: "+str(configLength))
		logger.debug("--> interface = "+interface)
		logger.debug("--> oid = "+oid)
		logger.debug("--> location = "+location)
		logger.debug("--> collector = "+collector)
		logger.debug("--> packetFilter = "+packetFilter)
		logger.debug("--> samplingRatio = "+samplingRatio)
		logger.debug("--> install = "+str(install))
		#logger.debug("--> hostname = "+hostname)
		logger.debug("--> public_ip = "+public_ip)
		logger.debug("--> password = "+password)
		logger.debug("--> resource = %s" %resource)
		logger.debug("--> username = "+username)
		logger.debug("--------------------------------------------------")

		# Collector Parsing
		indexCollectorSplit = collector.find(":")
		collectorIP = collector[0:indexCollectorSplit]
		collectorPort = collector[indexCollectorSplit+1:len(collector)]		

		# Enumerate the probe instance
		n = "0"
		if not name:
			if config.has_key("oid"):
				n = oid
				name = n

			else:
				i = 0
				while True:
					n = str(i)
					if n not in self.__instances:
						break
					i += 1
				name = n
				oid = name
		else:
			if name in self.__instances:
				raise DuplicateNameError(parent_id, typename, name)

		self.__instances.add(n)

		# Writing the command

		cmd = ("sudo /usr/bin/impd4e -i i:" + interface + " -C " + collectorIP +
			" -P " + collectorPort + " -o " + oid + " -l '" + location +
                        "' -r " + samplingRatio)
		

		if (packetFilter != ""):
			cmd = cmd + " -f "+packetFilter

		if (public_ip == "empty"):
			# Run the probe on the local machine
			self.run_local(cmd,oid)
		else:
			# Run the probe on another machine
			self.run_remote(cmd,oid,username,public_ip,password,install)
			
		return name

	
		

	def execute_command(self,channel,cmd,password,username):

		channel.send(cmd)

		resp = ""
		while (resp.find(username+"@") == -1):
			resp = channel.recv(1000000)
			logger.debug(resp)

			if resp.find("password") != -1:
				logger.debug("--- password needed! ---")
				channel.send(password+"\n")

	def wait_for_new_execute(self,channel,password,username):

		resp = ""
		while (resp.find(username+"@") == -1):
			resp = channel.recv(1000000)
			logger.debug(resp)

			if resp.find("password") != -1:
				logger.debug("--- password needed! ---")
				channel.send(password+"\n")

	def command_available(self,channel,cmd,username):

		channel.send(cmd)
		available = True

		resp = ""
		while (resp.find(username+"@") == -1):
			resp = channel.recv(1000000)
			logger.debug(resp)

			if (resp.find("command not found") != -1 or resp.find("not installed") != -1):
				available = False

		return available

	def run_remote(self,cmd,oid,username,public_ip,password,install):
		logger.debug("--- starting impd4e on machine "+public_ip+" ...")

		global cmd_mkdir
		global cmd_install_screen
		global cmd_install_git_core
		global cmd_install_libpcap
		global cmd_download_libev
		global cmd_extract_libev
		global cmd_install_libev
		global cmd_download_libipfix
		global cmd_install_libipfix
		global cmd_install_software_properties
		global cmd_add_repo
		global cmd_update
		global cmd_install_impd4e
		global cmd_install_ntp
		cmd_execute = "screen -m -d -S probe"+oid+" "+cmd
		logger.debug(cmd_execute)

		# Initialize Client and Channel.
		client = paramiko.SSHClient()
		client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
		client.connect(public_ip,username=username,password=password)

		# Logging in.
		channel = client.invoke_shell()
		self.wait_for_new_execute(channel,password,username)

		if (install == True):
			# Create temporary folder
			self.execute_command(channel,cmd_mkdir+'\n',password,username)
			
			# Check out the distribution
			sftp = client.open_sftp()
			files = sftp.listdir("/etc/")
			found_fedora = False
			found_debian = False
			for file in files:
				if (file.find("fedora") != -1):
					found_fedora = True
					break
				if (file.find("debian") != -1):
					found_debian = True
					break
			
			if (found_fedora == True):
				cmd_install_screen = "sudo yum " + cmd_install_screen
				cmd_install_git_core = "sudo yum " + cmd_install_git_core
				cmd_install_libpcap = "sudo yum " + cmd_install_libpcap

			elif (found_debian == True):
				cmd_install_screen = "sudo apt-get " + cmd_install_screen
				cmd_install_git_core = "sudo apt-get " + cmd_install_git_core
				cmd_install_libpcap = "sudo apt-get " + cmd_install_libpcap
				cmd_install_impd4e_apt = "sudo add-apt-repository ppa:pt-team/pt && apt-get update && apt-get install impd4e"


			else:
				# Check out which package managers are available
				apt_available = self.command_available(channel,"apt\n")
				yum_available = self.command_available(channel,"yum\n")
				if (apt_available == True):
					cmd_install_screen = "sudo apt-get " + cmd_install_screen
					cmd_install_git_core = "sudo apt-get " + cmd_install_git_core
					cmd_install_libpcap = "sudo apt-get " + cmd_install_libpcap

				elif (yum_available == True):
					cmd_install_screen = "sudo yum " + cmd_install_screen
					cmd_install_git_core = "sudo yum " + cmd_install_git_core
					cmd_install_libpcap = "sudo yum " + cmd_install_libpcap

				else:	
					raise Exception("No apt or yum found, so the probe " +
									"can't be installed automatically. Try " +
									"to install impd4e manually.")

			# Install screen
			self.execute_command(channel,cmd_install_screen+'\n',password,username)
			# Install git-core
			self.execute_command(channel,cmd_install_git_core+'\n',password,username)
			# Install libpcap
			self.execute_command(channel,cmd_install_libpcap+'\n',password,username)

			# Install libev
			self.execute_command(channel,cmd_download_libev+'\n',password,username)
			self.execute_command(channel,cmd_extract_libev+'\n',password,username)
			self.execute_command(channel,cmd_install_libev+'\n',password,username)
			# Install libipfix
			self.execute_command(channel,cmd_download_libipfix+'\n',password,username)
			self.execute_command(channel,cmd_install_libipfix+'\n',password,username)
			# Get newest version of impd4e
			try:
				self.execute_command(channel,cmd_install_impd4e_apt+'\n',password,username)
			except:
				raise
			self.execute_command(channel,cmd_download_impd4e+'\n',password,username)
			self.execute_command(channel,cmd_install_impd4e+'\n',password,username)

		
		# start impd4e
		logger.debug("Remotely executing: '%s'" % (cmd_execute, ))
		self.execute_command(channel,cmd_execute+'\n',password,username)
		
		# Close channel and client
		channel.close()
		client.close()

		logger.debug("--- impd4e started on machine "+public_ip+" ---")

	def run_local(self,cmd,oid):
		logger.debug("--- installing impd4e on this machine ... --- ")
		self.install_probe_local()
		logger.debug("--- starting impd4e on this machine ... --- ")

		cmd_execute = "screen -m -S probe"+oid+" "+cmd
		
		logger.debug("locally running: %s" %cmd_execute)

		os.system(cmd_execute)

		logger.debug("--- impd4e started on this machine! ---")
		
	def install_probe_local(self):
		apt = True
		#"sudo add-apt-repository ppa:pt-team/pt && apt-get update && apt-get install impd4e"
		try:
			subprocess.Popen(["sudo", "add-apt-repository", "-y", "ppa:pt-team/pt"]).wait()
			subprocess.Popen(["sudo", "add-get", "-y", "update"]).wait()
			subprocess.Popen(["sudo", "add-get", "-y", "install", "impd4e"]).wait()
		except:
			apt = False
		if not apt:
			if not os.path.exists("/opt/ptm/ra/ProbeAdapter/tmp"):
				subprocess.Popen(["mkdir", "/opt/ptm/ra/ProbeAdapter/tmp"])
			try:
				if "debian" in self.os :
					subprocess.Popen(["sudo", "apt-get", "-y", "install", "libpcap-dev"]).wait()
				elif "fedora" in self.os or "redhat" in self.os:
					subprocess.Popen(["sudo", "yum", "-y", "install", "libpcap-dev"]).wait()
				else:
					logger.error("Unknown OS: %s" %self.os)
				subprocess.os.chdir("/opt/ptm/ra/ProbeAdapter/tmp")
				subprocess.Popen(["wget",  "http://dist.schmorp.de/libev/libev-4.04.tar.gz"]).wait()
				subprocess.Popen(["tar", "-xf",  "libev-4.04.tar.gz"]).wait()
				subprocess.os.chdir("/opt/ptm/ra/ProbeAdapter/tmp/libev-4.04")
				subprocess.Popen(["./configure"]).wait()
				subprocess.Popen(["make"]).wait()
				subprocess.Popen(["sudo", "make", "install"]).wait()
				subprocess.os.chdir("/opt/ptm/ra/ProbeAdapter/tmp/")
				subprocess.Popen(["git", "clone", "git://impd4e.git.sourceforge.net/gitroot/impd4e/impd4e"]).wait()
				subprocess.os.chdir("/opt/ptm/ra/ProbeAdapter/tmp/impd4e")
				subprocess.Popen(["./configure"]).wait()
				subprocess.Popen(["make"]).wait()
				subprocess.Popen(["sudo", "make", "install"]).wait()
			except:
				print "Unexpected error:", sys.exc_info()[0]
				raise

	@property
	def os(self):
		otherwise = ""
		list_of_distros = ["/etc/SuSE-release", 
			"/etc/redhat-release", 
			"/etc/fedora-release", 
			"/etc/slackware-release",
			"/etc/debian_release", 
			"/etc/mandrake-release",
			"/etc/gentoo-release"]
		for elem in list_of_distros:
			if os.path.exists(elem):
				return elem
			else:
				otherwise = elem
		
		return otherwise

	def have_resource(self, identifier):
		assert(identifier.parent == None)
		#assert(identifier.parent == "/pnode-0")
		assert(identifier.typename == "proberesource")
		return identifier.name in self.__instances


	def get_resource(self, identifier):
		return identifier


	def get_configuration(self, identifier):
		assert(identifier.parent == None)
		#assert(identifier.parent == "/pnode-0")
		assert(identifier.typename == "proberesource")

		if not self.have_resource(identifier):
			raise InstanceNotFound(identifier)

		return {}


	def set_configuration(self, identifier, config):
		assert(identifier.parent == None)
		#assert(identifier.parent == "/pnode-0")
		assert(identifier.typename == "proberesource")
		return


	def get_attribute(self, identifier, name):
		assert(identifier.parent == None)
		#assert(identifier.parent == "/pnode-0")
		assert(identifier.typename == "proberesource")
		raise ConfigurationAttributeError(name)


	def set_attribute(self, identifier, name, value):
		#assert(identifier.parent == None)
		assert(identifier.parent == "/pnode-0")
		
		
	def delete_resource(self, identifier, owner, force = False):
		assert(identifier.parent == None)
		#assert(identifier.parent == "/pnode-0")
		assert(identifier.typename == "proberesource")
		self.__instances.pop(identifier.resourcename)
	
	def execute_method(self, identifier, name, *args, **kw ):
		assert(identifier.parent == None)
		assert(identifier.typename == "simpletest")
		try:
			fun = getattr(self, 'execute_%s' %(name,))
		except AttributeError:
			raise NoSuchMethodError(identifier, name)
		return fun(identifier, **kw)
	
	def execute_update(self, identifier, **kw):
		logger.debug("update method")
		self.set_configuration(identifier, kw)
		return 
	
	def execute_start(self, identifier, **kw):
		logger.debug("start method")
		return
	
	def execute_stop(self, identifier, **kw):
		logger.debug("stop method")
		return		 

