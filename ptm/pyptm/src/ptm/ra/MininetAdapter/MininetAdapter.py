'''
Created on 07.10.2010

@author: kca
'''

'''
Created on 13.08.2010

@author: kca
'''

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import DuplicateNameError, ConfigurationAttributeError, InstanceNotFound
import logging

logger = logging.getLogger("ptm")

class MiniNetAdapter(AbstractResourceAdapter):
    '''
    classdocs
    '''


    def __init__(self, manager, *args, **kw):
        super(MiniNetAdapter, self).__init__(*args, **kw)
        self.__id = "/mininet-0"
        manager.register_adapter(self.__id, self)
        
    def list_resources(self, parent, typename):
        return [ Identifier(self.__id) ]

    def add_resource(self, parent_id, name, typename, config, owner = None):
        raise Exception("Adding mininets is not supported")

    def get_resource(self, identifier):
        return identifier

    def have_resource(self, identifier):
        return identifier == self.__id

    def get_configuration(self, identifier):
        return {}

    def set_configuration(self, identifier, config):
        return 

    def get_attribute(self, identifier, name):
        raise ConfigurationAttributeError(name)

    def set_attribute(self, identifier, name, value):
        raise ConfigurationAttributeError(name)

    def delete_resource(self, identifier, owner, force = False):
        raise Exception("Deleting mininets is not supported")

    
        