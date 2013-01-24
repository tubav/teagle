from django.conf.urls.defaults import patterns
import teagleoe.django.teagleoe
import teaglegw.django.teaglegw
import teaglerp.django.teaglerp
import teagle.repository.webapp.django.repoactions
import teagle.repository.webapp.django.repogui
import teagle.repository.webapp.django.gridbackend
from django.contrib.staticfiles.urls import staticfiles_urlpatterns

# Uncomment the next two lines to enable the admin:
# from django.contrib import admin
# admin.autodiscover()

urlpatterns = patterns('',

)


urlpatterns += teagleoe.django.teagleoe.urlpatterns

urlpatterns += teaglegw.django.teaglegw.urlpatterns

urlpatterns += teaglerp.django.teaglerp.urlpatterns

urlpatterns += teagle.repository.webapp.django.repoactions.urlpatterns
urlpatterns += teagle.repository.webapp.django.repogui.urlpatterns 
urlpatterns += teagle.repository.webapp.django.gridbackend.urlpatterns

urlpatterns += staticfiles_urlpatterns()

