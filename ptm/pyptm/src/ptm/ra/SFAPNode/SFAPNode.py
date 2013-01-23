'''
Created on Apr 25, 2012

@author: gca
'''
from ptm import Identifier
from ptm.exc import InstanceNotFound, DuplicateNameError, ConfigurationAttributeError
import logging
from ptm.ResourceAdapter import AbstractResourceAdapter
from sfa.client.sfi import Sfi
from sfa.client import sfi
from sfa.rspecs.rspec import RSpec
from sfa.util.xml import XML
from sfa.rspecs.rspec_converter import RSpecConverter

import random
#from sfa.examples.miniclient import list_resources

class SFAPNode(AbstractResourceAdapter):
    '''
    This class represents a resouce adapter for interopereting with other SFA testbeds
    '''
    
    
    def __init__(self, manager, *args, **kw):
        '''
        Constructor
        '''
        super(SFAPNode, self).__init__(*args, **kw)
        self.__instances = set()
        global logger
        logger = logging.getLogger("SFAPNode")
        manager.register_adapter("/SFAPNode", self)
        self.__manager = manager
        version_dict = {'type':'SFA', 'version':'1'}
        self.__rspecs = {}
        self.__sfi = Sfi()
        self.__sfi.read_config()
        self.__sfi.bootstrap()
        self.__credentials = [ self.__sfi.my_credential_string ]
        self.__options = {}
        self.__options[ 'geni_rspec_version' ]  = version_dict
        
        #fill self.__instances
#        temp = list_resources(None, "SFAPNode")
        

    def list_resources(self, parent, typename):
        assert(typename == "SFAPNode" or not typename)
        assert(parent == None)
        list_resources = self.__sfi.bootstrap.server_proxy(self.__sfi.sm_url).ListResources(self.__credentials,self.__options)
        self.__sfi.bootstrap.server_proxy(self.__sfi.sm_url)
        logger.debug( "ListResources at %s returned : %s"%(self.__sfi.sm_url,list_resources['value']))
        rspec = RSpec(list_resources['value'])
        nodes = rspec.version.get_nodes()
        networks = rspec.version.get_networks()
        for node in nodes:
            version = {'namespace': None, 'version': '1', 'type': 'SFA', 'extensions': [], 'schema': None}
            rspec_tmp = RSpec(version = version)
            rspec_tmp.version.add_network(networks[0]['name'])	
            rspec_tmp.version.add_nodes([node])
            component_name = ''
            component_name = node['component_name']

            rspec_tmp.version.add_slivers(hostnames = [component_name])
            rspec_tmp_xml = RSpecConverter.to_sfa_rspec(rspec_tmp.toxml())
 
            self.__instances.add(component_name)
            rspec_xml = {}
            rspec_xml['xmlRspec'] = rspec_tmp_xml
            self.__rspecs['/SFAPNode-%s' %component_name] = rspec_xml
            logger.debug("PNodes %s " %component_name)

        logger.debug(self.__rspecs)
        return [ Identifier("/SFAPNode-%s"  %i) for i in self.__instances ]
    

    
    def add_resource(self, parent_id, name, typename, config, owner = None):
        return random.choice(self.__instances)
    

    def have_resource(self, identifier):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAPNode")
        return identifier.name in self.__instances


    def get_resource(self, identifier):
        return identifier


    def get_configuration(self, identifier):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAPNode")

        if not self.have_resource(identifier):
            raise InstanceNotFound(identifier)
        
        return self.__rspecs[identifier]




    def set_configuration(self, identifier, config):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAPNode")
        config
        return


    def get_attribute(self, identifier, name):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAPNode")
        raise ConfigurationAttributeError(name)


    def set_attribute(self, identifier, name, value):
        assert(identifier.parent == None)
                                 
                                     
    def delete_resource(self, identifier, owner, force = False):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAPNode")
        self.__instances.pop(identifier.resourcename)

    def _get_rspec(self, identifier):
        return self.rspecs[identifier]

    def execute_method(self, identifier, name, *args, **kw ):
        assert(identifier.parent == None)
        assert(identifier.typename == "SFAPNode")
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
 
