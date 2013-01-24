'''
Created on 03.09.2011

@author: kca
'''
from ngniutils.net.django.AbstractController import AbstractController
from teagle.repository.entities import get_entity_class, Ptm
from teagle.repository.webapp.django import get_repo
from ngniutils.net.django.exc import MissingParameter
from operator import attrgetter
from json import dumps
from math import ceil
from ngniutils import uc
from teagle.repository.Entity import Entity
from teagle.repository.exc import NoEntityFound

def get_grid_fields(klass):
	return filter(attrgetter("display_inline"), klass.get_fields())

class GridController(AbstractController):
	mimetype = "application/json"
	
	def _convert_value(self, v):
		if v is None:
			return ""
		if isinstance(v, Entity):
			return v.name
		return uc(v)
	
	def _handle_request(self, request):
		klass = get_entity_class(self._get_param("entity_type"))
		
		try:
			page = int(self._get_param("page")) - 1
		except (TypeError, ValueError, MissingParameter):
			page = 0
			
		try:
			rows = int(self._get_param("rows"))
		except (TypeError, ValueError, MissingParameter):
			rows = 10
			
		repo = get_repo()
		filterargs = request.REQUEST.getlist("owns[]")
		owns = {}
		for f in filterargs:
			key, _, valuestr = f.partition("=")
			valklass, _, valid = valuestr.split("-")
			if key and valklass and valuestr:
				try:
					value = repo.get_entity(valklass, valid)
				except NoEntityFound:
					self.logger.exception("error filtering result according to %s", f)
					continue
				owns[key] = value
				
		if klass.__name__ == "ResourceSpec":
			try:
				ptm_id = request.REQUEST["ptm"]
				ptm = repo.get_entity( Ptm, id = ptm_id)
#				entities = ptm.resourceSpecs#needs sorting
				ptm_info = ptm.describedByPtmInfo
				owns["supportedBy"] = ptm_info
			except KeyError:
				pass
		
		entities = repo.list_entities(klass, order_by = request.REQUEST.get("sidx"), order_desc = request.REQUEST.get("sord", "").lower() == "desc", owns = owns)
		records = len(entities)
		
		offset = rows * page
		if offset >= records:
			offset = 0
					
		rowdata = [ {
					"id": str(e.id),
					"cell": [ self._convert_value(getattr(e, f.name)) for f in get_grid_fields(klass )],
				} for e in entities[offset:offset + rows] ]

		result = {
			"total": str(int(ceil(float(records) / float(rows)))),
			"records": str(records),
			"page": str(page + 1),
			"rows": rowdata
		}
				
		return dumps(result)
