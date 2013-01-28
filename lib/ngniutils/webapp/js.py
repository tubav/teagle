'''
Created on 02.09.2011

@author: kca
'''

from .. import uc
from collections import Mapping
from _abcoll import Iterable

_JOINSTR = u",\n"

_id = lambda s: unicode(str(s))

class js_safe(unicode):
	pass

class js_name(js_safe):
	def __new__(cls, s, name):
		return unicode.__new__(cls, s)
	
	def __init__(self, s, name):
		self.name = _id(name)

def js_str(s, quote = u'"'):
	return js_safe(quote + uc(s).replace("\\", "\\\\").replace(quote, "\\" + quote).replace("\n", "\\n").replace("\r", "\\r") + quote)

def js_value(o):
	if o is None:
		return "null"
	if isinstance(o, js_name):
		return o.name
	if isinstance(o, (int, float, js_safe)):
		return unicode(o)
	if isinstance(o, basestring):
		return js_str(o)
	if isinstance(o, Mapping):
		return js_dict(o)
	if isinstance(o, Iterable):
		return js_list(o);
	if isinstance(o, bool):
		return unicode(o).lower()
	raise TypeError(o)
	
def js_list(l, conv = js_value):
	return js_safe(u'[' + _JOINSTR.join(map(conv, l)) + u']')

def js_dict(d, conv = js_value, keyconv = js_str):
	return js_safe(u"{" + _JOINSTR.join([ keyconv(k) + u":" + conv(v) for k, v in dict(d).iteritems() ]) + u"}")

def js_var(name, value, local = True, endstatement = True, conv = js_value):
	name = _id(name)
	s = name + "=" + conv(value)
	if endstatement:
		s += ";"
	if local:
		s = "var " + s
	return js_name(s, name = name)

def js_vars(vars, local = True):
	return js_safe("".join([ js_var(k, v, local) for k, v in dict(vars).iteritems() ]))

def js_call(method, *args, **kw):
	s = _id(method) + "(" + _JOINSTR.join(map(js_value, args)) + ")"
	if kw.get("endstatement", True):
		s += ";"
	return js_safe(s)

def js_assign_call(method, name, *args, **kw):
	return js_var(name, js_call(method, *args), local = kw.get("local", True), endstatement = False)
