#! /usr/bin/env python

import logging
from path import path as Path
from tempfile import mkdtemp

logger = logging.getLogger("ptm")

class Installer(object):
	def install(self, target):
		raise NotImplementedError()

class AbstractInstaller(Installer):
	def __init__(self, package, *args, **kw):
		super(AbstractInstaller, self).__init__(*args, **kw)
		self.__package = package

	def get_package(self):
		return self.__package
	package = property(get_package)


def StepInstaller(AbstractInstaller):
	def __init__(self, package, steps = None, *args, **kw):
		super(StepInstaller, self).__init__(package = package, *args, **kw)
		if steps is None:
			self.__steps = []
		else:
			self.__steps = list(steps)

	def preinstall(self):
		pass

	def postinstall(self):
		pass

	def postmerge(self):
		pass

	def install(self):
		logger.debug("Installing: %s" % (self.package.uuid, ))

		self.package.repodir.checkdir()

		done = []
		td = self.make_tempdir()
		try:
			steps = self.get_steps(td)
			logger.debug("Performing %d steps: %s" % (len(steps), steps))

			self.preinstall()
			for s in steps:
				logger.debug("Performing step: " + str(s))
				s.do()
				done.insert(0, s)

			self.postinstall()
			self.merge(td)
			self.postmerge()
		except:
			logger.exception("Error installing")
			logger.debug("rolling back %d steps: %s" % (len(done), done))
			for s in done:
				try:
					logger.debug("Undoing: " + str(s))
					s.undo()
				except:
					logger.exception("Error while undoing step: %s" % (s, ))
			raise
		finally:
			td.rmtree()

	def get_steps(self):
		return self.__steps
		
	def make_tempdir(self):
		td = Path(mkdtemp())
		(td / "image").mkdir(0770)
		return td
		
	def merge(self, td):
		self.package.adapter.merger.merge(self.package, td)

from Step import CopyTreeStep

class CopyTreeInstaller(AbstractInstaller):
	def install(self, target):
		imagedir = self.package.repodir / "image"
		imagedir.checkdir()

		CopyTreeStep(imagedir, target / "image").do()


