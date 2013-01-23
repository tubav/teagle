#! /usr/bin/env python
'''
Created on 18.07.2010

@author: kca
'''

from util import is_list

__all__ = ("DELETED", "UPDATED", "CHANGED")

class NotificationId(int):
    __names = {
        1: "deleted",
        2: "updated",
        
        3: "changed"
    }
    __last = 2
    
    def __new__(cls, i, *args, **kw):
        if isinstance(i, NotificationId):
            return i
        
        if is_list(i):
            i = reduce(int.__or__, map(int, i), 0)
        else:
            i = int(i)
            
        #TODO: check val
        return super(NotificationId, cls).__new__(cls, i, *args, **kw)
        
    def get_name(self):
        return NotificationId.__names[self]
    name = property(get_name)
    
    def get_ids(self):
        try:
            return self.__pieces
        except AttributeError:
            self.__pieces = self.__make_pieces()
            return self.__pieces
    ids = property(get_ids)
    
    def __make_pieces(self):
        i = 1
        pieces = []
        while i <= NotificationId.__last:
            if self & i:
                pieces.append(NotificationId(i))
            i = i << 1
        return tuple(pieces)
    
    def get_is_compound(self):
        return len(self.get_ids) > 1
    is_compound = property(get_is_compound)
    
    def __contains__(self, o):
        try:
            return self & int(o)
        except ValueError:
            return False

    def __iter__(self):
        return self.__get_pieces().__iter__()
    
    def __str__(self):
        return "%d (%s)" % (self, self.name)
    def __unicode__(self):
        return unicode(str(self))

DELETED = NotificationId(1)
UPDATED = NotificationId(2)

CHANGED = NotificationId(3)
