<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, teagle.vct.model.*, java.io.*, de.fhg.fokus.teaglewebsite.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
	<jsp:param name="res_reg" value="true"/>
	<jsp:param name="tooltip" value="true"/>
</jsp:include>

<div id="dhtmltooltip"></div>

<script type="text/javascript">

/***********************************************
* Cool DHTML tooltip script- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

var offsetxpoint=-60 //Customize x offset of tooltip
var offsetypoint=20 //Customize y offset of tooltip
var ie=document.all
var ns6=document.getElementById && !document.all
var enabletip=false
if (ie||ns6)
var tipobj=document.all? document.all["dhtmltooltip"] : document.getElementById? document.getElementById("dhtmltooltip") : ""

function ietruebody(){
return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body
}

function ddrivetip(thetext, thecolor, thewidth){
if (ns6||ie){
if (typeof thewidth!="undefined") tipobj.style.width=thewidth+"px"
if (typeof thecolor!="undefined" && thecolor!="") tipobj.style.backgroundColor=thecolor
tipobj.innerHTML=thetext
enabletip=true
return false
}
}

function positiontip(e){
if (enabletip){
var curX=(ns6)?e.pageX : event.clientX+ietruebody().scrollLeft;
var curY=(ns6)?e.pageY : event.clientY+ietruebody().scrollTop;
//Find out how close the mouse is to the corner of the window
var rightedge=ie&&!window.opera? ietruebody().clientWidth-event.clientX-offsetxpoint : window.innerWidth-e.clientX-offsetxpoint-20
var bottomedge=ie&&!window.opera? ietruebody().clientHeight-event.clientY-offsetypoint : window.innerHeight-e.clientY-offsetypoint-20

var leftedge=(offsetxpoint<0)? offsetxpoint*(-1) : -1000

//if the horizontal distance isn't enough to accomodate the width of the context menu
if (rightedge<tipobj.offsetWidth)
//move the horizontal position of the menu to the left by it's width
tipobj.style.left=ie? ietruebody().scrollLeft+event.clientX-tipobj.offsetWidth+"px" : window.pageXOffset+e.clientX-tipobj.offsetWidth+"px"
else if (curX<leftedge)
tipobj.style.left="5px"
else
//position the horizontal position of the menu where the mouse is positioned
tipobj.style.left=curX+offsetxpoint+"px"

//same concept with the vertical position
if (bottomedge<tipobj.offsetHeight)
tipobj.style.top=ie? ietruebody().scrollTop+event.clientY-tipobj.offsetHeight-offsetypoint+"px" : window.pageYOffset+e.clientY-tipobj.offsetHeight-offsetypoint+"px"
else
tipobj.style.top=curY+offsetypoint+"px"
tipobj.style.visibility="visible"
}
}

function hideddrivetip(){
if (ns6||ie){
enabletip=false
tipobj.style.visibility="hidden"
tipobj.style.left="-1000px"
tipobj.style.backgroundColor=''
tipobj.style.width=''
}
}

document.onmousemove=positiontip

</script>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_resources_reg" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">

<%
	int buttonbreite = 50;
	String p = request.getParameter("p");
	String user = request.getUserPrincipal().getName();
	List<? extends Organisation> userOrganisationList;
	
	try {
		if(p == null){
			String provider = "";
			String name = "TypeName";
			String description = "";
			String url = "";
			String price = "";
			
			String resourceName = request.getParameter("name");
			boolean existing = resourceName!=null;
					
			ResourceSpec res = null;
			
			if (existing) {
				res = ModelManager.getInstance().getResourceSpec(resourceName);
				provider = res.getProvider();
				name = res.getCommonName();
				description = res.getDescription();
				url = res.getUrl();
				price = "" + res.getPrice();
			}
%>
		<h2>Teagle Resource Description Wizard</h2>
		
		<p>
		This form accepts a list of configuration fields based on which Teagle
		resources are provisioned. These fields will be compiled as a resource
		description and added to the Teagle library, and made available to the VCT
		tool.
		</p>
		<br>
		<p>
		<b>Move your Mouse over the labels to see the tooltips!</b>
		</p>

		<form method="post" action="resource_registration.jsp" id="fieldsform">
			<input type="hidden" name="p" value="compile">
			<input type="hidden" name="existing" value="<%=existing %>">
			
			<br>
			<h3><span class="tooltipspan" onMouseover="ddrivetip('<b>Resource</b><br>...','#f8f8f8', 300)" onMouseout="hideddrivetip()">Resource</span></h3>
			
			<table class="teagle" border=0>
				<tr>
					<td><span class="tooltipspan" onMouseover="ddrivetip('<b>Provider</b><br>This is the organization the resource will be assigned to.','#f8f8f8', 300)" onMouseout="hideddrivetip()">Provider:</span></td>
					<td>
						<select name="provider">
	<%
							
			userOrganisationList = ModelManager.getInstance().findOrganisationsByUserName(user);
			for (Organisation o : userOrganisationList) {
				if(o.getName().equals(provider)){ 
	%>
								<option selected value="<%=o.getName() %>"><%=o.getName() %></option>
	<%
				}
				else{ 
	%>
								<option value="<%=o.getName() %>"><%=o.getName() %></option>
	<%
				}
			}
	%>
						</select>
					</td>
				</tr>
				<tr>
					<td><span class="tooltipspan" onMouseover="ddrivetip('<b>Type name</b><br>This is the name of the resource type. It can be found in the VCT','#f8f8f8', 300)" onMouseout="hideddrivetip()">Type name:</span></td>
					<td>
						<input type="text" name="resource" size=30 value="<%=name %>">
					</td>
				</tr>
				<tr>
					<td><span class="tooltipspan" onMouseover="ddrivetip('<b>Price</b><br>This is the price of the resource type.','#f8f8f8', 300)" onMouseout="hideddrivetip()">Price:</span></td>
					<td><input name="price" type="text" size="10" maxlength="10" value="<%=price %>" /></td>
				</tr>
				<tr>
					<td><span class="tooltipspan" onMouseover="ddrivetip('<b>Description</b><br>Describe you resource with a few words.','#f8f8f8', 300)" onMouseout="hideddrivetip()">Description:</span></td>
					<td>
						<textarea name="description" cols="50" rows="4"><%=description %></textarea>
					</td>
				</tr>
				<tr>
					<td><span class="tooltipspan" onMouseover="ddrivetip('<b>URL</b><br>You can optionally enter an URL for more detailed information about your resource.','#f8f8f8', 300)" onMouseout="hideddrivetip()">URL:</span></td>
					<td>
						<input type="text" name="url" size=30 value="<%=url %>">
					</td>
				</tr>
			</table>
		
			<br>
			
			<h3><span class="tooltipspan" onMouseover="ddrivetip('<b>Parameters</b><br>In this part the parameters for you resource are configured.','#f8f8f8', 300)" onMouseout="hideddrivetip()">Parameters</span></h3>
			
			<div id=fields>
				<div id=template style="display:none">
					<div id="field{id}">
						<label>Name:</label>
						<input type="text" name="field{id}_name" size=15 value="{name}">
						<label>Type:</label>
						<select name="field{id}_type">
							<option value="string" selected="string">string</option>
							<option value="int" selected="int">int</option>
							<option value="double" selected="double">double</option>
							<option value="boolean" selected="boolean">boolean</option>
							<option value="reference" selected="reference">reference</option>
							<option value="reference array" selected="reference array">reference array</option>
						</select>
						<label>Default:</label>
						<input type="text" name="field{id}_default" size=15 value="{defval}">
						<label>Description:</label>
						<input style="margin-left:2px;" type="text" name="field{id}_description" size=20 value="{description}">
					</div>
				</div>
			</div>
			
			<script type="text/javascript" src="../js/jquery-1.3.2.js"></script>
			<script type="text/javascript">
				var field_num = 0;
				var template;

				function field_add(name, type, defval, description) {
					var text = template;
					function rep(pat, rep) { text = text.replace(pat, rep); }
					
					rep(/display:none/, "display:block");
					rep(/{name}/g, name);
					rep(/{id}/g, ""+field_num);
					rep(/{defval}/g, defval);
					rep(/{description}/g, description);
					
					var sel = 'selected="true"';

					rep(/selected="string"/g,  (type=="string") ? sel : "");
					rep(/selected="int"/g,     (type=="int") ? sel : "");
					rep(/selected="double"/g,  (type=="double") ? sel : "");
					rep(/selected="boolean"/g, (type=="boolean") ? sel : "");
					rep(/selected="reference"/g, (type=="reference") ? sel : "");
					rep(/selected="reference array"/g, (type=="reference array") ? sel : "");
					
					$("#fields").append(text);

					field_num++;
				}

				function field_del() {
					if (field_num > 0) { 				
						field_num--;						
						$("#field" + field_num).remove();
					}
				}
				
				$(document).ready(function(){
					template = $("#template").html();
					$("#template").remove();

					$("#add_btn").click(function() { field_add('field', 'string', '', ''); return false; });
					$("#del_btn").click(function() { field_del(); return false; });
					
<% 
			String action = request.getParameter("action");
			if (action!=null && action.equals("edit_config")) {
				// if editing a config, add all existing fields
				if (resourceName==null)
					throw new ServletException("resource_name param undefined");
			
				for (ConfigParamAtomic field: res.getConfigurationParameters()) {
				// this is awful: first of all, defval needs escaping
%>
					field_add('<%=field.getCommonName()%>', '<%=field.getType()%>', '<%=field.getDefaultValue()%>', '<%=field.getDescription()%>');
<%
				}
			}
			else {
			// if creating a config, start with just 1 field as example
%>
					field_add('field', 'string', '', '');
<%
			}
%>
				});	
			</script>
			
			<br>
			<input id="add_btn" type="button" value="Add Field">
			<input id="del_btn" type="button" value="Remove Field">
			
			<br>
			<br>
			<!--h3>Options</h3>
		
			<input type="checkbox" name="overwrite">Overwrite existing resources</input>
		
			<br-->
			<br>
			<input type="submit" value="Submit">
		</form>
		
	
<%
		}
//*********************************
// COMPILE
//*********************************
		if("compile".equals(p)){
			Exception ex=null;
			
			try {
				// collect name, fields
	
				boolean overwrite = request.getParameter("overwrite") != null;
	
				String owner = request.getUserPrincipal().getName();
				String provider = request.getParameter("provider");
				String resource = request.getParameter("resource");
				String description = request.getParameter("description");
				String url = request.getParameter("url");
				String existing = request.getParameter("existing");
				String price = request.getParameter("price");
	
				if (owner==null||provider==null||resource==null||existing==null)
					throw new Exception("Missing request fields.");
				// owner = owner.toLowerCase(); -- why is this needed ?
				
				//String[] sources = {"bla", "bla"};//TSSGResourceManager.generateSources(request);
				//String libraryCodebase = "http://www.fire-teagle.org/web-start-stable/library/";
				//ClassLoader loader = new Loader(Thread.currentThread().getContextClassLoader(), libraryCodebase);
				//TSSGResourceManager RM = new TSSGResourceManager(loader);
							
				if (existing.equals("false")) {
					ResourceSpec r = ModelManager.getInstance().createResource();
					Organisation o = ModelManager.getInstance().getOrganisation(provider);
					r.setCommonName(resource);
					r.setDescription(description);
					r.getCost().setAmount(Double.valueOf(price));
					r.getCost().setCurrency("Euro");
					r.setUrl(url);
					r.setConfigurationParameters(ConfigFieldCreator.generateConfigFields(request));
					r = ModelManager.getInstance().persist(r);
					o.addResourceSpec(r);
					o = ModelManager.getInstance().persist(o);
%>
		<p>Resource created.</p>
		Back to <a href='my_resources.jsp'>my resources</a>.
		<br>
		<br>
		<p>You can create policies for this resource by clicking <a href="policies.jsp?action=new&editorScope=Resource&resourceName=<%=resource%>">here</a>.</p>
<%
				} else {
					ResourceSpec r = ModelManager.getInstance().getResourceSpec(resource);
					r.setCommonName(resource);
					r.setDescription(description);
					r.getCost().setAmount(Double.valueOf(price));
					r.getCost().setCurrency("Euro");
					r.setUrl(url);
					r.setConfigurationParameters(ConfigFieldCreator.generateConfigFields(request));
					//Resource old_res = RM.getResource(resource); 
					//Resource resDesc = new Resource(old_res.getId(), owner, provider, resource, resource, old_res.getPrice(), old_res.getSynopsis(), old_res.getUrl(), TSSGResourceManager.generateConfigFields(request));
					//RM.setResource(resDesc);
					r = ModelManager.getInstance().persist(r);
%>
		<p>Resource edited.</p>
		Back to <a href='my_resources.jsp'>my resources</a>.
<%
				}
				
				
			} catch (IOException e) { ex=e;
			} catch (Exception e) { ex=e;
			}
			if (ex!=null) {
				ex.printStackTrace();
				String reason = ex.getMessage();
				Throwable cause = ex.getCause();
				if (cause!=null) reason += "\nCause: " + cause.getMessage();

			%>
				<p>Resource not created.</p>
				<pre style='border:thin dashed #c0c0c0; background-color:#faf8fa; font-size: 1em'><code><%=reason%></code></pre>
				<p>Use your browser's back button to review your description and try again.</p>
			<%
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
	<h3>Something went wrong!</h3>
	<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
	<h3>Exception:</h3>
	<code><%=e.toString()%></code>
<%
 	}
%>


		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>
