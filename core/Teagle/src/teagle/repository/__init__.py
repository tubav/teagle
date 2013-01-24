import teagle
import sys
from ngniutils.logging import get_logger

entities = "teagle.repository.entities_" + teagle.get_teagle_flavour()
get_logger().info("Using %s as entities module" % (entities, ))
entities = __import__(entities, globals(), locals(), ("get_entity_classes", ))
sys.modules["teagle.repository.entities"] = entities

from TeagleRepository import TeagleRepository
from Entity import Entity, Field, Password

DEFAULT_URI = "http://localhost:8080/repository/rest" 

def make_repoclient(url, username = None, password = None, classes = None):
	if teagle.single_process_environment():
		from tssg import TSSGRepository as _RepoClass
	else:
		from tssg.multiprocess import SynchronizedTSSGRepository as _RepoClass
	return _RepoClass(url, username = username, password = password, classes = classes)
