'''
Created on 13.08.2010

@author: kca
'''

from ptm.ResourceAdapter import AbstractResourceAdapter
from ptm import Identifier
from ptm.exc import InstanceNotFound, DuplicateNameError, ConfigurationAttributeError, NoSuchMethodError
import logging

logger = logging.getLogger("ptm")

#For this (very simple) example we will implement a basic RA that manages only onetype of resource: called "pdngw".
#The type has no further configuration and can be freely instantiated and deleted. 

#For this example we will extend AbstractResourceAdpters, which is absolutely basic.
#We'll have to do everything ourselves, some of it might seem redundant or plain odd.
#There are a number of convenience classes that provide default behaviour for a number of different scenarios.
#These will be covered later

class Pdngw(AbstractResourceAdapter):
    '''
    classdocs
    '''

    #ctor for our RA. By convention it MUST accept the manager argument.
    #please refer to http://fuhm.net/super-harmful/ if you wonder about the ctor signature / supercall
    def __init__(self, parent, manager, *args, **kw):
        super(Pdngw, self).__init__(*args, **kw)
        
        #this is where we store our instances. We simply store the names of all existing instances.
        #here, one instance ("0") already exists. The full id of this instance would be "/pdngw-0"
        self.__instances = set("0")
        
        #tell the manager to announce that we are responsible for that type "pdngw" at the root of the hierarchy
        manager.register_adapter("/pdngw", self)
        manager.register_adapter("/switch*/pdngw", self)
        
        logger.debug("---up---")
        
        
    #This will get called whenever someone requests a list of existing instances.
    #parent tells us what part of the hierarchy is requested.
    #typename tells us which types to include in the list. None means all types. 
    def list_resources(self, parent, typename):
        
        #The asserts are here only to show you what cou can rely on.
        #since we have only registered for the type "pdngw", thats the only type that can be requested
        assert(typename == "pdngw" or not typename)
        
        #our resources have no parent
#        assert(parent == None)
        
        #we have the option of returning a list of objects which represent instances (will be covered later)
        #or just a list of the exitsing IDs. For simplicity we'll do the latter.
        return [ Identifier("/pdngw-" + i) for i in self.__instances ]

    #This will be called whenever someone requests to instantiate a new instance of a certain type
    #parent_id specifies the parent of the new instance
    #name specifies a name to be given for the new resource.
    #   it can be None or empty in which case the RA must generate a unique name
    #   if a name is given, the RA _must_ adhere to it. If the name already exists, its an error.
    def add_resource(self, parent_id, name, typename, config, owner = None):
        assert(typename == "pdngw")
        
        
        if not name:
            #No name was specified. generate one.
            i = 0
            while True:
                n = str(i)
                if n not in self.__instances:
                    break
                i += 1
            name = n
        else:
            #A name was specified. Check if its still available.
            if name in self.__instances:
                raise DuplicateNameError(parent_id, typename, name)
        
        #Add the new instance
        self.__instances.add(n)
        
        #Several options here.
        #either return an object, the full identifier, or just the name
        #we are lazy and do the latter. The Framework will puzzle "/pdngw-<name>" together from it
        return name

    #tell the system if a given id exists
    def have_resource(self, identifier):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        return identifier.name in self.__instances

    #return a given resource instance.
    #since we dont have some specific implementation, it boils down to checking if the instance exists.
    def get_resource(self, identifier):
        return identifier


    #Return the config of an existing instance. 
    def get_configuration(self, identifier):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        
        if not self.have_resource(identifier):
            raise InstanceNotFound(identifier)
        
        #since our instances have no config we return an empty dict
        return {}

    #reconfigure an existing instance
    def set_configuration(self, identifier, config):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        
        #Just ignore it
        return
            

    #return a single config parameter of an existing instance
    def get_attribute(self, identifier, name):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        
        #Our instances have no config, so this is always an error
        raise ConfigurationAttributeError(name)

    #set a single config parameter
    def set_attribute(self, identifier, name, value):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        
        #Our instances have no config, so this is always an error
        raise ConfigurationAttributeError(name)

    #delete an existing instance
    def delete_resource(self, identifier):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        self.__instances.remove(identifier.resourcename)

    def execute_method(self, identifier, name, *args, **kw ):
#        assert(identifier.parent == None)
        assert(identifier.typename == "pdngw")
        try:
            fun = getattr(self, 'execute_%s' %(name,))
        except AttributeError:
            raise NoSuchMethodError(identifier, name)
        return fun(identifier, **kw)
    
    def execute_update(self, identifier, **kw):
	   logger.debug("update method")
	   return 
    
    def execute_start(self, identifier, **kw):
        logger.debug("start method")
        return
    
    def execute_stop(self, identifier, **kw):
        logger.debug("stop method")
        return		 
        
