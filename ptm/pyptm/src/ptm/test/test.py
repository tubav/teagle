'''
Created on 13.08.2010

@author: kca
'''

from ptm.ManagerServer import ManagerServer
from ptm.ra.SimpleTestAdapter import SimpleTestAdapter

def main():
    import logging
    logger = logging.getLogger("ptm")
    console = logging.StreamHandler()
    formatter = logging.Formatter('Manager: %(levelname)s [%(funcName)s(%(filename)s:%(lineno)s] - %(message)s')
    console.setFormatter(formatter)
    console.setLevel(logging.DEBUG)
    logger.setLevel(logging.DEBUG)
    logger.addHandler(console)
    
    from optparse import OptionParser

    parser = OptionParser()
    parser.add_option("-r", "--registry_url", dest="registry_url", help="Set URL of the ptm registry", default=None)
    (options, _args) = parser.parse_args()

    m = ManagerServer(None, registry_url = options.registry_url)
    _a = SimpleTestAdapter(manager = m.manager, parent = None)
    m.serve_forever()

if __name__ == '__main__':
    main()