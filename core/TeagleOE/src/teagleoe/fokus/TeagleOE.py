'''
Created on 10.07.2011

@author: kca
'''

from teagleoe.exc import ParseError, ModelError, OEError, InternalError, OperationNotFoundError
from xml.dom import minidom
import types
from teagle import Identifier
from teagle.oe import OrchestrationResult
from teagle.tgw import LegacyTGWClient
from teagle.TeagleModule import TeagleModule
from ngniutils.exc import errorstr 
from ngniutils.path import quote
from OEInputParser import DynamicReference, OEInputParser
from teagle.t1.T1Resource import T1Resource
from ngniutils.logging.logbook import Logbook
from ngniutils.logging.logtap import BufferingLogTap
from xml.dom.minidom import Element

class TeagleOE(TeagleModule):
	XMLTAG_TESTBED = 'testbed'
	OPT_FORCE_PARENT = False
	
	def __init__(self, tgwurl, *args, **kw):
		super(TeagleOE, self).__init__(*args, **kw)
		self.__tgw = LegacyTGWClient(tgwurl)
		self.__parser = OEInputParser(self.__tgw)

	def orchestrate_legacy(self, vctid):
		path = self.datadir / quote(vctid)
		return self.orchestrate(path.open())
		
	def put_vct_spec(self, vctid, xml):
		path = self.datadir / quote(vctid)
		path.write_bytes(xml.read())
		
	def orchestrate_startVct(self, component):
		c = component
		if c._runtimeid:
			self.logger.debug("considering instance for starting: id=%s config=%s" % (c._runtimeid, c._conf_dict))
			id = Identifier(c._runtimeid)
			instance  = self.__tgw.get_resource(id)
			self.__tgw.execute_method(instance, "start", c._conf_dict)
		return instance
	
	def orchestrate_stopVct(self, component):
		c = component
		if c._runtimeid:
			self.logger.debug("considering instance for stopping: id=%s config=%s" % (c._runtimeid, c._conf_dict))
			id = Identifier(c._runtimeid)
			instance  = self.__tgw.get_resource(id)
			self.__tgw.execute_method(instance, "stop",c._conf_dict)
		return instance

		
	def orchestrate_setVct(self, component):
		c = component
		#self.logger.info("set vct instance with id %s" %c.runtimeid)
		mapping = {}	
		if c._runtimeid:
			self.logger.debug("considering instance for update: id=%s config=%s" % (c._runtimeid, c._conf_dict))
			id = Identifier(c._runtimeid)
			instance  = self.__tgw.get_resource(id)
			oldcfg = instance.config
			oldcfg.pop("identifier", None)
			if oldcfg != c._conf_dict:
				self.logger.debug("updating %s (%s != %s)" % (id, oldcfg, c._conf_dict))
				self.__tgw.update(id, c._conf_dict) 
			else:
				self.logger.debug("No update required for %s (%s == %s)" % (id, oldcfg, c._conf_dict))
		else:
			type = c._type.lower()
			self.logger.info("add instance: parent=%s typename=%s config=%s ptm=%s" % (c._owner and c._owner._realid or "None", type, c._conf_dict, c._ptm))
			owner = c._owner
			if owner and not owner._runtimeid:
				raise InternalError("Parent not provisioned when provisioning %s" % (c._realid, ))
			owner = Identifier(owner and owner._runtimeid or None, c._ptm)
			instance = self.__tgw.add_resource(owner, type, c._conf_dict)
			c._runtimeid = instance.identifier
			self.logger.info("Instance created new id and config: %s and %s " %(instance.identifier,instance.config))
		return instance
		
	def orchestrate(self, xml, operation):
		self.logger.info("Received a request with this xml %s"%(xml,))
		try:				
			#TODO: parallel execution, async, rollback
			with BufferingLogTap(name = "Orchestration") as tap:
				mapping = {}	
				opname = "Orchestration"
				testbed = self.prepareInput(xml)
				self.logger.info("Orchestrating...")
				for c in testbed._sortedcomponents:
					for k, v in c._conf_dict.items():
						if isinstance(v, DynamicReference):
							self.logger.debug("Resolving dynid %s" % (v.identifier, ))
							c._conf_dict[k] = T1Resource(mapping[v.identifier], self.__tgw)
							self.logger.info("Resolved dynid %s -> %s" % (v.identifier, c._conf_dict[k]))
					try:
						fun = getattr(self, 'orchestrate_%s' %(operation,))
					except AttributeError:
						raise OperationNotFoundError(operation)
					##############
					c._conf_dict["vctname"] = testbed._name
					instance = fun(c)
					
					#instance = self.orchestrate_startVct(c)
					self.logger.info("Finished processing %s" % (c._id, ))
					mapping[c._id] = instance.identifier
				 	#instance_data = []
					#instance_data.append(instance)	
				self.logger.info("Orchestration finished.")
		except Exception, e:
			self.logger.exception("Error during orchestration")
			return OrchestrationResult(1, errorstr(e), log = Logbook(opname, "Orchestration Engine", entries = tap.log))
		
		return OrchestrationResult(0, "success", mapping, log = Logbook(opname, "Orchestration Engine", entries = tap.log))
		
	def prepareInput(self, xml):
		doc = minidom.parse(xml)
		#self.logger.debug("Orchestration Input: " + doc.toxml())
		try:
			testbed = self.analyseXml(doc)
			self.computeDependencies(testbed)
			self.sortComponents(testbed)
			self.showInputStats(testbed)
			return testbed
		finally:
			doc.unlink()

	def analyseXml(self,doc):
		usedptms = set()
		compNodes,connNodes = [], []
		nodesDict = {}
		testbeds = doc.getElementsByTagName(self.XMLTAG_TESTBED)
		if not testbeds: ## OLD VCT Tool version try "Testbed"
			testbeds = doc.getElementsByTagName("Testbed")
		if not testbeds:
			raise ParseError("No testbeds found in file", doc.toxml())
		if len(testbeds)>1:
			raise ParseError("More than one testbed in file", doc.toxml())
		testbed = testbeds[0]

		################
		testbed._name = testbed.getElementsByTagName("name")[0].childNodes[0].data

		components = testbed.getElementsByTagName("components")
		if components and len(components)==1:
			components = components[0]
		connections = testbed.getElementsByTagName("connections")
		if connections and len(connections)==1:
			connections = connections[0]
		for comp in components.childNodes:
			if comp.nodeType == minidom.Node.ELEMENT_NODE:
				compNodes.append(comp)
				compid = self.retrieveXmlValue(comp,'identifier')
				if not compid:
					compid = self.retrieveXmlValue(comp,'id')
					if not compid:
						raise ParseError("Id attribute missing on %s (%s)" % (comp.tagName, comp.toxml()))
				comp._id = compid
				comp._realid = compid.partition(".resources.")[-1]
				if not comp._realid:
					self.logger.warning(".resources. not found in Id. Will try to go on with %s" % (comp._id, ))
					comp._realid = comp._id
				comp._qtype = comp.tagName
				ptmname, _, typename  = comp.tagName.partition(".")
				
				if not typename:
					self.logger.warning("not . found in tagName %s" % (comp.tagName, ))
					typename = comp.tagName
				elif typename.startswith("resources."):
					typename = typename[10:]
				
				comp._type = typename
				comp._conf = comp.getElementsByTagName("configuration")[0]
				comp._conf_dict = self.__parser._unserialize_config(comp._conf)
				comp._conftxt = self.retrieveConfTxt(comp)
				comp._state = self.retrieveState(comp)
				comp._owner = None
				ptm = self.retrievePtmId(comp)
				comp._ptm = ptm
				comp._runtimeid = self.retrieveRunTimeId(comp)
				if not self.OPT_FORCE_PARENT:
					parentid,localid = self.splitComponentId(compid)
					parentid = '/' + parentid.replace('.','/')
				else:
					parentid,localid = self.retrieveRuntimeParentId(comp),""
				comp._parentid = parentid
				comp._localid = localid
				
				usedptms.add(ptm)
				nodesDict[comp._id] = comp
		for conn in connections.childNodes:
			if conn.nodeType == minidom.Node.ELEMENT_NODE:
				connNodes.append(conn)
				conn._id = self.retrieveXmlValue(conn,'id')
				conn._srcId = self.retrieveXmlValue(
					conn.getElementsByTagName("src")[0],'id')
				conn._dstId = self.retrieveXmlValue(
					conn.getElementsByTagName("dst")[0],'id')

				conn._type = self.retrieveXmlValue(conn,'type')
				conn._src = nodesDict.get(conn._srcId)
				conn._dst = nodesDict.get(conn._dstId)
				if not conn._src:
					raise ModelError(
						"Connection '%s' with unknown source '%s'" % (
						conn._id,conn._srcId),doc.toxml())
				if not conn._dst:
					raise ModelError(
						"Connection '%s' with unknown destination '%s'" % (
						conn._id,conn._dstId),doc.toxml())
				if conn._type=="contains":
					dstowner = getattr(conn._dst,'_owner',None)
					if dstowner:
						#raise Vct2SpatelError(
						#	"Component with more than one parent",
						#	conn._dst._id)
						conn._dst._shared = True
						if type(conn._dst._owner)!=types.ListType:
							conn._dst._owner = [conn._dst._owner]
							conn._dst._ownerlink = [conn._dst._ownerlink]
						conn._dst._owner.append(conn._src)
						conn._dst._ownerlink.append(conn)
					else:
						dst = conn._dst
						src = conn._src
						dst._owner = src
						dst._ownerlink = conn
						if dst._ptm != src._ptm and not dst._runtimeid:
							self.logger.warning("New instance %s is on different PTM (%s) than parent %s (%s). Forcing to %s" % (dst._id, dst._ptm, src._id, src._ptm, src._ptm))
							dst._ptm = src._ptm
		testbed._components = compNodes
		testbed._connections = connNodes
		testbed._nodesDict = nodesDict
		testbed._usedptms = usedptms
		return testbed        
	
	def splitComponentId(self,compid):
		dotpos = compid.rfind('.')
		if dotpos!=-1:
			parentid = compid[0:dotpos];localid = compid[dotpos+1:]
			hyphenpos = localid.find('-')
			if hyphenpos!=-1:
				parentid += "." + localid[:hyphenpos]
				localid = localid[hyphenpos+1:]
		else:
			parentid = ""
			localid = compid
		return parentid,localid
	
	def retrieveRunTimeId(self,comp):
		if comp._state and comp._state.lower() == "provisioned": 
			return comp._id
		return None
	
	def retrievePtmId(self,comp):
		if comp._id.startswith("//"):
			pos = comp._id.find("/",2)
			if pos!=-1:
				ptm = comp._id[2:pos]
				print "Matched ptm",comp._id,"==>",ptm
				return ptm
		dotpos = comp._id.find(".")
		if dotpos!=-1:
			ptm = comp._id[:dotpos]
			print "Implicit ptm",comp._id,"==>",ptm
			return ptm
		raise OEError("Unable to derive PTM from id %s" % (comp, ))

	def retrieveXmlValue(self,elt,key):
		#subitems = elt.getElementsByTagName(key)
		subitems = [ e for e in elt.childNodes if isinstance(e, Element) and e.tagName == key ]
		if subitems: 
			childNodes = subitems[0].childNodes
			if childNodes:
				return childNodes[0].data
		return None
	
	def retrieveConfTxt(self,comp):
		if not comp._conf: 
			raise ParseError("")
		xmltxt = comp._conf.toxml()
		pos1 = xmltxt.find("<configuration>")
		pos2 = xmltxt.rfind("</configuration>")
		if pos1!=-1 and pos2!=-1:
			xmltxt = xmltxt[pos1+len('<configuration>'):pos2].strip()
		return xmltxt
	
	def retrieveState(self,comp):
		state = self.retrieveXmlValue(comp,'state')
		if not state:
			raise ParseError("State missing on component: %s" % (comp.toxml(), ))
		return state
	
	def computeDependencies(self,testbed):
		## direct dependencies
		for comp in testbed._components:
			depends = []
			for conn in testbed._connections:
				if not conn._type:
					raise OEError("Type missing on: " + conn.toxml())
				if conn._type != "contains":
					src,dst = conn._src,conn._dst
				else: 
					src,dst = conn._dst,conn._src
				if src == comp:
					if dst != comp and dst not in depends:
						depends.append(dst)
			comp._depends = depends
		## recursive dependencies
		for comp in testbed._components:		
			comp._alldeps = self.computeAllDependencies(comp,comp,[])

	def computeAllDependencies(self,top,curr,alldeps):
		for x in curr._depends:
			if x != top and x not in alldeps:
				alldeps.append(x)
				self.computeAllDependencies(top,x,alldeps)
		return alldeps
	
	def sortComponents(self,testbed):
		res = []
		ipos = -1
		for i in testbed._components:
			ipos += 1
			xpos = -1
			for x in res[:]:
				xpos += 1
				status = self.compare(i,x)
				if status==-1:
					res.insert(xpos,i)
					break
			else:
				res.append(i)
			#print "LIST(%d): " % ipos, [e._id for e in res]
		testbed._sortedcomponents = res
		return res

	def compare(self,comp1,comp2):
		if comp1 in comp2._alldeps: 
			return -1
		if comp2 in comp1._alldeps: 
			return 1
		#print "comparing %s,%s => %s" % (comp1._id,comp2._id,res) 
		return 0
	
	def showInputStats(self,testbed):
		self.logger.debug("testbed")
		self.logger.debug( "components")
		for comp in testbed._components:
			self.logger.debug( "  %s id=%s" % (comp.tagName,comp._id))
		self.logger.debug( "connections")
		for conn in testbed._connections:
			self.logger.debug( "  %s:  %s -> %s" % (conn._id,conn._src._id,conn._dst._id))
		self.logger.debug( "dependencies")
		for comp in testbed._components:
			deptxt = ",".join([x._id for x in comp._alldeps])
			self.logger.debug( "  %s id=%s dependsOn: %s" % (comp.tagName,comp._id,deptxt))
		
		self.logger.debug( "sorted components")
		for comp in testbed._sortedcomponents:
			self.logger.debug( "  %s id=%s" % (comp.tagName,comp._id))
	
