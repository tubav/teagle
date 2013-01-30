<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, teagle.vct.model.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_my_resources" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	String action = request.getParameter("action");
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	List<? extends Organisation> userOrganisationList;
	List<? extends ResourceSpec> resourceSpecList;
	
	try {
		userOrganisationList = ModelManager.getInstance().findOrganisationsByUserName(user);
		
//*********************************
//START - only display
//*********************************
		if(action == null){

			if("admin".equals(user)){
%>
		<h2>my Resources (admin view)</h2>
<%
				resourceSpecList = ModelManager.getInstance().listResourceSpecs();
%>	
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
				<tr>
					<th bgcolor="#dddddd" style="height:25px">provider</th>
					<th bgcolor="#dddddd">type</th>
					<th bgcolor="#dddddd">description</th>
					<th bgcolor="#dddddd">price</th>
				</tr>
			</thead>
<%
				for (ResourceSpec r: resourceSpecList) {
%>
			<tr>
				<td bgcolor="#efefef"><strong><%=r.getProvider()%></strong></td>
				<td bgcolor="#efefef"><%=r.getType()%></td>
				<td bgcolor="#efefef"><%=r.getDescription()%></td>
				<td bgcolor="#efefef"><%=r.getPrice()%></td>
				<td bgcolor="#efefef"><a href="?action=edit&name=<%=r.getCommonName()%>">edit</a></td>
				<td bgcolor="#efefef"><a href="?action=delete&name=<%=r.getCommonName()%>">delete</a></td>	 
			</tr>
<%
			
				}
%>
		</table>
		</div>
<%	
			}
			else{//normal user
%>
		<h2>my Resources</h2>
<%				
				if(userOrganisationList.size() == 0){
%>
				<p>no Organization bound to your User.</p>
<%	
				}
				else{
					for(Organisation o :userOrganisationList){
						List<? extends ResourceSpec> resourceList = o.getResourceSpecs();
%>
		<h3>resources from <%= o.getName()%></h3>
<%
				
%>	
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
	    		<tr>
					<th bgcolor="#dddddd" style="height:25px">provider</th>
					<th bgcolor="#dddddd">type</th>
					<th bgcolor="#dddddd">description</th>
					<th bgcolor="#dddddd">price</th>
				</tr>
			</thead>
<%						
						for (ResourceSpec r: resourceList) {
%>
			<tr>
	  			<td bgcolor="#efefef"><strong><%=r.getProvider()%></strong></td>
	  			<td bgcolor="#efefef"><%=r.getType()%></td>
	  			<td bgcolor="#efefef"><%=r.getDescription()%></td>

	  			<td bgcolor="#efefef"><%=r.getPrice()%></td>
				<td bgcolor="#efefef"><a href="?action=edit&name=<%=r.getCommonName()%>">edit</a></td>
	  			<td bgcolor="#efefef"><a href="?action=delete&name=<%=r.getCommonName()%>">delete</a></td>	 
	  		</tr>
<%
						}
					}
				}
%>
		</table>
		</div>
<%
			}
%>
		
		<br>
		<a href="resource_registration.jsp">Register</a> a new Resource.
<%
		}
//*********************************
// EDIT
//*********************************
		if("edit".equals(action)){
			String exception = "";
			String resourcename = request.getParameter("name");
			ResourceSpec resource = ModelManager.getInstance().getResourceSpec(resourcename);
			boolean isUsed;
			try {
				//checking if editing is allowed (i.e. resource is used by any vct)
				isUsed = resource.isUsed();
				if(isUsed == true){
%>
		<h4>The resource <%=resourcename %> is currently used and can't be edited.</h4>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_resources.jsp'"/></p>
<%
				}
				else{
%>
		<h4>Editing: <%=resourcename %></h4>
		<p>isUsed = <%=isUsed %></p>
		<form action="my_resources.jsp" method="post">
			<table class="teagle" border="0">
				<tr>
					<td>provider</td>
					<td><input name="provider" type="text" size="50" maxlength="50" value="<%=resource.getProvider() %>" readonly style="background-color:#dddddd"/></td>
				</tr>
				<tr>
					<td>name</td>
					<td><input name="name" type="text" size="50" maxlength="50" value="<%=resource.getType() %>" readonly style="background-color:#dddddd"/></td>
				</tr>
				<tr>
					<td>price</td>
					<td><input name="price" type="text" size="10" maxlength="10" value="<%=resource.getPrice() %>" /></td>
				</tr>
				<tr>
					<td>description</td>
					<td><textarea name="description" cols="50" rows="4"><%=resource.getDescription() %></textarea></td>
				</tr>
				<tr>
					<td>url</td>
					<td><input name="url" type=text" size="50" value="<%=resource.getUrl() %>"/>
				</tr>
				<tr>
					<td>configuration</td>
					<td>&nbsp;&nbsp;&nbsp;<a href="resource_registration.jsp?action=edit_config&name=<%=resourcename%>">edit here</a></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="hidden" name="action" value ="set"/></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_resources.jsp'"/><input type="submit" value="Edit" style="width:<%=buttonbreite%>pt"/></td>
				</tr>
			</table>
		</form>
<%
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
		}
//*********************************
// SET
//*********************************
		if("set".equals(action)){
			String resourcename = request.getParameter("name");
			ResourceSpec resource;
			boolean isUsed;
			try {
				resource = ModelManager.getInstance().getResourceSpec(resourcename);
				//checking again if editing is allowed (i.e. resource is used by any vct)
				isUsed = resource.isUsed();
				if(isUsed == true){
%>
		<h4>The resource <%=resourcename %> is currently used and can't be edited.</h4>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_resources.jsp'"/></p>
<%
				}
				else{
					boolean allowed = false;
					for(Organisation o : userOrganisationList){
						if(o.getName().equals(resource.getProvider())){
							allowed = true;
						}
					}
					if(allowed){
						resource.getCost().setAmount(Double.parseDouble(request.getParameter("price")));
						resource.getCost().setCurrency("Euro");//quickhack
						resource.setDescription(request.getParameter("description"));
						resource.setUrl(request.getParameter("url"));
						resource = ModelManager.getInstance().persist(resource);
%>
		<p>Resource <%=resourcename %> successfully edited.</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_resources.jsp'"/></p>
<%
					}
					else{
%>
		<p>You are not allowed to do this!</p>
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
		}
//*********************************
// DELETE
//*********************************
		if("delete".equals(action)){
			String resourcename = request.getParameter("name");
			ResourceSpec resource = ModelManager.getInstance().getResourceSpec(resourcename);

			if("yes".equals(request.getParameter("sure"))){
				boolean allowed = "admin".equals(user);
				for(Organisation o : userOrganisationList){
					if(o.getName().equals(resource.getProvider())){
						allowed = true;
					}
				}
				if(allowed){
					if(resource.isUsed()){
%>
		<p>Resource <%=resourcename %> is used and can't be deleted.</p>
<%						
					}
					else{
						ModelManager.getInstance().delete(resource);
%>
		<p> <%=resourcename %></p>
		<p>successfully deleted</p>
<%
					}
				}
				else{
%>
		<p>You are not allowed to do this!</p>
<%
				}
%>
				<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_resources.jsp'"/></p>
<%			
			}
			else{
%>
				<h3>Are You sure?</h3>
				<p>
					<input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_resources.jsp'"/>
					<input type="button" value="Delete" style="width:<%=buttonbreite%>pt" onClick="window.location='?action=delete&name=<%=request.getParameter("name") %>&sure=yes'"/>
				</p>
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
