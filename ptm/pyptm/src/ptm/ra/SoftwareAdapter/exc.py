#! /usr/bin/env python

class InternalError(Exception):
	pass

class CollisionError(InternalError):
	pass

class ExternalError(Exception):
	pass

class ConfigurationError(ExternalError):
	pass


