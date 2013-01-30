<%@page import="de.tu.av.teagle.openpe.editor.ItemHtmlElementEnum"%>
<%@page import="de.tu.av.teagle.openpe.editor.EditorDataItem"%>
<%@page import="de.tu.av.openpe.xcapclient.RepositoryException"%>
<%@page import="de.tu.av.openpe.xcapclient.PolicyObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page
	import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page
	import="de.fhg.fokus.teaglewebsite.policyeditor.TeaglePolicyEditorViewer"%>
<%@page import="gen.openpe.identifiers.policy.PolicyIdentifier"%>

<%@page
	import="de.fhg.fokus.teaglewebsite.policyeditor.OrganisationPoliciesEditor"%>
<%@page import="de.fhg.fokus.teaglewebsite.policyeditor.UsersPoliciesEditor"%>
<jsp:useBean
	id="policyViewer"
	class="de.fhg.fokus.teaglewebsite.policyeditor.TeaglePolicyEditorViewer"
	scope="session"/>

<%@page
	import="de.fhg.fokus.teaglewebsite.policyeditor.ResourcePoliciesEditor"%>
	
<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../" />
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
	<jsp:param name="current" value="vct_control_policies" />
	<jsp:param name="depth" value="../" />
</jsp:include>


<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
<div id="col3_innen" class="floatbox">

<%
	policyViewer.setServletConfig(config);
	String action = request.getParameter("action");
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	List<String> editorScopes =  new ArrayList<String>();
	try{
	  if (action == null)
	  {	  
		  
		  if("admin".equals(user)) //the admin view of policies listing. He should be able to view all policies and be able to be modify only the global ones.
		  {	
			  	policyViewer.setUserFilter(null);
		  }
		  else
		  { // partener view of policies listing. It displays only policies related to the resources created by the user itself.
			policyViewer.setUserFilter(user);		  
		  }
			editorScopes.add(OrganisationPoliciesEditor.EditorScope);
			editorScopes.add(ResourcePoliciesEditor.EditorScope); 			  
			editorScopes.add(UsersPoliciesEditor.EditorScope); 			  

%>

<%
		for (int i = 0; i < editorScopes.size();i++)
		{
			String editorScope = editorScopes.get(i);
	 		List<PolicyIdentifier> piList = policyViewer.getUserFilteredPolicyObject(editorScope);
%>

<h2>defined <%=editorScope%> policies</h2>

<div style="max-width: 100%; overflow: auto">
<table class="teagle" border="0">
	<thead>
		<tr>
			<th bgcolor="#dddddd" style="height: 25px"><span class="tooltipspan2" onMouseover="ddrivetip('Id of the policy.','#ddddda', 300)" onMouseout="hideddrivetip()">id</span></th>
			<th bgcolor="#dddddd"><span class="tooltipspan2" onMouseover="ddrivetip(<%=policyViewer.getIdentityTipsMessage()%>,'#ddddda', 300)" onMouseout="hideddrivetip()"><%=policyViewer.getIdentityType(editorScope)%></span></th>
			<th bgcolor="#dddddd"><span class="tooltipspan2" onMouseover="ddrivetip(<%=policyViewer.getScopeTipsMessage(editorScope)%>,'#ddddda', 300)" onMouseout="hideddrivetip()">Scope</span></th>
			<th bgcolor="#dddddd"><span class="tooltipspan2" onMouseover="ddrivetip(<%=policyViewer.getOperationTipsMessage()%>,'#ddddda', 300)" onMouseout="hideddrivetip()">Operation</span></th>
		</tr>
	</thead>
	<% 	
	 	for (PolicyIdentifier pi:piList )
	 	{
%>
	<tr>
		<td bgcolor="#efefef"><%= pi.getId()%></td>
		<td bgcolor="#efefef"><strong><%= pi.getIdentity()%></strong></td>
		<td bgcolor="#efefef"><%= pi.getScope()%></td>
		<td bgcolor="#efefef"><%= pi.getEvent()%></td>
		<td bgcolor="#efefef"><a href="?action=edit&id=<%=pi.getId()%>&editorScope=<%=editorScope%>">edit</a></td>
		<td bgcolor="#efefef"><a
			href="?action=deleteConfirmation&id=<%=pi.getId()%>&editorScope=<%=editorScope%>">delete</a></td>
	</tr>
	<%
 		}
%>
</table>
</div>
<br>

<a href="?action=new&editorScope=<%=editorScope%>">Create</a> a new <span class="tooltipspan" onMouseover="ddrivetip(<%=policyViewer.getGeneralTipsMessage(editorScope)%>,'#f8f8f8', 300)" onMouseout="hideddrivetip()"><%=editorScope%> policy</span>.
<%
}
	}else if(action.equals("set"))
	{
		String id = request.getParameter("id");
		String editorScope = request.getParameter("editorScope");
		if(id != null && editorScope != null)
		{
			policyViewer.saveEditedDataObject(editorScope, id, request.getParameterMap());
%>
<p>Policy successfully updated!</p>
<p><input type="button" value="Back"
	style="width:<%=buttonbreite%>pt"
	onClick="window.location='policies.jsp'" /></p>
<% 			
		}			
	}else if (action.equals("delete"))
	{
		String id = request.getParameter("id");
		String editorScope = request.getParameter("editorScope"); 
		if(id != null && editorScope != null)
		{
			policyViewer.deleteData(editorScope, id);
%>
<p>Policy successfully deleted!</p>
<p><input type="button" value="Back"
	style="width:<%=buttonbreite%>pt"
	onClick="window.location='policies.jsp'" /></p>
<% 
		}
	}else if(action.equals("deleteConfirmation"))
	{
		String id = request.getParameter("id");
		String editorScope = request.getParameter("editorScope");
			if(id != null && editorScope!=null)
			{		
%>
<h3>Are you sure you want to delete the policy with id <%=id%></h3>
<p><input type="button" value="Cancel"
	style="width:<%=buttonbreite%>pt"
	onClick="window.location='policies.jsp'" /> <input type="button"
	value="Delete" style="width:<%=buttonbreite%>pt"
	onClick="window.location='?action=delete&id=<%=id %>&editorScope=<%=editorScope%>&sure=yes'" /></p>
<%
			}
		}
		else if (action.equals("new") || action.equals("edit"))
		{
			
			String editorScope = request.getParameter("editorScope");
			String resourceName = request.getParameter("resourceName");
			String id = null;
			if (editorScope != null)
			{
				
				List<EditorDataItem> newObject = new ArrayList<EditorDataItem>();
				if(action.equals("new"))
					newObject = policyViewer.getNewDataObject(editorScope);
				else{
					id = request.getParameter("id");
					newObject = policyViewer.getToEditObject(editorScope, id);
				}
%>
<form accept-charset="utf-8" style="background-color: white"
	method="post" action="policies.jsp">

		<table class="teagle" border="0">
<%	
			
				for (int i = 0; i < newObject.size(); i++)
				{
					EditorDataItem edi = newObject.get(i);
					if(edi.getHtmlElement() == ItemHtmlElementEnum.TEXT)
					{							
			%>
			<tr> 
				<td><label for="<%=edi.getName()%>" title="Name"><span class="tooltipspan" onMouseover="ddrivetip(<%=policyViewer.getTipsMessage(editorScope, edi.getName()) %>,'#ddddda', 300)" onMouseout="hideddrivetip()"><%=edi.getName()%>:</span></label></td>
				<td><input type="text" name="<%=edi.getName()%>" value="<%=edi.getValue() == null?"":edi.getValue()%>" /> </td>
			</tr>
	<%		
					}
					else if (edi.getHtmlElement().equals(ItemHtmlElementEnum.SELECTWITHEVENT))
					{
						EditorDataItem triggeredEdi = edi.getTrigeredEditorItem();
						
			%> 
			<tr>
				<td><label for="<%=edi.getName()%>" title="Name"><span class="tooltipspan" onMouseover="ddrivetip(<%=policyViewer.getTipsMessage(editorScope, edi.getName()) %>,'#ddddda', 300)" onMouseout="hideddrivetip()"><%=edi.getName()%>:</span></label></td>
				<td><select id=<%=edi.getName()%> name="<%=edi.getName()%>"	onchange="getAssociatedData(<%=triggeredEdi.getName()%>, this.value)">
	<% 
						List<String> list = (List<String>)edi.getValue();
								for (int k = 0; k < list.size(); k++)
								{
									String value = list.get(k);
							%>
									<option <%=value.equals(edi.getOldInput())?"selected":"" %>><%=list.get(k) %></option>

	<%											
								}
							%>
				</select>
			</td>
		</tr> 
		<%			
					}else if (edi.getHtmlElement() == ItemHtmlElementEnum.SELECT)
					{
			%> 			
			
				<td><label for="<%=edi.getName()%>" title="Name"><span class="tooltipspan" onMouseover="ddrivetip(<%=policyViewer.getTipsMessage(editorScope, edi.getName()) %>,'#ddddda', 300)" onMouseout="hideddrivetip()"><%=edi.getName()%>:</span></label></td>
				<td>
				<% 
						List<String> list = null;
						if(i ==0 && editorScope.equals(ResourcePoliciesEditor.EditorScope) && resourceName != null)
						{
							list = new ArrayList<String>();
							list.add(resourceName);
						}
						else 
							list = (List<String>)edi.getValue();
							
				%>
					<select id=<%=edi.getName()%> name="<%=edi.getName()%>">
	<% 
							for (int k = 0; k < list.size(); k++)
							{
								String value = list.get(k);
								String selected = "";
								if(i == 0 && editorScope.equals(ResourcePoliciesEditor.EditorScope) && resourceName != null && list.get(k).equals(resourceName))
								{
									selected = "selected";
								}
						%>
						<option <%=value.equals(edi.getOldInput())?"selected":"" %>><%=value %></option>
	<%	
							}
						%>
				</select>
			</td>
		</tr>
		<%									
					}else if (edi.getHtmlElement() == ItemHtmlElementEnum.TEXTAREA)
					{
			%> 
			
			<tr>
				<td valign="top"><span class="tooltipspan" onMouseover="ddrivetip(<%=policyViewer.getTipsMessage(editorScope, edi.getName()) %>,'#ddddda', 300)" onMouseout="hideddrivetip()"><%=edi.getName()%>:</span></td>				
  				<td> <div style= "border: 1px solid #CCCCCC; padding: 0px; margin-left: 1em; width: 100%">
							<textarea id="code" name='policy' cols="120" rows="60"><%= edi.getValue()== null?"":edi.getValue() %></textarea>
					</div> 

	<script type="text/javascript">
							  var editor = CodeMirror.fromTextArea('code', {
							    height: "450px",
							    width: "500px",
							    parserfile: "parsexml.js",
							    stylesheet: "../xmleditor/css/xmlcolors.css",
							    path: "../xmleditor/js/",
							    continuousScanning: 500,
							    lineNumbers: true,
							    textWrapping: false
							  });
						</script>
				</td>
			</tr>
		   <% 		
						
					}
				}

			%> 
				<!--<tr>
					<td><input type="button" value="View Schema" class="submit button" onclick="return popitup('popupWindow.jsp')" />
						<script type="text/javascript" charset="utf-8">
								function popitup(url) {
									newwindow=window.open(url,'name','height=600,width=700');
									if (window.focus) {newwindow.focus()}
									return false;
								}
								   		
							</script>					
					</td>
					<td></td>
				</tr> -->
			<tr>
					<td><input type="hidden" name="editorScope" value="<%=editorScope%>" /></td>
					
			<%
				if(action.equals("new"))
				{
			%>		
					<td><input type="hidden" name="action" value="setNew" /></td>
			<%
				}else{
			%>
					<td><input type="hidden" name="action" value="set" /></td>			
					<td><input type="hidden" name="id" value="<%=id %>"></td>
			<%
			
				}			
			%>
				</tr>
	
		<tr>
			<td><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='policies.jsp'" /></td>
			<td><input type="submit" value="Save" style="width:<%=buttonbreite%>pt"/></td>
		</tr>
	   </table>	
	</form>

<%			
			
		 }
		}else if(action.equals("setNew"))
		{
			String editorScope = request.getParameter("editorScope");
			if(editorScope != null)
			{
				policyViewer.saveNewData(editorScope, request.getParameterMap());
%>
	<p>Policy successfully created!</p>
	<p><input type="button" value="Back"
	style="width:<%=buttonbreite%>pt"
	onClick="window.location='policies.jsp'" /></p>
<% 				
			}
			
		}
	}catch(RepositoryException e)
	{
%>
<h3>Something went wrong!</h3>
<p><input type="button" value="Back"
	style="width:<%=buttonbreite%>pt" onClick="history.go(-1);" /></p>
<h3>Exception:</h3>

<code><%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(e.getMessage())%></code> 

<%	
	e.printStackTrace();
	}
%>
</div>
</div>
<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp"%>
