'''
Created on Apr 25, 2012

@author: gca
'''

import os, os.path
from ptm import Identifier
from ptm.exc import InstanceNotFound, DuplicateNameError, ConfigurationAttributeError
import logging
from ptm.ResourceAdapter import AbstractResourceAdapter
from sfa.util.xrn import hrn_to_urn
from sfa.rspecs.rspec_converter import RSpecConverter
from sfa.client.client_helper import pg_users_arg, sfa_users_arg
from sfa.client.return_value import ReturnValue
from sfa.util.cache import Cache
from sfa.client.sfaserverproxy import SfaServerProxy, ServerException

from sfa.client.sfi import Sfi
from sfa.client import sfi
from sfa.rspecs.rspec import RSpec
from sfa.util.xml import XML
from sfa.util.cache import Cache
import paramiko
import time
import random

from sfa.util.xml import XpathFilter
from sfa.storage.record import SfaRecord

import uuid
#from sfa.examples.miniclient import slice_hrn
from logging import exception
def unique_call_id(): return uuid.uuid4().urn

class SFAVNode(AbstractResourceAdapter):
    '''
    This class represents a resouce adapter for interopereting with other SFA testbeds
    '''
    class DummyOptions:
        pass
   
 
    def __init__(self, manager, *args, **kw):
        '''
        Constructor
        '''
        super(SFAVNode, self).__init__(*args, **kw)
        self.__instances = set()
        manager.register_adapter("/sfavnode*", self)
        manager.register_adapter("/SFAPNode*/sfavnode", self)

        #version_dict = {'type':'SFA', 'version':'1'}
        #version_dict = {'type':'SFA', 'version':'1','schema':None,'namespace':None,'extensions':[]}
        self.__manager = manager
        self.__sfi = Sfi()
        self.__sfi.read_config()
        self.__sfi.bootstrap()
        self.__credentials = [ self.__sfi.my_credential_string ]
        self.__bootstrap = self.__sfi.bootstrap.server_proxy(self.__sfi.sm_url)
#        self.__options = {}
#        self.__options[ 'geni_rspec_version' ]  = version_dict
        
        self.__config = {}

        global logger
        logger = logging.getLogger("SFAVNode")



    #
    # Management of the servers
    # 

    def registry (self):
    #def registry (self, sfi):
        # cache the result
        if not hasattr (self, 'registry_proxy'):
            self.logger.info("Contacting Registry at: %s"%self.__sfi.reg_url)
            #self.logger.info("Contacting Registry at: %s"%sfi.reg_url)
            self.registry_proxy = SfaServerProxy(self.__sfi.reg_url, self.__sfi.private_key, self.__sfi.my_gid)
            #self.registry_proxy = SfaServerProxy(sfi.reg_url, sfi.private_key, sfi.my_gid)
                                                   
        return self.registry_proxy
        
        
    def list_resources(self, parent, typename):
        assert(typename == "SFAVNode" or not typename)
        logger.debug('List of running instances %s' %self.__instances)
        return [ Identifier("/SFAVNode-" + i) for i in self.__instances ]
    

    
    def add_resource(self, parent_id, name, typename, config, owner = None):
        if not parent_id:
            raise ValueError("Need a parent")
        
        #Creating a new SFI client object
#        sfi = Sfi()
#        sfi.read_config()
#        sfi.bootstrap()
#        credentials = [ sfi.my_credential_string ]
#        bootstrap = sfi.bootstrap.server_proxy(sfi.sm_url)
        
        #Create new options
        version_dict = {'type':'SFA', 'version':'1','schema':None,'namespace':None,'extensions':[]}
        options = {}
        options[ 'geni_rspec_version' ]  = version_dict
           
        #Getting the configuration paramter of the parent resource, in this case an RSpec
        parent = self.__manager.get_resource(parent_id)
        pconfig = parent.get_configuration()
        rspec = pconfig['xmlRspec']
        logger.debug('RSpec of the parent %s'%rspec)

        #Saving the hostname of the parent in order to retrieve the slice later
        hostname = self.fetch_tag(rspec,'hostname')
        logger.debug('Saved the hostname %s'%hostname)        

        #Getting the vctname and creating the slice_hrn        
        slice_hrn = 'raven.fts.%s' %config['vctname']
        slice_urn = hrn_to_urn(slice_hrn, 'slice')
        logger.info('Creating or updating a slice with the name %s'%slice_hrn)

        #Preparing the server_proxy object and getting the server version
        result = self.__bootstrap.GetVersion()
        server_version= ReturnValue.get_value(result)
        logger.debug('Received server version %s'%server_version)	


        #Creating the slice record dict or string
        #recorddict = dict({'hrn': slice_hrn,
        #'url': 'http://planet-lab.org',
        #'type': 'slice',
        #'researcher': ['teagle.teagle.teagle'],
        #'description': 'Teagle slice'})
        slice_str = '<record description="Teagle Slice4" hrn="%s" type="slice" url="http://planet-lab.org"><researcher>teagle.teagle.teagle</researcher></record>' %slice_hrn
        slicerecord = SfaRecord(string = slice_str).as_dict()
        logger.debug('Prepared a slice record to add to the registry %s'%slice_str)
 
        
        #Retrieving the credential of the authority
        auth_cred = self.__sfi.bootstrap.authority_credential_string (self.__sfi.authority)
        #auth_cred = sfi.bootstrap.authority_credential_string (sfi.authority)
        logger.debug('Authority %s credentials %s'%(self.__sfi.authority, auth_cred,))
        #logger.debug('Authority %s credentials %s'%(sfi.authority, auth_cred,))

        #Trying to create the slice
        try:
            records = self.registry().Register(slicerecord, auth_cred)
            #records = self.registry(sfi).Register(slicerecord, auth_cred)
        except ServerException:
            logger.debug("Slice already existing")
            pass
        
        #Saving the slice credential
        creds = [self.__sfi.slice_credential_string(slice_hrn)]
        #creds = [sfi.slice_credential_string(slice_hrn)]
        logger.debug('The slice credential: %s'%creds)
        
        
        # users
        # need to pass along user keys to the aggregate.
        # users = [
        #  { urn: urn:publicid:IDN+emulab.net+user+alice
        #    keys: [<ssh key A>, <ssh key B>]
        #  }]
        users = []
        slice_records = self.registry().Resolve(slice_urn, [self.__sfi.my_credential_string])
        #slice_records = self.registry(sfi).Resolve(slice_urn, [sfi.my_credential_string])
        if slice_records and 'researcher' in slice_records[0] and slice_records[0]['researcher']!=[]:
            slice_record = slice_records[0]
            user_hrns = slice_record['researcher']
            user_urns = [hrn_to_urn(hrn, 'user') for hrn in user_hrns]
            user_records = self.registry().Resolve(user_urns, [self.__sfi.my_credential_string])
            #user_records = self.registry(sfi).Resolve(user_urns, [sfi.my_credential_string])

            if 'sfa' not in server_version:
                users = pg_users_arg(user_records)
                rspec = RSpec(rspec)
                rspec.filter({'component_manager_id': server_version['urn']})
                rspec = RSpecConverter.to_pg_rspec(rspec.toxml(), content_type='request')
            else:
                users = sfa_users_arg(user_records, slice_record)


        logger.debug('Creating the sliver using the RSpec %s'%rspec)
        time.sleep(5)

        #Creating the sliver
        logger.debug("###################slice_urn: %s"%(slice_urn,))
        logger.debug("###################creds: %s"%(creds,))
        logger.debug("###################rspec: %s"%(rspec,))
        logger.debug("###################users: %s"%(users,))
        logger.debug("###################options: %s"%(options,))
        result = self.__bootstrap.CreateSliver(slice_urn, creds, rspec, users, options)
        #result = bootstrap.CreateSliver(slice_urn, creds, rspec, users,options)
        value = ReturnValue.get_value(result)
        logger.debug("###################return value: %s"%(value, ))

        options['geni_slice_urn'] = hrn_to_urn(slice_hrn, 'slice')
        options['call_id'] = unique_call_id()

        slice_credentials = self.__sfi.slice_credential_string(slice_hrn)
        #slice_credentials = sfi.slice_credential_string(slice_hrn)

        list_resources = self.__bootstrap.ListResources(slice_credentials, options)
        #list_resources = bootstrap.ListResources(slice_credentials,options)
        #self.__sfi.bootstrap.server_proxy(self.__sfi.sm_url)
        logger.debug( "ListResources of slice %s returned : %s"%(slice_hrn,list_resources['value']))
        slice_rspec = RSpec(list_resources['value'])
        nodes = slice_rspec.version.get_nodes()
        for node in nodes:
            component_name = ''
            component_name = node['component_name']
            tags = self.convert_tags(node['tags'])
            node_hostname = tags['hostname']
            
            if hostname in node_hostname:
                #store the information of the specific sliver in the config 
                
                ###XXX change the username with the real sliver name
                sliver_name = self.fetch_username(slice_rspec)
                ip = self.fetch_ip(slice_rspec)
                name = '%s.%s'%(hostname,sliver_name,)
                conf = {'public_ip':ip,'username':sliver_name,'password':None}
                #self.__config['/SFAVNode-%s' %name] = conf
                self.__config[name] = conf
        logger.info('Adding the resource instance %s '%name)
        time.sleep(10) 
        if not self.poll_machine(ip, sliver_name):
            raise Exception('Connection with the sliver not possible')

        
        
        
        if name in self.__instances:
                raise DuplicateNameError(parent_id, typename, name)
        self.__instances.add(name)

        return name
    

    def have_resource(self, identifier):
        assert(identifier.typename == "sfavnode")
        logger.debug('identifier %s'%identifier)
        return identifier.name in self.__instances


    def get_resource(self, identifier):
        return identifier


    def get_configuration(self, identifier):
        logger.debug("identifier %s and resource name %s"%(identifier,identifier.resourcename,))
        assert(identifier.typename == "sfavnode")

        if not self.have_resource(identifier):
            raise InstanceNotFound(identifier)

        return self.__config[identifier.resourcename]


    def set_configuration(self, identifier, config):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAVNode")
        return


    def get_attribute(self, identifier, name):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAVNode")
        raise ConfigurationAttributeError(name)


    def set_attribute(self, identifier, name, value):
        assert(identifier.parent == None)
                                 
#************************
    def delete_resource(self, identifier, owner, force = False):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAVNode")
        #TODO create the method for removing this sliver instanciated
        
        #Create new options
        version_dict = {'type':'SFA', 'version':'1','schema':None,'namespace':None,'extensions':[]}
        options = {}
        options[ 'geni_rspec_version' ]  = version_dict
        
        
        
        self.__instances.pop(identifier.resourcename)


    def get_cached_server_version(self, server, options):
        # check local cache first
        cache = None
        version = None 
        cache_file = os.path.join(options.sfi_dir,'sfi_cache.dat')
        cache_key = server.url + "-version"
        try:
            cache = Cache(cache_file)
        except IOError:
            cache = Cache()
            self.logger.info("Local cache not found at: %s" % cache_file)

        if cache:
            version = cache.get(cache_key)

        if not version: 
            result = server.GetVersion()
            version= ReturnValue.get_value(result)
            # cache version for 20 minutes
            cache.add(cache_key, version, ttl= 60*20)
            self.logger.info("Updating cache file %s" % cache_file)
            cache.save_to_file(cache_file)

        return version  

    @staticmethod
    def default_sfi_dir ():
        if os.path.isfile("./sfi_config"): 
            return os.getcwd()
        else:
            return os.path.expanduser("~/.sfi/")


    def poll_machine(self, ip, uname, passwd = None):
        sshclient = paramiko.SSHClient()
        sshclient.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        counter = 0
        connection = False
        while(counter < 100):
            counter = counter + 1
            try:
                sshclient.connect(ip, username=uname, password=None)
                connection = True
#                logger.debug("connection possible")
                break
            except:
#                logger.debug("no connection possible")
                time.sleep(10)
        sshclient.close()
        return connection

    def convert_tags(self, tags):
        tags_dict = {}
        for tag in tags:
            tags_dict[tag['tagname']] = tag['value'] 
        return tags_dict
    
    def fetch_tag(self, rspec_string, tag):
        hostnames = []
        rspec = RSpec(rspec_string)
        nodes = rspec.version.get_nodes();
        for node in nodes:
            tags = self.convert_tags(node['tags'])
            hostnames.append(tags[tag])
        
        #if len(hostnames) > 0:#????
            
        return hostnames[0]
    
    def fetch_ip(self, rspec):
        ips = []
        nodes = rspec.version.get_nodes();
        for node in nodes:
            interfaces = node['interfaces']
            for interface in interfaces:
                ips.append(interface['ipv4'])
        #if len(hostnames) > 0:#????
        return ips[0]

    def fetch_username(self, rspec):
        username = ''
        
        filter = {}
        xpath = '//node%s | //default:node%s' % (XpathFilter.xpath(filter), XpathFilter.xpath(filter))
        node_elems = rspec.xml.xpath(xpath)
        for node_elem in node_elems:
            for services_elem in node_elem.xpath('./default:services | ./services'):
                for login_elem in services_elem.xpath('./default:login | ./login'):
                    username = login_elem.attrib['username']
        return username
    
    def execute_method(self, identifier, name, *args, **kw ):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAVNode")
        try:
            fun = getattr(self, 'execute_%s' %(name,))
        except AttributeError:
            raise NoSuchMethodError(identifier, name)
        return fun(identifier,**kw)


    def execute_update(self, identifier, **kw):
        logger.debug("update method")
        return

    def execute_start(self, identifier, **kw):
        logger.debug("start method")
        return

    def execute_stop(self, identifier, **kw):
        logger.debug("stop method")
        return
 
