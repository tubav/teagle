#! /bin/env python
import logging
import os
import sys
import optparse
from tempfile import mkdtemp, mkstemp, NamedTemporaryFile, mktemp
from path import path as Path
from shutil import copytree, rmtree,  move, copymode
from os import remove, fdopen
import struct
from exc import *
#from system import *

logger = logging.getLogger("ptm")

class Step(object):
	logger = logger

	def do(self):
		raise NotImplementedError()

	def undo(self):
		pass

	def __str__(self):
		return self.__class__.__name__

class ReverseStep(Step):
	def __init__(self, s):
		self.__step = s

	def do(self):
		self.__step.undo()

	def undo(self):
		self.__step.do()

	def __str__(self):
		return Step.__str__(self) + "(" + str(self.__step) + ")"


class RemoveStep(Step):
	def __init__(self, target):
		self.__target = Path(target)

	def do(self):
		if not self.__target.exists() or not self.__target.isfile():
			raise InternalError(self.__target + " does not exist")
		self.__target.remove()

	def undo(self):
		raise InternalError("Cannot undo")

class CopyTreeStep(Step):
	def __init__(self, source, target, *args, **kw):
		super(CopyTreeStep, self).__init__(*args, **kw)
		self.source = Path(source)
		self.target = Path(target)

	def do(self):
		self.logger.debug("CopyTree: " + str(self.source) + " -> " + str(self.target))
		copytree(self.source, self.target)
		assert(self.target.isdir())

class TreeWalker(Step):
	def __init__(self, source, target, mangler_factory, *args, **kw):
		super(TreeWalker, self).__init__(*args, **kw)
		self.source = source
		self.target = target
		self.__get_mangler = mangler_factory

	def do(self):
		self.target.checkdir()

		for f in self.source.walkfiles():
			logger.debug("Walking over: " + f)
			rel = self.source.relpathto(f)

			targetdir = self.target / rel.dirname()
			if targetdir.exists():
				targetdir.checkdir()
			else:
				targetdir.mkdir(0770)

			mangler = self.__get_mangler(self.source / rel, self.target / rel)
			mangler.do()

class DefaultTreeWalker(TreeWalker):
	def __init__(self, source, target, package, tokens = None, overwrite = False, *args, **kw):
		super(DefaultTreeWalker, self).__init__(source, target, mangler_factory = self.__get_mangler, *args, **kw)
		self.__tokens = tokens
		self.package = package
		self.__overwrite = overwrite

	
	def __get_mangler(self, source, target):
		logger.debug("Mangler for: %s -> %s" % (source, target))
		if source.endswith(".jar"):
			raise NotImplementedError()

		return TokenReplacer(tokens = self.__tokens, source = source, target = target, package = self.package, overwrite = self.__overwrite)


				

class FileMangler(Step):
	def __init__(self, source, target, package, mode = "r", overwrite = False, *args, **kw):
		super(FileMangler, self).__init__(*args, **kw)
		self.source = source
		self.target = target
		self.package = package
		self.__overwrite = overwrite
		if not mode.startswith("r"):
			mode = "r" + mode
		self.mode = mode

	def do(self):
		self.source.checkfile()
		tf = NamedTemporaryFile(delete = False)
		try:
			with tf:
				with self.source.open(self.mode) as source:
					self.mangle(source, tf)
				
			if not self.__overwrite and self.target.exists() and not self.target.isdir():
				raise Exception("Target file already exists: " + self.target)

			#self.logger.debug("Copymode: %s -> %s" % (self.source, self.target))
			copymode(self.source, tf.name)

			Path(tf.name).move(self.target)
		except:
			Path(tf.name).remove()
			raise

	def mangle(self, source, target):
		raise NotImplementedError()

class TokenReplacer(FileMangler):
	def __init__(self, tokens = None, *args, **kw):
		super(TokenReplacer, self).__init__(*args, **kw)

		self.__tokens = {
			"%%NAME%%": self.package.name,
			"%%INSTANCE_NAME%%": self.package.name,
			"%%INSTALL_DIR%%": self.package.installdir,
			"%%BASENAME%%": self.package.identifier.basename,
			"%%TYPENAME%%": self.package.typename,
			"%%PUBLIC_IP%%": self.package.parent.get_attribute("public_ip"),
			#"%%PUBLIC_IP%%": self.package.parent.public_ip,
			"%%SHARED_DIR%%": self.package.shareddir
		}	
		
		#config = self.package.get_configuration()

		#if hasattr(self.package, "port"):
		#	self.__tokens["%%PORT%%"] = unicode(self.package.port.number)
		
		self.__tokens.setdefault("%%PORT%%", "3306")
		#else: raise Exception(self.package)
		if hasattr(self.package, "webcontext"):
			self.__tokens["%%WEBCONTEXT%%"] = unicode(self.package.webcontext)

		parent = self.package.parent
		if parent is not None:
			self.__tokens["%%PARENT_ID%%"] = unicode(parent.identifier)

		if tokens is not None:
			self.__tokens.update(tokens)

	def mangle(self, source, target):
		for line in source:
			for k, v in self.__tokens.iteritems():
				if v is not None:
					line = line.replace(k, unicode(v))

			target.write(line)

class RemoveTreeStep(Step):
	def __init__(self, target):
		self.__target = Path(target)

	def do(self):
		if not self.__target.exists():
			raise InternalError(self.__target + " does not exist")
		rmtree(self.__target)

	def undo(self):
		raise InternalError("Cqannot undo")

class ReplaceAndCopyStep(Step):
	def __init__(self, sourcefile, destfile, tokens, exe = False, *args, **kw):
		super(ReplaceAndCopyStep, self).__init__(*args, **kw)
		self.__source = Path(sourcefile)
		self.__dest = Path(destfile)
		self.__tokens = tokens
		self.__exe = exe

	def do(self):
		self.logger.debug("ReplaceAndCopyStep to: "+ str(self.__dest))
		self.logger.debug("From: " + self.__source)
		if self.__dest.exists():
			raise CollisionError(str(self.__dest) + " already exists")
		outfile = self.__dest.open("w")
		self.logger.debug(self.__tokens)
		try:
			for line in (self.__source).open():
				for k, v in self.__tokens.iteritems():
					line = line.replace(k, v)
				outfile.write(line)
		except:
			self.logger.exception("Error doing")
			raise
		finally:
			outfile.close()

		if self.__exe:
			os.chmod(self.__dest, 0755);

	def undo(self):
		self.__dest.remove()

class MkdirStep(Step):
	def __init__(self, dir, mode = 0750, soft = True):
		Step.__init__(self)
		self.__dir = Path(dir)
		self.__mode = mode
		self.__soft = soft
		self.__created = False

	def do(self):
		if not self.__soft or not self.__dir.exists():
			self.__dir.mkdir(self.__mode)
			self.__created = True

	def undo(self):
		if not self.__soft or self.__created:
			self.__dir.rmdir()

	def __str__(self):
		return super(self.__class__, self).__str__() + ": " + self.__dir

class CreateDBStep(Step):
	def __init__(self, user, pw, db, data, host = "localhost"):
		Step.__init__(self)
		self.__user = user
		self.__pw = pw
		self.__db = db
		self.__host = host
		self.__data = Path(data)

	def do(self):
		if os.system("echo CREATE DATABASE %(db)s | mysql -u %(user)s --password=%(pw)s > /dev/null" % dict(db = self.__db, user = self.__user, pw = self.__pw)) != 0:
			raise Exception("Failed to create database")
		if self.__data is not None and os.system("cat %(file)s | mysql -u %(user)s --password=%(pw)s %(db)s > /dev/null" % dict(db = self.__db, user = self.__user, pw = self.__pw, file = self.__data)) != 0:
			raise Exception("Failed to initialize database")

	
	def undo(self):
		if os.system("echo DROP DATABASE %(db)s | mysql -u %(user)s --password=%(pw)s > /dev/null" % dict(db = self.__db, user = self.__user, pw = self.__pw)) != 0:
			raise Exception("Failed to drop database")

class SickClassHackStep(Step):
	def __init__(self, beginfile, endfile, outfile, text):
		Step.__init__(self)
		self.__begin = Path(beginfile)
		self.__end = Path(endfile)
		self.__out = Path(outfile)
		self.__text = text

	def do(self):
		self.logger.debug("Rewriting class file with: " + self.__text)		
		assert (not self.__out.exists())
		l = struct.pack("B", len(self.__text))
		f = self.__out.open("wb")
		f.write(self.__begin.open("rb").read())
		f.write(l)
		f.write(self.__text)
		f.write(self.__end.open("rb").read())
		f.close()

	def undo(self):
		self.__out.remove()

class JarStep(Step):
	def __init__(self, sourcedir, target):
		Step.__init__(self)
		self.__dir = Path(sourcedir)
		self.__target = Path(target)

		if not self.__dir.isabs() and self.__target.isabs():
			raise InternalError("source and target must be absolute")

		if self.__target.exists():
			raise CollisionError(str(self.__target) + " exists.")

	def do(self):
		r = os.system("jar -cf %(target)s -C %(dir)s . > /dev/null && chmod o+r %(target)s" % dict(dir = self.__dir, target = self.__target))
		if r != 0:
			raise Exception("jar failed with exitcode: " + str(r))
	
	def undo(self):
		self.__target.remove()

class InsertIntoFileStep(Step):
	def __init__(self, file, mark, data):
		Step.__init__(self)
		self.__file = Path(file)
		self.__mark = mark
		if not data.endswith("\n"):
			data += "\n"
		self.__data = data

	def do(self):
		found = False
		tfd, tfname = mkstemp()
		try:
			f = open(self.__file)
			for line in f:
				if not found and line[:-1] == self.__mark:
					os.write(tfd, self.__data)
					found = True
				os.write(tfd, line)
			os.close(tfd)
			f.close()
			move(tfname, self.__file)
		except BaseException, e:
			remove(tfname)
			raise e

	def undo(self):
		tfd, tfname = mkstemp()
		try:
			f = open(self.__file)
			all = f.read()
			f.close()
			p = all.find(self.__data)
			if p < 0:
				raise InternalError("data not found in file")
			if all.find(self.__data, p + 1) >= 0:
				raise InternalError("data found twice, bailing...")

			all = all.replace(self.__data, "")
			os.write(tfd, all)
			os.close(tfd)
			move(tfname, self.__file)
		except BaseException, e:
			remove(tfname)
			raise e

class ExecStep(Step):
	def __init__(self, cmd, undocmd = None):
		self.__cmd = cmd
		if undocmd is None:
			undocmd = cmd
		self.__undo = undocmd

	def do(self):
		self.__exec(self.__cmd)

	def __exec(self, cmd):
		self.logger.debug("Executing: " + cmd)
		rv = os.system(cmd)
		if rv != 0:
			raise InternalError(cmd + " exited with code " + str(rv))

	def undo(self):
		self.__exec(self.__undo)

class DeployWarStep(Step):
	def __init__(self, path, webcontext):
		self.__path = Path(path)
		self.__context = webcontext

	def do(self):
		path = self.__path
		if path.isdir():
#			f = NamedTemporaryFile(delete = False) #need Python 2.6 for this
#			f.close()
#			f = PAth(f.name)
			f = Path(mktemp())
			try:
				JarStep(path, f).do()
				System.deploy_war(f, self.__context)
			finally:
				f.remove()
		else:
			System.deploy_war(path, self.__context)

	def undo(self):
		System.undeploy_war(self.__context)

class TestStep(Step):
	def do(self):
		pass

	def undo(self):
		pass


class ChDirStep(Step):
	def __init__(self, target):
		self.__target = Path(target)
		self.__cwd = os.getcwd()
	def do(self):
		self.__cwd = os.getcwd()
		os.chdir(self.__target)

	def undo(self):
		os.chdir(self.__cwd)

class DirectSQLStep(Step):
	def __init__(self, host, port, user, pw, db, sql, undosql):
		self.__host = host
		self.__port = port
		self.__user = user
		self.__pw = pw
		self.__db = db
		self.__sql = sql
		self.__undosql = undosql

	def do(self):
		from subprocess import Popen, PIPE

		cmd = Popen(["mysql", "-h", str(self.__host), "-P", str(self.__port), "-u", self.__user, "-p" + self.__pw, self.__db], stdin = PIPE, stdout=PIPE, stderr=PIPE, close_fds = True)
		out, err = cmd.communicate(self.__sql)
		self.logger.debug("Childs out: " + str(out))
		self.logger.debug("Childs err: " + str(err))
		if cmd.returncode != 0:
			raise Exception("Child exited with exit code: " + str(cmd.returncode))
	
	def undo(self):
		pass

class ManagedSQLStep(Step):
	def __init__(self, db, sql, undosql):
		self.__db = db
		self.__sql = sql
		self.__undosql = undosql

	def do(self):
		self.__db.execute_sql(self.__sql)

	def undo(self):
		self.__db.execute_sql(self.__undosql)

class StandardStep(Step):
	def __init__(self, instance, tokens):
		self.__instance = instance
		self.__tokens = tokens

	def do(self):
		imagedir = self.__instance.repodir / "image"
		templatedir = self.__instance.repodir / "templates"
		
		try:
			imagedir.copytree(self.__instance.installdir)
			l = len(templatedir) + 1
			for p in templatedir.walk():
				ip = self.__instance.installdir / p[l:]
				if p.isdir():
					if ip.exists():
						if not ip.isdir():
							raise CollisionError(ip + " already exists but is not a directory")
					else:
						ip.mkdir(0755)
				else:
					if ip.exists():
						raise CollisionError(ip + " already exists")
					with ip.open("w") as outfile:
						with p.open() as infile:
							for line in infile:
								for k, v in self.__tokens.iteritems():
									line = line.replace(k, str(v))
								outfile.write(line)
		except:
			self.logger.exception("Error while performing install")
			try:
				self.undo()
			except:
				logger.exception("Error while undoing")
				pass
			raise


	def undo(self):
		self.__instance.installdir.rmtree()
