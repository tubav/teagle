'''
Created on Sep 26, 2012

@author: gca
'''
import oca
import os, subprocess, shlex, paramiko, sys
from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import DuplicateNameError, ConfigurationAttributeError, InstanceNotFound, NoSuchMethodError
import logging
import time
logger = logging.getLogger("OpenImsAdapter")

class OpenImsAdapter(AbstractResourceAdapter):
    '''
    classdocs
    '''
    def __init__(self, manager, *args, **kw):
        super(OpenImsAdapter, self).__init__(*args, **kw)
        self.__instances = set("0")
        self.__manager = manager
        self.__manager = manager.register_adapter("/fokusopenims", self)
        self.__template = open('/home/teagle/opennebula/ims.template.one','r').read()
        self.__client = oca.Client(address='http://192.168.144.22:2633/RPC2')
        self.__pending_vms = {}
        self.__vm_pool =  oca.VirtualMachinePool(self.__client)
        self.__vm_pool.info(-3,-1,-1,-2)
	self.__username = 'root'
	self.__password = 'root'
	logger.debug("---vm adapter up---")
        
    def list_resources(self, parent, typename):
        return [ Identifier("/fokusopenims-" + i) for i in self.__instances ]

    def add_resource(self, parent_id, name, typename, config, owner = None):
        assert(typename == "fokusopenims")
        
        print "creating a vm with the template"
	print self.__template
        vm_id = oca.VirtualMachine.allocate(self.__client,self.__template)
      
        self.__vm_pool.info(-3,-1,-1,-2)
	vm = self.__vm_pool.get_by_id(vm_id)
      	vm.deploy(3) 
        if not name:
            i = 0
            while True:
                n = str(i)
                if n not in self.__instances:
                    break
                i += 1
            name = n
        else:
            if name in self.__instances:
                raise DuplicateNameError(parent_id, typename, name)
        vmConf={}
	vmConf['vm_name'] = vm.name    
	vmConf['vm_id'] = vm.id    
        vmConf['vm_ip'] = '192.168.144.42'
	# Initialize Client and Channel.
	self.__poll_machine()

        self.__pending_vms['/fokusopenims-%s' %name] = vmConf

        self.__instances.add(name)
 
        
	return name

    def get_resource(self, identifier):
        return identifier

    def have_resource(self, identifier):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
        return identifier.name in self.__instances

    def get_configuration(self, identifier):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
        if not self.have_resource(identifier):
            raise InstanceNotFound(identifier)
        return self.__pending_vms[identifier]

    def set_configuration(self, identifier, config):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
        
        if not self.have_resource(identifier):
            raise InstanceNotFound(identifier)
        
        self.__instances[identifier.resourcename] = config            

    def get_attribute(self, identifier, name):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
        
        try:
            instance = self.__instances[identifier.resourcename]
        except KeyError:
            raise InstanceNotFound(identifier)
        
        try:
            return instance[name]
        except KeyError:
            raise ConfigurationAttributeError(name)

    def set_attribute(self, identifier, name, value):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
        
        try:
            instance = self.__instances[identifier.resourcename]
        except KeyError:
            raise InstanceNotFound(identifier)
        
        if name not in instance:
            raise ConfigurationAttributeError(name)
        
        instance[name] = value

    def delete_resource(self, identifier, owner, force = False):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
        self.__instances.pop(identifier.resourcename)



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

    def __poll_machine(self, ip='192.168.144.42', uname='ubuntu', passwd='ubuntu' ):
        sshclient = paramiko.SSHClient()
        sshclient.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        counter = 0
        connection = False
        while(counter < 100):
            counter = counter + 1
            try:
                sshclient.connect(ip, username=uname, password=passwd)
                connection = True
#                logger.debug("connection possible")
                break
            except:
#                logger.debug("no connection possible")
                time.sleep(10)
        sshclient.close()
        return connection


    def execute_method(self, identifier, name, *args, **kw ):
        assert(identifier.parent == None)
        assert(identifier.typename == "fokusopenims")
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
        vm_conf = self.__pending_vms[identifier]

        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ip = vm_conf['vm_ip']
        client.connect(ip,username=self.__username,password=self.__password)

        # Logging in.
        channel = client.invoke_shell()
        self.wait_for_new_execute(channel,self.__password,self.__username) 
	start_cmd = '/opt/OpenIMSCore/openimscore.start.sh'
        self.execute_command(channel,start_cmd+'\n',self.__password,self.__username)

	return
    
    def execute_stop(self, identifier, **kw):
        logger.debug("stop method")
	vm_conf = self.__pending_vms[identifier]
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ip = vm_conf['vm_ip']

	client.connect(ip,username=self.__username,password=self.__password)

        # Logging in.
        channel = client.invoke_shell()
        self.wait_for_new_execute(channel,self.__password,self.__username)
        stop_cmd = '/opt/OpenIMSCore/openimscore.kill.sh'


        self.execute_command(channel,stop_cmd+'\n',self.__password,self.__username)


	return         
    
