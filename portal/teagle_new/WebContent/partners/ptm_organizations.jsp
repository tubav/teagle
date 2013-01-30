<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, teagle.vct.model.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_ptm_organizations" />
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
	List<? extends Organisation> organisationList;
	
	try {
		organisationList = ModelManager.getInstance().listOrganisations();

//*********************************
//START - only display organizations
//*********************************
		if(action == null){
%>
		<h2>Registered Organizations</h2>

		<table class="teagle" border="0">
			<colgroup>
				<col width="400"/>
				<col width="100"/>
			</colgroup>
			<thead>
	    		<tr>
					<th bgcolor="#dddddd" style="height:25px">name</th>
				</tr>
			</thead>
<%
			for (Organisation o: organisationList) {
%>
			<tr>
	  			<td bgcolor="#efefef"><%= o.getName()%></td>

<%
				if(request.isUserInRole("authAdmin")){
%>
	  			<td bgcolor="#efefef" align="center"><a href="?action=delete&name=<%= o.getName() %>">delete</a></td>
<%
				}
%>
	  		</tr>
<%
			}
%>
		</table>
		<br>
		<a href="?action=create">Create</a> a new Organization.
		
<%
		}
//*********************************
//CREATE
//*********************************
		if("create".equals(action)){
%>
		<h3>Create a new Organization</h3>
		<form action="ptm_organizations.jsp" method="post">
		<table class="teagle" border="0">
			<colgroup>
				<col width="200"/>
				<col width="200"/>
			</colgroup>
			<tr>
				<td>User</td>
				<td><input name="user_name" value="<%=user %>" size="30" readonly style="background-color:#dddddd"/></td>
			</tr>
			<tr>
				<td>Organization name</td>
				<td><input name="organization_name" type="text" size="30" maxlength="100" /></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="hidden" name="action" value ="insert"/></td>
			</tr>
		</table>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='ptm_organizations.jsp'"/><input type="submit" value="Create" style="width:<%=buttonbreite%>pt"/></p>
		</form>
<%
		}
//*********************************
// DELETE
//*********************************
		if("delete".equals(action)){
			if("yes".equals(request.getParameter("sure"))){
				//ModelManager.getInstance().removeOrganization(request.getParameter("name"));
%>
		<p>successfully deleted</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='ptm_organizations.jsp'"/></p>
<%					
			}
			else {
%>
		<h3>Are You sure?</h3>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='ptm_organizations.jsp'"/><input type="button" value="Delete" style="width:<%=buttonbreite%>pt" onClick="window.location='?action=delete&id=<%=request.getParameter("id") %>&sure=yes'"/></p>
<%
			}
		}
//*********************************
// INSERT
//*********************************
		if("insert".equals(action)){
			if("".equals(request.getParameter("organization_name"))){
				
%>
		<h3>Organization Name must not be empty!</h3>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
<%
			}
			else {
				Organisation org = ModelManager.getInstance().createOrganisation();
				org.setName(request.getParameter("name"));
%>
		<p>Organization successfully created</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='ptm_organizations.jsp'"/></p>	
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
