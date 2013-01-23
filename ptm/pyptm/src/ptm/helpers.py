#! /usr/bin/env python

def is_iterable(o):
	return hasattr(o, "__iter__") or hasattr(o, "__getitem__")

def is_mapping(o):
	return hasattr(o, "__getitem__") and (hasattr(o, "keys") or hasattr(o, "has_key"))
	
def is_primitive(o):
	return o is None or isinstance(o, (int, long, float, basestring, bool, complex))

def is_mutable(o):
	return o is not None and is_primitive(o) and not isinstance(o, (tuple, frozenset))

