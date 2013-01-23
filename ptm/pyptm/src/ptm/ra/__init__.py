#! /usr/bin/env python

import ptm
from ptm.Resource import Resource
from ptm.ResourceAdapter import ReflectiveConstructorAddAdapter
from ptm.Identifier import Identifier 
import MySQLdb
import string
import logging
import sqlparse

logger = logging.getLogger("ptm")

LEGAL_CHARS = string.letters + string.digits + "_"

class DBObject(Resource):
    def __init__(self, db, *args, **kw):
        Resource.__init__(self, *args, **kw)
        self._check_string(self.name)

        self._check_string(db)
        rdbms = self.parent
        config = rdbms.get_configuration()
        self.__host = config["public_ip"]
        self.__port = config["port_number"]
        self.__user = config["admin_user"]
        self.__pw = config["admin_pass"]
        self.__db = db

    @staticmethod
    def _check_string(name):
        if not isinstance(name, basestring):
            raise Exception("%s %s" % (name, type(name)))
        for c in unicode(name):
            if c not in LEGAL_CHARS:
                raise ValueError("Illegal name: " + name)

    @staticmethod
    def __make__(klass, uuid, adapter, db):
        o = DBObject.__new__(klass)
        DBObject.__init__(o, identifier = uuid, adapter = adapter, db = db)
        return o

    def _connect(self, db = None):
        if db is None:
            db = self.__db
        return MySQLdb.connect (host = self.__host, port = self.__port, user= self.__user, passwd= self.__pw, db = db)

    def _execute(self, sql, db = None):
        conn = self._connect(db)
        if not isinstance(sql, (tuple, list, set)):
            sql = (sql, )
        try:
            cursor = conn.cursor()
            try:
                for s in sql:
                    logger.debug("Executing: " + str(s))
                    cursor.execute(s)
                result = cursor.fetchall()
            finally:
                cursor.close()
        except:
            logger.exception("Error executing sql")
            conn.rollback()
            raise
        else:
            conn.commit()
        finally:
            conn.close()

        return result

    def execute_raw(self, sql, db):
        conn = self._connect(db)
        executed = False
        try:
            conn.autocommit(True)
            sql = sqlparse.split(sql)
            for sql in sql:
                sql = sql.strip()
                if sql:
                    logger.debug("executing: " + str(sql))
                    conn.query(sql)
                    executed = True
            if executed:
                conn.commit()
        finally:
            conn.close()

    def get_rdbms(self):
        return self.parent
    rdbms = property(get_rdbms)

class DBUser(DBObject):
    def __init__(self, password = None, *args, **kw):
        DBObject.__init__(self, db ="mysql", *args, **kw)    

#        if password is None:
#            password = self.name
        password = self.name
        self._check_string(password)

        self._execute("CREATE USER '%s'@'%%' IDENTIFIED BY '%s'" % (self.name, password))

    @classmethod
    def __make__(klass, uuid, adapter):
        o = DBObject.__make__(klass, uuid, adapter, "mysql")
        #careful, mysql is braindead (read: case sensitive)
        count = int(o._execute("SELECT COUNT(User) FROM user WHERE User = '%s' AND Host = '%%'" % uuid.resourcename)[0][0])
        if count < 1:
            raise KeyError("No such user here: " + uuid)
        assert(count == 1)
        return o

    def _get_configuration(self):
        return dict( name = self.name, password = self.password )

    def get_password(self):
        return self.name
    password = property(get_password)


class Database(DBObject):
    def __init__(self, owner, *args, **kw):
        DBObject.__init__(self, db = "information_schema", *args, **kw)
        self.__owner = owner
        self._execute(["CREATE DATABASE %s;" % (self.name, ),  "GRANT ALL PRIVILEGES ON %s.* TO %s;" % (self.name, owner.name) ])

    def get_owner(self):
        return self.__owner
    owner = property(get_owner)

    def execute(self, sql):
        return self.execute_raw(sql, self.name)

    def _get_configuration(self):
        return dict( name = self.identifier.resourcename )

    @classmethod
    def __make__(klass, uuid, adapter):
        o = DBObject.__make__(klass, uuid, adapter, "information_schema")
        count = int(o._execute("SELECT COUNT(SCHEMA_NAME) FROM SCHEMATA WHERE SCHEMA_NAME = '%s'" % (uuid.resourcename, ), "information_schema")[0][0])

        if count < 1:
            raise KeyError("No such database here: " + uuid.resourcename)
        assert(count == 1)
        return o


class MySQLAdapter(ReflectiveConstructorAddAdapter):
    def __init__(self, manager, parent, *args, **kw):
        super(MySQLAdapter, self).__init__(manager = manager, parent = parent, *args, **kw)
	print '######### MySQLAdapter, parent: %s' %parent
        self.__dbuuid = parent

        self.add_type(DBUser, "dbuser", None)
        self.add_type(Database, "database", None)
        self.__i = False
        
    def _init(self):
        if self.__i:
            return
        rdbms = self.parent

        
        config = rdbms.get_configuration()
        self.__host = config["public_ip"]
        self.__port = config["port_number"]
        self.__user = config["admin_user"]
        self.__pw = config["admin_pass"]
        self.__rdbms = rdbms
        self.__i = True

    def _get_resource(self, uuid):
        if uuid.typename == "dbuser":
            return DBUser.__make__(uuid, self)
        if uuid.typename == "database":
            return Database.__make__(uuid, self)
        raise KeyError("No such obketc here: " + str(uuid))
            

    def _list_resources(self, parent, type = None):
        self._init()
        conn = MySQLdb.connect (host = self.__host, port = self.__port, user= self.__user, passwd= self.__pw, db = "mysql")
        try:
            c = conn.cursor()
            c.execute("SELECT DISTINCT User FROM user WHERE User != 'root'")
            #result =  [ self.__dbuuid / ("dbuser_" + str(x[0])) for x in c.fetchall() ]
            result =  [ self.get_entity(self.__dbuuid.make_child_uuid("dbuser", str(x[0]))) for x in c.fetchall() ]
        finally:
            conn.close()

        conn = MySQLdb.connect (host = self.__host, port = self.__port, user= self.__user, passwd= self.__pw, db = "information_schema")

        try:
            c = conn.cursor()
            c.execute("SELECT DISTINCT SCHEMA_NAME FROM SCHEMATA WHERE SCHEMA_NAME != 'mysql'")
            #result +=  [ self.get_resource(self.__dbuuid.make_child_identifier("database", str(x[0]))) for x in c.fetchall() ]
            result +=  [ self.get_resource((Identifier(self.__dbuuid)).make_child_identifier("database", str(x[0]))) for x in c.fetchall() ]
        finally:
            conn.close()

        return tuple(result)
        
    def _mangle_args(self, klass, kwargs):
        if klass == Database and "owner" not in kwargs:
            kwargs["owner"] = kwargs.pop("user")
        return super(MySQLAdapter, self)._mangle_args(klass, kwargs)

    def _set_attribute(self, *args, **kw):
        pass
