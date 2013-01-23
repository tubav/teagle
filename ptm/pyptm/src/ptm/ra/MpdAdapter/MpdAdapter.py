'''
Created on 14.02.2011

@author: nil
'''

'''
This RA should deploy a ns-3 component as a regular instance with a given ns-3 script (currently the script has to be written in Python).
'''

from ptm.Resource import Resource

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import InstanceNotFound, DuplicateNameError, ConfigurationAttributeError
import logging

from path import path as Path

import subprocess, shlex, paramiko, time

logger = logging.getLogger("ptm")

	
class MpdAdapter(AbstractResourceAdapter):
	'''
	classdocs
	'''
	def __init__(self, manager, *args, **kw):
		super(MpdAdapter, self).__init__(*args, **kw)
		self.__instances = set("0")
		manager.register_adapter("/pnode-0/mpd", self)
		self.manager = manager
		self.music_db = music_db
		logger.debug("---up---")
		


	def list_resources(self, parent, typename):
        	assert(typename == "mpd" or not typename)
		#assert(parent == None)
		#return [ Identifier("/mpd-" + i) for i in self.__instances ]
		return [ Identifier("/mpd-0")  ]

	def add_resource(self, parent_id, name, typename, config, owner = None):
		assert(typename == "mpd")
		if not parent_id:
			raise ValueError("Need a perant")
		
		parent = self.manager.get_resource(parent_id)

		if not config.has_key("music_db"):	
			raise Exception("no music_db given"):
		
		ref_ip = config["music_db"].get_attribute("public_ip")
		ref_user = config["music_db"].get_attribute("username")
		ref_pass = config["music_db"].get_attribute("password")

		client = paramiko.SSHClient()
                client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
                client.connect("xxx.xxx.xxx.xxx", username = "root", password = "password")
		x, y, z = client.exec_command("ps aux | grep mpd")
		tmp_ = y.readlines()
		res_ = ""
		for elem in tmp_:
			res_ = res_ + elem
		if 'mpd.conf' in res:
			return Identifier(parent_id) / "mpd-0"
		sftp = client.open_sftp()
		sftp.put("/opt/ptm/ra/MpdAdapter/mpd.conf", "/etc/mpd.conf", callback=None)
		logger.debug("executing cmd.")
		a, b, c = client.exec_command("aptitude -y install mpd")
		time.sleep(20)
		#client.exec_command("/etc/init.d/mpd stop")
		#time.sleep(5)
		#logger.debug("executed cmd.")
		#sftp = client.open_sftp()
		#sftp.put("/opt/ptm/ra/MpdAdapter/mpd.conf", "/etc/mpd.conf", callback=None)
		#h, i, j = client.exec_command("/etc/init.d/mpd start")
		d, e, f = client.exec_command("ps aux | grep mpd")
		tmp = e.readlines()
		res = ""
		for elem in tmp:
			res = res + elem
		if not 'mpd.conf' in res:
			raise Exception("mpd isn't running.")
		
		l, m, n = client.exec_command("whereis sshfs")
		cmd = "echo %s | sshfs %s@%s:/tmp/Music /tmp/Music -o workaround=rename -o password_stdin" %(ref_pass, ref_user, ref_ip)	
		if not m.readlines() == "sshfs":
			client.exec_command("aptitude -y install sshfs")
			client.exec_command(cmd)
		else:
			client.exec_command(cmd)
			
		#print e.readlines().find("mpd.conf")
		#if not 'mpd.conf' in e.readlines():
		#	raise Exception("mpd isn't running.")
		
		client.close()
		logger.debug("client closed")
		self.__instances.add(name)
		return Identifier(parent_id) / "mpd-0"

			
	def have_resource(self, identifier):
		assert(identifier.typename == "mpd")
		return identifier.name in self.__instances

    
	def get_resource(self, identifier):
		return identifier
		

	def get_configuration(self, identifier):
		assert(identifier.typename == "mpd")
        
		if not self.have_resource(identifier):
			raise InstanceNotFound(identifier)

		return {"http_port": "9876", "remote_port": "6600"}

	def set_configuration(self, identifier, config):
		assert(identifier.parent == None)
		assert(identifier.typename == "mpd")
		return
            

	def get_attribute(self, identifier, name):
		assert(identifier.parent == None)
		assert(identifier.typename == "mpd")
		raise ConfigurationAttributeError(name)


	def set_attribute(self, identifier, name, value):
		assert(identifier.parent == None)
		assert(identifier.typename == "mpd")
		raise ConfigurationAttributeError(name)


	def delete_resource(self, identifier, owner, force = False):
		assert(identifier.parent == None)
		assert(identifier.typename == "mpd")
		self.__instances.pop(identifier.resourcename)
