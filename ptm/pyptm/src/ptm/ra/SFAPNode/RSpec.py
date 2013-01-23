'''
Created on May 7, 2012

@author: gca
'''
from ptm.Resource import Resource

class RSpec(Resource):
    '''
    classdocs
    '''
    
    def __init__(self, hostname, ip, hrn, adapter, identifier = None, parent_id = None, type = None, name = None):
        Resource.__init__(self, adapter, identifier, parent_id, type, name)
        if not isinstance(hostname, basestring):
            raise TypeError("Need a string for Hostname: " + str(hostname))
    
    
    