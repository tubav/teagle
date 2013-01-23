#! /usr/bin/env/python

from path import path as Path

class Merger(object):
	def merge(self, package, dir):
		raise NotImplementedError()

class SoftwareMerger(Merger):
	def merge(self, package, dir):
		source = Path(dir) / "image"

		source.checkdir()

		target = package.basedir
		if target.exists() and not target.isdir():
			raise Exception("'%s' is not a directory" % (target, ))
		if not target.exists():
			target.mkdir(077)

		target = package.installdir
		if target.exists():
			raise Exception("'%s' already exists" % (target, ))

		source.move(target)

