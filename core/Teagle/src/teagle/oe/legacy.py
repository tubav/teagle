'''
Created on 16.05.2011

@author: kca
'''

from ngniutils.path import path
from ngniutils.multiprocess import Lock
import re
from teagle.exc import TeagleError
from urllib2 import quote, urlopen
from abc import ABCMeta, abstractmethod

BASE_PATH = path("/var/www/panlab")

VCT2SPATEL_CONF_PATH = BASE_PATH / "teagle/teagle.commonservices/impl_spatelengine/appcommonservices/services/TeagleGen/entities/vct2spatel_conf.py"

LOCK_PATH = "/tmp/.rwlock"

URL = "http://localhost/spatelrunner/cgi-bin/clientMyRD.py?step=xdocmd&op=addPTM&siteid=teagle&appid=teagle.commonservices&serviceid=TeagleGen&clientid=&v_ptmid=%(name)s&options=d0&userId="

PTM_CONF_PATH = BASE_PATH / "teagle/teagle.ptm.%(name)s/impl_spatelengine/app%(name)s/services/%(capitalized)sGeneric/entities/%(capitalized)sGeneric_conf.py"

PTM_TEMPLATE = """
PTM_DATA_%(uppercase)s = {
    'ptmapp' : "teagle.ptm.%(name)s",
    'servicename' : '%(capitalized)sGeneric',
    'defaultparentid' : '/',
    'endpoint' : '%(endpoint)s',
    'interfacepattern' : 'generic',
    'initialresources' : {},
}

"""

class LegacyOEClient(object):
    __metaclass__ = ABCMeta
    
    @abstractmethod
    def register_ptm(self, ptm):
        raise NotImplementedError()
        
    @abstractmethod
    def register_ptm_data(self, name, ptmurl):
        raise NotImplementedError()

class OEError(TeagleError):
    pass

class LocalOEClient(object):
    def __init__(self, *args, **kw):
        super(LocalOEClient, self).__init__(*args, **kw)
        self.__lock = Lock(VCT2SPATEL_CONF_PATH)
        self.__pattern = re.compile(r"(.*}[\s^$#]+)(PTM_DICT = {)(.*)", re.M | re.DOTALL)
    
    def register_ptm(self, ptm):
        return self.register_ptm_data(ptm.commonName, ptm.url)

    def register_ptm_data(self, name, ptmurl):
        name = name.lower()        
        capitalized = name[0].upper() + name[1:]
        uppercase = name.upper()
        
        
        assert("'" not in name and "/" not in name)
        
        with self.__lock as spatelconffile:
            with open(VCT2SPATEL_CONF_PATH) as f:
                spatelconf = f.read()
                
            match = self.__pattern.match(spatelconf)
            if not match:
                raise OEError("not found")
            
            ptmconf = PTM_TEMPLATE % dict(name = name, capitalized = capitalized, uppercase = uppercase, endpoint = ptmurl)
            
            url = URL % dict(name = quote(name))
            
            result = urlopen(url)
            
            answer = result.read()
            
            result.close()
            
            if answer.find("Done") <= 0:
                print (answer)
                raise OEError("unsuccessful")
            
            urlconfpath = PTM_CONF_PATH % dict(name = name, capitalized = capitalized)
            
            with open(urlconfpath, "w") as f:
                f.write('PTM_URL = "\t%s"\n' % (ptmurl, ))
            
            spatelconffile.seek(len(match.group(1)))
            spatelconffile.truncate()
            
            spatelconffile.write(ptmconf)
            spatelconffile.write(match.group(2))
            spatelconffile.write("\n'%s': PTM_DATA_%s," % (name, uppercase))
            spatelconffile.write(match.group(3))

            
            