'''
Created on 05.10.2010

@author: kca
'''

from ptm.ManagerServer import ManagerServer
from Identifier import Identifier

logger = None

def _run_ra(fullname, manager, parent):
	logger.info("starting RA %s" % (fullname, ))
	
	module, _, klassname = fullname.rpartition(".")
	
	if not module or not klassname:
		raise ValueError("Not a fully qualified class name: %s" % (fullname, ))
	
	module = __import__(module, fromlist = [ klassname ])
	klass = getattr(module, klassname)
	if parent:
		parent = Identifier(parent, need_full = True)
	return klass(parent = parent, manager = manager)

def RARunner(fullnames, port = None, regurl = None, parent = None):
	import logging
	global logger
	logger = logging.getLogger("ptm")
	console = logging.StreamHandler()
	formatter = logging.Formatter('Manager: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
	console.setFormatter(formatter)
	console.setLevel(logging.DEBUG)
	logger.setLevel(logging.DEBUG)
	logger.addHandler(console)

	m = ManagerServer(None, port = port, registry_url = regurl)

	if isinstance(fullnames, basestring):
		_run_ra(fullnames, m.manager, parent)
	else:
		for fullname in fullnames:
			_run_ra(fullname, m.manager, parent)

	m.serve_forever()
	
def main():
	from optparse import OptionParser

	parser = OptionParser()
	parser.add_option("-r", "--registry_url", dest="registry_url", help="Set URL of the ptm registry", default=None)
	parser.add_option("-p", "--port", type="int", dest="port", help="Set port to bind to", default = 8001)
	parser.add_option("-P", "--parent", type="string", dest="parent", help="parent instance of this RA", default = None)

	(options, args) = parser.parse_args()
	
	if not args:
		raise Exception("Missing argument.")
	
	RARunner(args, port = options.port, regurl = options.registry_url, parent = options.parent)

if __name__ == '__main__':
	main()
		