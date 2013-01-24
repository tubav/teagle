'''
Created on 09.11.2011

@author: kca
'''

from cms.app_base import CMSApp
from cms.apphook_pool import apphook_pool
from django.utils.translation import ugettext_lazy as _

class RepoguiApphook(CMSApp):
    name = _("Repogui Apphook")
    urls = ["teagle.repository.webapp.django.repogui"]

apphook_pool.register(RepoguiApphook)