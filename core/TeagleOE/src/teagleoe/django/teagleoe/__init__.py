from controller import LegacyOEController
from teagleoe.django.teagleoe.controller import OEController

legacy_oecontroller = LegacyOEController("teagleoe_legacy")
oecontroller = OEController("teagleoe")

urlpatterns = oecontroller.urlpatterns + legacy_oecontroller.urlpatterns