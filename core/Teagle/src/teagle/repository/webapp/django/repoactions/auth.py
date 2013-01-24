'''
Created on 01.09.2011

@author: kca
'''
from teagle.repository.webapp.django import get_repo
from ngniutils.logging import LoggerMixin

'''
Created on 26.04.2011

@author: kca
'''

from django.contrib.auth.models import User
from teagle.repository.entities import Person

class TeagleAuthBackend(LoggerMixin):
    def __init__(self, *args, **kw):
        super(TeagleAuthBackend, self).__init__(*args, **kw)
        
        self.__api = get_repo()
    
    def authenticate(self, username, password):
        try:
            teagleuser = self.__api.authenticate_user(username, password)
        except Exception, e:
            self.logger.info("Failed to authenticate user %s: %s" % (username, e))
            return None
        
        try:
            user = User.objects.get(username=username)
        except User.DoesNotExist:
            user = User(username = username, password = "")
            user.set_unusable_password()
            user.save()
          
        user.teagleuser = teagleuser
        return user
    
    def get_user(self, user_id):
        try:
            user = User.objects.get(pk=user_id)
        except User.DoesNotExist:
            return None
        
        user.teagleuser = self.__api.get_unique_entity(Person, commonName = user.username)
        user.set_unusable_password()
        
        return user
    