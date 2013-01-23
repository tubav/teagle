#! /usr/bin/env python

from ptm.Resource import Resource
from Deployer import DefaultDeployer
from path import path as Path
import logging
import sqlalchemy.orm
from sqlalchemy.orm.exc import NoResultFound
from Step import DefaultTreeWalker
from shutil import rmtree

logger = logging.getLogger("ptm")

class Package(Resource):

	def __init__(self, adapter, uuid = None, parent = None, type = None, name = None, *args, **kw):
		
		Resource.__init__(self, adapter, uuid, parent, type, name)
	
		self.id = self.name
		#raise Exception(self.identifier, uuid, parent, type, name)

	def deploy(self):
		self.get_deployer().deploy()

	def undeploy(self):
		raise NotImplementedError()

	def postdeploy(self):
		pass

	def init(self, *args, **kw):
		pass

	def get_deployer(self):
		return DefaultDeployer(package = self)

	def get_repodir(self):
		return  self._get_repodir()
	repodir = property(get_repodir)

	def _get_repodir(self):
		return self.adapter.repodir / self.typename

	def get_fields(self):
		return self.adapter.get_fields(self.__class__)
	fields = property(get_fields)

	def get_session(self):
		s = sqlalchemy.orm.object_session(self)
#		if s is None:
#			s = self.adapter.make_session()
#			s = s.merge(self)
		return s
	session = property(get_session)

	def commit(self):
		s = self.session
		if s is None:
			s = self.adapter.make_session()
			s.merge(self)
			for f in self.fields:
				if f[1]:
					for x in tuple(getattr(self, f[0])):
						if isinstance(x, Package):
							s.merge(x)
		s.commit()

	@classmethod
	def get_instance(klass, adapter, parent, name):
		s = adapter.make_session()
		try:
			e = s.query(klass).filter_by(id = name).one()
		except NoResultFound:
			logger.exception("Resource not found: %s %s" % (klass, name))
			raise Exception("Resource %s not found (%s)" % (name, klass))
		Resource.__init__(e, adapter = adapter, type = klass.__name__.lower(), parent = parent, name = name)
		return e

	@classmethod
	def list_instances(klass, adapter, parent):
		s = adapter.make_session()
		entities = s.query(klass).all()
		for e in entities:
			Resource.__init__(e, adapter = adapter, type = e.__class__.__name__.lower(), parent = parent, name = e.id )
		#if klass.__name__.lower() == "mysql":
			#raise Exception(entities)
		return entities

	def _get_configuration(self):
		conf = {}
		for f in self.fields:
			logger.debug("getconf: %s" % str(f))
			conf[f[0]] = getattr(self, f[0])
		return conf

	def _set_configuration(self, config):
		if config:
			for k, v in config.iteritems():
				self._setAttribute(k, v)
			return self._do_set_configuration()

	def _set_attribute(self, key, value):
		self._setAttribute(key, value)
		return self._setConfiguration()
		

	def _set_ttribute(self, key, value):
		for name, islist in self.fields:
			if name == key:
				break;
		else:			
			raise AttributeError(key)

		if islist:
			l = getattr(self, key)
			del l[:]
			l[:] = value
		else:
			setattr(self, key, value)

	def _do_set_configuration(self):
		self.commit()
		return self.get_configurator(self.installdir).do()
#		return super(Package, self).setConfiguration(config)

	def get_configurator(self, target, **kw):
		return DefaultTreeWalker(package = self, source = self.repodir / "templates", target = target, tokens = kw, overwrite = True)
		

import tempfile

class SoftwarePackage(Package):

	def get_basedir(self):
		return self.adapter.installdir / self.typename
	basedir = property(get_basedir)

	def get_installdir(self):
		return self.basedir / self.name
	installdir = property(get_installdir)

	def undeploy(self):
	#	self.installdir.rmtree()
		pass
	
	@classmethod
	def get_shareddir(klass, adapter):
		return adapter.shareddir / klass.__name__.lower()

	def __get_shareddir(self):
		return self.get_shareddir(self.adapter)
	shareddir = property(__get_shareddir)

	@classmethod
	def deploy_shared(klass, adapter):
		source = adapter.repodir / klass.__name__.lower() / "shared"
		target = klass.get_shareddir(adapter)

		source.checkdir()
		if target.exists():
			logger.warning(target + " already exists. Removing it.")
			rmtree(target)

		td = Path(tempfile.mkdtemp())
		try:
			temptarget = td / "shared"
			source.copytree(temptarget)
			temptarget.move(target)
		finally:
			td.rmtree()


import datatypes

class Daemon(SoftwarePackage):
	started = datatypes.Boolean(nullable = False)

	def __init__(self, sleep = 0, *args, **kw):
		super(Daemon, self).__init__(*args, **kw)
		self.started = False

	def postdeploy(self):
		super(Daemon, self).postdeploy()
		self.start()

	def is_running(self):
		return self._is_running()
	running = property(is_running)

	def _is_running(self):
		return self.started

	def start(self, oknodo = False):
		controller = self.get_controller()
		if self.started and controller.is_running():
			if not oknodo:
				raise Exception("Already running")
			return
				
		logger.debug("--------STARTING: %s" % self)
		self.started = True
		self.commit()

		controller.start()

	def stop(self, oknodo = False):
		controller = self.get_controller()
		if not self.started and not controller.is_running():
			if not oknodo:
				raise Exception("Already running")
			return
		
		self.started = False

		controller.stop()

	def get_controller(self):
		raise NotImplementedError()

	def restart(self, oknodo = False):
		self.stop(oknodo)
		self.start()

	def undeploy(self):
		try:
			self.stop(True)
		except:
			logger.exception("Error while undeploying")
		finally:
			super(Daemon, self).undeploy()

	def _set_onfiguration(self, config):
		config.pop("port", None)

		started = config.pop("started", None)

		if not config and (started is None or started == self.started):
			if unicode(started).lower() != "restart":
				return

		logger.debug("will restart %s %s %s" % (config, started, self.started))

		if started is not None:
			started = unicode(started).lower()
			if started == "restart":
				self.stop(True)
			elif not started or started[0] not in ("t", "j", "1", "y"):
				self.stop(True)
				started = None
		else:
			started = self.started
			if started:
				self.stop()

		try:
			if config:
				super(Daemon, self).setConfiguration(config)
			else:
				self.commit()
		except:
			if started is None:
				self.start(True)
			raise
		finally:
			logger.debug("Deamon restart %s check - %s" % (self, started))
			if started:
				self.start(True)

	def setAttribute(self, name, value):
		return self.setConfiguration({name: value})
	
import time

class DaemonController(object):
	def __init__(self, package, sleep = 5, stop_sleep = 3, *args, **kw):
		super(DaemonController, self).__init__(*args, **kw)

		self.__package = package
		self.__sleep = int(sleep)
		self.__stop_sleep = int(stop_sleep)

	def get_package(self):
		return self.__package
	package = property(get_package)

	def is_running(self):
		raise NotImplementedError()

	def start(self):
		self._start()
		time.sleep(self.__sleep)

	def _start(self):
		raise NotImplementedError()

	def stop(self):
		self._stop()
		time.sleep(self.__stop_sleep)

	def _stop(self):
		raise NotImplementedError()

class DummyController(DaemonController):
	def start(self):
		pass

	stop = start

	def is_running(self):
		return self.package.started

import os
import errno

class CheckPIDFileController(DaemonController):
	def __init__(self, package, pidfile = None, *args, **kw):
		super(CheckPIDFileController, self).__init__(package = package, *args, **kw)

		self.__package = package

		if pidfile is None:
			pidfile = Path(self.package.basename + ".pid")
		elif not Path(pidfile).isabs():
				pidfile = self.package.installdir / pidfile

		self.__pidfile = pidfile


	def get_pidfile(self):
		return self.__pidfile
	pidfile = property(get_pidfile)

	def is_running(self):
		if not self.pidfile.exists():
			return False

		if not self.pidfile.isfile():
			raise Exception("pidfile '%s' is not a file" % (self.pidfile, ))

		try:
			pid = int(self.__pidfile.open().readline(16))
		except:
			logger.exception("Error reading pidfile")
			raise

		try:
			os.kill(pid, 0)
			return True
		except OSError, e:
			if e.errno == errno.ESRCH:
				return False
			raise

import subprocess

class StartStopDaemonController(CheckPIDFileController):
	def __init__(self, package, executable, fork = False, workingdir = None, makepidfile = False, daemonargs = None, ssd = "/sbin/start-stop-daemon", ldpath = None, outfile = "/dev/null", *args, **kw):
		super(StartStopDaemonController, self).__init__(package = package, *args, **kw)

		executable =  Path(executable)
		if not executable.isabs():
			executable = self.package.installdir / executable
		self.__executable = unicode(executable)

		if workingdir is not None:
			workingdir = Path(workingdir)
			if not workingdir.isabs():
				workingdir = self.package.installdir / workingdir
			self.__workingdir = unicode(workingdir)
		else:
			self.__workingdir = self.package.installdir

		if ldpath is not None:
			if not isinstance(ldpath, (list, set, tuple, frozenset)):
				ldpath = [ ldpath ]
			ldpath = tuple(set(ldpath))
		self.__ldpath = ldpath

		self.__makepidfile = makepidfile
		self.__daemonargs = daemonargs
		self.__fork = fork
		self.__ssd = ssd
		self.__outfile = outfile

	def get_daemonargs(self):
		return self.__daemonargs
	def set_daemonargs(self, da):
		self.__daemonargs = da
	daemonargs = property(get_daemonargs, set_daemonargs)

	def __make_cmd(self, cmd, test):
		cmd = [ self.__ssd, cmd, '-x', self.__executable, '-d', self.__workingdir, '-p', self.pidfile, '-o' ]

		if test:
			cmd.append('-t')
	
		env = None
		if self.__ldpath:
			env = dict(LD_LIBRARY_PATH = ':'.join(self.__ldpath))

		return cmd, env

	def __check_cmd(self, cmd, env):
		logger.debug("ssd env: " + str(env))
		logger.debug("ssd command: " + ' '.join(cmd))

		outfile = self.__outfile
		if outfile is True:
			outfile = self.package.installdir / self.package.basename + ".log"
		if outfile:
			outfile = Path(outfile).open("a")

		try:
			subprocess.check_call(cmd, stdout = outfile, stderr = subprocess.STDOUT, close_fds = True, cwd = self.__workingdir, env = env)
		finally:
			if outfile is not None:
				outfile.close()

	def _start(self):
		cmd, env = self.__make_cmd("-S", False)
		if self.__makepidfile:
			cmd.append('-m')

		if self.__fork:
			cmd.append('-b')

		if self.__daemonargs:
			cmd += [ '--' ] + list(self.__daemonargs)

		self.__check_cmd(cmd, env)

	def _stop(self):
		cmd, env = self.__make_cmd("-K", False)
		self.__check_cmd(cmd, env)

