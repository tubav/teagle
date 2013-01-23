from ptm.Resource import Resource
from ptm.Identifier import Identifier
from SoftwareAdapter.Package import Daemon, StartStopDaemonController
from SoftwareAdapter.Step import *
from SoftwareAdapter.datatypes import Reference, String
from socket import gethostname
from path import path as Path

#__all = [ "MySQL", "MySQLManager" ]

class SetPWStep(Step):
    def __init__(self, sock):
        self.__socket = sock
    
    def do(self):
        import MySQLdb
    
        conn = MySQLdb.connect(unix_socket = self.__socket, db = "mysql")
        try:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM user WHERE (User != 'root' OR Host != 'localhost') AND User != 'xdms'");
            cursor.execute("CREATE USER 'root'@'%' IDENTIFIED BY 'root'")
            cursor.execute("GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION")
            cursor.execute("UPDATE user SET Password = PASSWORD('root') WHERE User = 'root'") 
            cursor.execute("FLUSH PRIVILEGES")
            conn.commit()
        finally:
            conn.close()

    def undo(self):
        pass

class MySQL(Daemon):
    __appname__ = "mysql"
    __ptm_slots__ = ( "port", )
    
    #port = Reference(nullable = False)
    dummy = String(nullable = True)

    def __init__(self, port = None, *args, **kw):
        super(MySQL, self).__init__(*args, **kw)
        from SoftwareAdapter.util import DummyPort
        self.port = DummyPort(3306)
        #if not isinstance(port, Resource):
        #	port = self.client.add_resource(Identifier(self.parent_id) / "ipv4interface-ANY", None, "tcpport", dict(number = port))
        
        #self.port = port
    
    
    def postdeploy(self):
        super(MySQL, self).postdeploy()
        self.client.add_resource(Identifier(self.parent_id) / "pyromanager-0", None, "pythonresourceadapter", config = dict(adapter_class = "MySQLAdapter.MySQLAdapter", adapter_parent = self))
    
    def get_port_num(self):
        return self.port.number
    port_num = property(get_port_num)
    port_number = port_num
    
    def get_ip(self):
        return self.parent.public_ip
    ip = property(get_ip)
    public_ip = ip
    
    def get_admin_user(self):
        return 'root'
    admin_username = property(get_admin_user)
    
    def get_admin_pass(self):
        return 'root'
    admin_password = property(get_admin_pass)
    
    def get_controller(self):
        pidfile = self.installdir + "/run/mysqld.pid"
        args = [ "--basedir=" + self.installdir, "--datadir=" + self.installdir + "/data", "--user=root", "--pid-file=" + pidfile, "--skip-external-locking" , "--port=" + str(self.port.number),  "--socket=" + self.installdir + "/run/mysqld.sock" ]
        return StartStopDaemonController(package = self, executable = "./libexec/mysqld", pidfile = pidfile, daemonargs = args, fork = True, sleep = 10)

    def _get_configuration(self):
        config = super(MySQL, self)._get_configuration()
        config["port"] = 3306
        config["port_number"] = 3306
        config["public_ip"] = "127.0.0.1"
        config["admin_user"] = "root"
        config["admin_pass"] = "root"
        return config
