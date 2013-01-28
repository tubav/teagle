'''
Created on 01.09.2011

@author: kca
'''

from django.db import models
from django.utils.translation import ugettext_lazy as _
from cms.models import CMSPlugin
from cms.models.pagemodel import Page


class ChildrenTeaser(CMSPlugin):
	placeholder_name = models.CharField(_("placeholder_name"), max_length=50, blank=False, null=False, help_text=_("The name of the placeholder to use as Teaser"))
	target_page = models.ForeignKey(Page, null = True)
	max_items = models.IntegerField(null = True)