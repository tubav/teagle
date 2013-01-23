'''
Created on 22.06.2011

@author: kca
'''

#! /usr/bin/env python

#test
from ptm.Resource import Resource
from ptm.Identifier import Identifier
from ptm.ResourceAdapter import ResourceAdapter, MangleConfigMixin, AbsoluteParentMixin
import urllib
import shelve
import logging
import sys
import threading

logger = logging.getLogger("ptm")

class PythonResourceAdapter(Resource):
    def __init__(self, config, *args, **kw):
        super(PythonResourceAdapter, self).__init__(parent = "pyromanager-0", type = "pythonresourceadapter", *args, **kw)

        config.pop("parent", None)

        self.config = config

    def _get_configuration(self):
        return self.config
    
class PyroManager(Resource):
    def __init__(self, adapter, *args, **kw):
        super(PyroManager, self).__init__(adapter, type = "pyromanager", name = "0", *args, **kw)
        self.config = {}

    def _get_configuration(self):
        return self.config
    

class ManagerAdapter(AbsoluteParentMixin, MangleConfigMixin, ResourceAdapter):
    def __init__(self, manager, parent, filename = None, *args, **kw):
        super(ManagerAdapter, self).__init__(keys = ("adapter_parent", "adapter_class"), manager = manager, parent = parent, *args, **kw)

        if filename is None:
            filename = self.get_homedir() / (urllib.quote_plus(unicode(self.parent_id)) + "_adapters.db")

        self.__adapters = shelve.open(filename)
        self.__running = {}
        self.__lock = threading.Lock()

        if "order" not in self.__adapters:
            self.__adapters["order"] = []

        self.__load_adapters()

        self.register("pyromanager*")

    def mangle_config(self, config):
        config = super(ManagerAdapter, self).mangle_config(config)
        config["parent"] = config.pop("adapter_parent")
        return config

    def _add_resource(self, parentId, name, typename, config):
        if typename != "pythonresourceadapter":
            raise TypeError("Can not deploy type: %s here " % (typename, ))
        assert(parentId == self.parent_id / "pyromanager-0")

        classname = unicode(config.pop("adapter_class"))
        self.__check_class(classname)
        config.pop(name, None)

        parent = config.get("parent") or "/"
        parent = unicode(Identifier(parent))
        if name is None:
            name = str(urllib.quote_plus(parent + u'#' + classname))

        with self.__lock:
            if name in self.__adapters:
                raise ValueError("Name '%s' already exists")

            self.__load_adapter(name, classname, config)

            config["adapter_class"] = classname
            config["parent"] = parent

            order = self.__adapters["order"]
            order.append(name)
            self.__adapters["order"] = order
            self.__adapters[name] = config

            self.__adapters.sync()

        return PythonResourceAdapter(adapter = self, name = name, config = config)

    def _get_resource(self, uuid):
        if uuid.typename == "pyromanager":
            if uuid.parent != self.parent_id or uuid.resourcename != "0":
                raise Exception("No such resource here (%s): %s, %s %s" % (self.parent_id, uuid, uuid.parent, uuid.resourcename))
            return PyroManager(adapter = self)
        
        assert(uuid.parent == self.parent_id / "pyromanager-0")
        assert(uuid.typename == "pythonresourceadapter")

        try:
            config = self.__adapters[str(uuid.resourcename)]
        except KeyError:
            raise Exception("No such adapter here: %s" % (uuid,))

        config.pop("adapter_class")
        return PythonResourceAdapter(adapter = self, uuid = uuid, config = config)
            

    def _list_resources(self, parentId, typename):
        if parentId == self.parent_id:
            if typename and typename != "pyromanager":
                raise Exception("No such type here: %s" % typename)

            return ( PyroManager(adapter = self), )

        assert(not typename or typename == "pythonresourceadapter")
        if parentId != self.parent_id / "pyromanager-0":
            raise Exception("Wrong parentid: %s (%s, %s)" % (parentId, self.parent_id, self.parent_id / "pyromanager_0"))

        with self.__lock:
            return [ PythonResourceAdapter(adapter = self, name = name, config = config) for name, config in self.__adapters.iteritems() if name != "order" ]

    def __load_adapters(self):
        order = self.__adapters["order"]
        for k in self.__adapters:
            if k != "order" and k not in order:
                raise AssertionError("Name %s not in order %s" % (k, order))

        for k in order:
            config = self.__adapters[k]
            logger.debug("Loading adapter: %s - %s" % (k, config))
            try:
                self.__load_adapter(k, config.pop("adapter_class"), config)
            except:
                logger.exception("Adapter %s failed to come up" % k)
        

    def __load_adapter(self, name, classname, config):
        module, klass = classname.rsplit(".", 1)

        logger.debug("Loading adapter: %s, %s.%s - %s" % (name, module, klass, config))

        if config["parent"] == "/":
            config["parent"] = None

        if module not in sys.modules:
            logger.debug("Importing: %s" % module)
            __import__(module, level = 0)
        adapter =  getattr(sys.modules[module], klass)(manager = self.manager, **config)

        self.__running[name] = adapter

        logger.debug("adapter loaded")

    """
    def __get_module(self, module):
        try:
            return sys.modules[module]
        except KeyError:
            parent, _, module = module.rpartition(".")

            path = fp = None
            parent = ""
            if parent:
                parent = self.__get_module(modules[0])
                path = parent.__path__
                parent = parent.__name__ + "."

            try:
                fp, path, desc = imp.find_module(module, path)
                return imp.load_module(parent + module, fp, path, desc)
            except ImportError:
                logger.exception("Module not found")
                raise
            finally:
                if fp:
                    fp.close()
    """
    
    def _set_attribute(self, *args, **kw):
        pass
            
    def __check_class(self, c):
        if "." not in c or ".." in c or c.startswith(".") or c.endswith("."):
            raise ValueError("Illegal classname: " + c)
