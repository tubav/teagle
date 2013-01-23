#! /usr/bin/env python

from Identifier import Identifier

_logger = None
_home = None

def check_owner(owner, weak):
	if ((weak and not isinstance(owner, Identifier)) or (isinstance(owner, Identifier) and not owner.is_absolute)):
		raise Exception("Illegal weak owner: %s" % (owner, ))
	return owner

def is_list(o):
	return isinstance(o, (tuple, list, set, frozenset))

def get_logger():
	global _logger
		
	if _logger is None:
		import logging
		
		_logger = logging.getLogger("ptm")
		
	return _logger

def get_ptm_home():
	global _home
	if _home is None:
		from path import path as Path
		import os
		
		_home = os.getenv("PTM_HOME")
		if not _home:
			get_logger().warn("PTM_HOME is not set. Assuming /opt/ptm")
			_home = "/opt/ptm"
		_home = Path(_home)
			
	return _home

def get_storage_dir():
	return get_ptm_home()
		
	