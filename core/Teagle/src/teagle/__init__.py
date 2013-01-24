
import os
from ngniutils.logging import get_logger
from base import Identifier, Resource, Testbed

_home = None

DEFAULT_FLAVOUR="default"

def get_teagle_home():
	global _home
	if _home is None:
		from ngniutils.path import path as Path
		
		_home = os.getenv("TEAGLE_HOME")
		if not _home:
			get_logger().warn("TEAGLE_HOME is not set. Assuming /opt/teagle")
			_home = "/opt/teagle"
		_home = Path(_home)
			
	return _home

def get_teagle_flavour():
	try:
		return get_teagle_flavour.__flavour__
	except AttributeError:
		f = os.getenv("TEAGLE_FLAVOUR", "")
		if not f:
			get_logger().info("TEAGLE_FLAVOUR is not set. Assuming default.")
			f = DEFAULT_FLAVOUR
		else:
			get_logger().debug("TEAGLE_FLAVOUR is %s", f)
		get_teagle_flavour.__flavour__ = f
		return f

def get_storage_dir():
	return get_teagle_home() / "var"

def single_process_environment():
	return os.getenv("TEAGLE_SINGLE_PROCESS", False)

def is_testrun():
	return os.getenv("TEAGLE_TESTRUN", False)