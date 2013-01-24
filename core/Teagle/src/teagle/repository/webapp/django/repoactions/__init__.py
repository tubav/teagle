
from controller import get_controller_class

repoactions_controller = get_controller_class()()

urlpatterns = repoactions_controller.urlpatterns
