'''
Created on 02.04.2011

@author: kca
'''

from abc import ABCMeta, abstractmethod

class TeagleRepository(object):
    __metaclass__ = ABCMeta
    
    @abstractmethod
    def list_entities(self, klass, order_by = None):
        raise NotImplementedError()
    
    @abstractmethod
    def get_entity(self, klass, id):
        raise NotImplementedError()
    
    @abstractmethod
    def persist(self, entity):
        raise NotImplementedError()
    
    