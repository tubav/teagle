from teagle.t1.T1Client import T1Client

class LegacyTGWClient(T1Client):
	def _get_path(self, identifier):
		return identifier.prefix + "/" + super(LegacyTGWClient, self)._get_path(identifier)
