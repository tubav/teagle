<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ page import="java.util.*" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
        <jsp:param name="boxes" value="no" />
        <jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
        <jsp:param name="current" value="vct" />
        <jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
				<h2>VCT</h2>
				<h4>Here you you can launch a demo version of the Teagle VCT tool</h4>
				<br/>
<%
    String user   = request.getUserPrincipal().getName();
    String cookie = request.getSession().getId();
%>
				<span style="display:block">
					<a href="jnlp.jsp?user=<%=user%>&cookie=<%=cookie%>&version=devel&type=.jnlp">
						<strong>Launch the VCT tool - development version</strong> (Updated 2010.12.06 - 16:30)
						<span style="display:none"><strong>(deployment in progress)</strong></span>
					</a>
					<br>
					<br>
				</span>
				
				<br>
<pre>
ChangeLog:
10.12.06
- major rewrite

10.06.25
- create missing configuration data for singletons regsitered by the TGW

10.06.24
- workaround / ignore some repo problems

10.06.20
- integration of the policy engine. 

10.06.17
- fixed deletion behaviour

10.06.17
- delete unused resource instances regardless of their state
- fixed a bug regarding renamed resource instances when loading a VCT

10.06.16
- ignore problematic repo entries

10.06.15
- fixed "clone as unprovisioned"

10.06.07
- better repo access behaviour. Things should be much faster.
- sort available resources alphabetically
- enable resource types to be supported by several PTMs
- booking summary is back
- booking confirmation page is back

10.06.01
- fixed an error regarding deletion of resource instances. 

10.05.31
- fixed a crash that occurs when editing configuration of resource instances 

10.05.27
- delete unprovisioned resource instances from the repo when they are not in any vct

10.05.27
- fixed an error regarding ordering of references
- only show provisioned instances in the instances tab
- don't show reference fields in configuration pane

10.05.26
- fixed ordering of configuration parameters 
- disallow multiple parents for a single resource instance
- remove some obsolete context menu entries

10.05.20
- fixed a bug where configuration data would not be retrieved from the repo

10.05.20
- fixed a deployment bug

10.05.17
- Integration with TSSG repository implementation
- direct acces to the repo is available here: http://repos.pii.tssg.org:8080/repository/

10.03.15
- add suport for lists of references
- named connection pins
- policy engine integration

09.07.30
- restructure the resource library. This means that most old testbeds will not
  load, but that new ones can be created/saved/loaded.

09.06.21
- fix deploy bug that caused all 11MB of jars to be downloaded each time
  (startup should be MUCH faster)

09.06.19
- resource description form: http://www.fire-teagle.org/resource-wizard/

09.06.17
- $dynid references in the exported xml output

09.06.16
- add reservation resources

09.06.12
- require authentication for /ptm/ and /repo/ services.

09.06.11
- elliminate the use of proxies. the vct tool now connects to
  www.fire-teagle.org:80 exclusively

09.06.08
- allow editing of ids for resources and connections
- update ids in connections when component id changes
- save confirmations on new/load/quit
- switch to hierarchical (e.g. /pnode_0/vnode_0/mysql_1) resource ids, as used by fokus ptm
- bring direct ptm/vct integration up to date

09.06.03
- implement removing of connections
- editing of connection properties (rules)
- switch between connection types from context menu instead of just clicking
- fix focus on startup (on windows)
- explanatory message if connection to repository fails

09.06.01
- merge website users with vct (and repository) users

09.05.29
- remove unused jars (starting should be faster now)
- minor visual fix when removing components
- serve the right SWT from the jnlp, not based on browser UA
- fix grid lost-resources-on-left-side bug
- fix XMLRPC startup exception
- minor ui tweak for rule text editor

09.05.28
- vct: 64bit java on 64bit windows support
- vct: deploy separate development version for testing

09.05.27
- vct: support removing of VCT resources from the grid

09.05.26
- using JDK 1.5 since we don't really need 1.6 yet
- vct: UI for editing dependency rules among resources
</pre>

		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>
