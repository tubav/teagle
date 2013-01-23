'''
Created on 11.08.2010

@author: kca
'''

from fcntl import lockf, LOCK_UN, LOCK_EX, LOCK_SH

class FLock(object):
    def __init__(self, filename, mode = LOCK_EX, *args, **kw):
        super(FLock, self).__init__(*args, **kw)
        
        if mode not in (LOCK_EX, LOCK_SH):
            raise ValueError(mode)
        
        self.__file = open(filename, mode == LOCK_EX and "a" or "r")
        self.__lock = lockf(self.__file, mode)
        
    def close(self):
        try:
            lockf(self.__file, LOCK_UN)
        finally:
            self.__file.close()
        
    def __exit__(self, exc_type, exc_value, traceback):
        self.close()
        