'''
Created on 12.05.2012

@author: gca
'''

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import DuplicateNameError, ConfigurationAttributeError, InstanceNotFound, NoSuchMethodError
import logging
import paramiko
logger = logging.getLogger("IperfAdapter")
'''
Iperf Adapter for demo, to be used only on a fedora machine
'''
class VmAdapter(AbstractResourceAdapter):
    '''
    classdocs
    '''


    def __init__(self, manager, *args, **kw):
        super(VmAdapter, self).__init__(*args, **kw)
        self.__instances = set("0")
        self.__manager = manager
        self.__manager = manager.register_adapter("/iperfadapter", self)
        self.__install_command_fed = 'yum install -y iperf screen'
        self.__install_command_deb = 'apt-get install -y iperf screen'
        logger.debug("---up---")
        
    def list_resources(self, parent, typename):
        return [ Identifier("/iperfadapter-" + i) for i in self.__instances ]

    def add_resource(self, parent_id, name, typename, config, owner = None):
        assert(typename == "iperfadapter")
        install = False
        iperf_type = 'server'
        #Getting the configuration paramter of the parent resource, in this case an RSpec
        resource = "empty"
        ostype = 'debian'
        username = 'root'

        if config.has_key("install"):
            install = True
        if config.has_key("iperf_type"):
            iperf_type = config['iperf_type']
        if config.has_key("server_address"):
            server_address = config['server_address']    
        if config.has_key("ostype"):
            ostype = config['ostype']    
             

        if config.has_key("resource"):
            resource = config["resource"]

        if config.has_key("target"):
            if resource != "empty":
                target = config["target"]
                configuration = target.get_adapter().get_configuration(resource)
                #hostname = configuration["hostname"]
                public_ip = configuration["public_ip"]
                password = configuration['password']
                if(configuration.has_key('username')):
                    username = configuration['username']


        # Initialize Client and Channel.
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        client.connect(public_ip,username=username,password=password)
        if(ostype == 'fedora'):
            install_command = self.__install_command_fed
        else:
            install_command = self.__install_command_deb
            
        if(iperf_type == 'server'):
            start_command = 'screen -m -d -S iperf /bin/bash -c "/usr/bin/iperf -s"'
        else:
            start_command = 'screen -m -d -S iperf /bin/bash -c "while true;do /usr/bin/iperf -c %s; done"'%server_address   
            
        # Logging in.
        channel = client.invoke_shell()
        self.wait_for_new_execute(channel,password,username)
        if(username != 'root'):
            install_command = 'sudo %s'%install_command

        if install:
            # Install screen and iperf
            self.execute_command(channel,install_command+'\n',password,username)
            
        self.execute_command(channel, start_command+'\n', password, username)

        
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
        
        self.__instances.add(name)
        
        return name

    def get_resource(self, identifier):
        return identifier

    def have_resource(self, identifier):
        assert(identifier.parent == None)
        assert(identifier.typename == "iperfadapter")
        return identifier.name in self.__instances

    def get_configuration(self, identifier):
        assert(identifier.parent == None)
        assert(identifier.typename == "iperfadapter")
        return {}


    def set_configuration(self, identifier, config):
        assert(identifier.parent == None)
        assert(identifier.typename == "iperfadapter")
        
        if not self.have_resource(identifier):
            raise InstanceNotFound(identifier)
        
        self.__instances[identifier.resourcename] = config            

    def get_attribute(self, identifier, name):
        assert(identifier.parent == None)
        assert(identifier.typename == "iperfadapter")
        
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
        assert(identifier.typename == "iperfadapter")
        
        try:
            instance = self.__instances[identifier.resourcename]
        except KeyError:
            raise InstanceNotFound(identifier)
        
        if name not in instance:
            raise ConfigurationAttributeError(name)
        
        instance[name] = value

    def delete_resource(self, identifier, owner, force = False):
        assert(identifier.parent == None)
        assert(identifier.typename == "iperfadapter")
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
    
