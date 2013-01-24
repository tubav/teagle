from teagle.repository.webapp.django.grid import GridController

grid_controller = GridController(name = "grid")

urlpatterns = grid_controller.urlpatterns