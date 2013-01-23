#! /usr/bin/env python

from BaseSerializer import BaseSerializer
from T1Entity import T1Entity

class T1EntitySerializer(BaseSerializer):
	def __init__(self, t1client, prefix, *args, **kw):
		super(T1EntitySerializer, self).__init__(prefix = prefix, *args, **kw)
		self.t1client = t1client

	def _unserialize_reference(self, id):
		t1Ent = T1Entity(id, self.t1client, None)
		return t1Ent
	
	# assuming config dict is a dict of dicts
	#def serialize(self, config_dict):
	#	raise NotImplementedError()

