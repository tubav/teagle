'''
Created on 31.08.2011

@author: kca
'''

from django.db import models
from django.utils.translation import ugettext_lazy as _

class Applicant(models.Model):
	fullname = models.CharField(_("fullname"), blank = False, null = False, max_length=50)
	username = models.CharField(_("username"), blank = False, null = False, max_length=50)
	password = models.CharField(_("password"), blank = False, null = False, max_length=50)
	organisation = models.CharField(_("organisation"), blank = False, null = False, max_length=255)
	email = models.CharField(_("email"), blank = False, null = False, max_length=255)
