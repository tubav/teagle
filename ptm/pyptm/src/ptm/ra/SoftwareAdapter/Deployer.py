#! /usr/bin/env python

import logging
from Installer import CopyTreeInstaller
from path import path as Path
from tempfile import mkdtemp
from Step import DefaultTreeWalker

logger = logging.getLogger("ptm")

class Deployer(object):
	def deploy(self):
		raise NotImplementedError()

class DefaultDeployer(Deployer):
	def __init__(self, package, installer = None, *args, **kw):
		super(DefaultDeployer, self).__init__(*args, **kw)

		self.package = package

		if installer is None:
			installer = CopyTreeInstaller(package = package)

		self.installer = installer
		assert(self.installer is not None)

	def deploy(self):
		logger.debug("Deploying: %s" % (self.package.identifier, ))

		td = self.make_tempdir()
		try:
			self.preinstall()

			self.installer.install(td)
			
			self.postinstall()

			imagedir = td / "image"

			imagedir.checkdir()

			self.configure(imagedir)

			self.merge(td)
			self.postmerge()
		except:
			logger.exception("Error deploying")
			raise
		finally:
			td.rmtree()

	def configure(self, target):
		self.package.get_configurator(target).do()
#		DefaultTreeWalker(package = self.package, source = self.package.repodir / "templates", target = target, tokens = self.__tokens).do()

	def make_tempdir(self):
		td = Path(mkdtemp())
#		(td / "image").mkdir(0770)
		return td
		
	def merge(self, td):
		self.package.adapter.merge(self.package, td)

	def preinstall(self):
		pass
	
	def postinstall(self):
		pass

	def postmerge(self):
		pass


