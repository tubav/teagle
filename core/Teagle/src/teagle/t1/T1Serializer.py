#! /usr/bin/env python

from T1Resource import T1Resource
from ..serializer import TeagleSerializer

class T1Serializer(TeagleSerializer):
	def __init__(self, t1client, *args, **kw):
		super(T1Serializer, self).__init__(*args, **kw)
		self.t1client = t1client

	def _unserialize_reference(self, id):
		return T1Resource(id, self.t1client, None)
	
