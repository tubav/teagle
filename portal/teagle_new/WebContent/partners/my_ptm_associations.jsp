<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, teagle.vct.model.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_my_ptms" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	String action = request.getParameter("action");
	String ptmid = request.getParameter("ptmid");
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	List<? extends Organisation> userOrganisationList;
	
	try {
		userOrganisationList = ModelManager.getInstance().findOrganisationsByUserName(user);

//*********************************
//START - only display
//*********************************
		if(action == null){
			if(ptmid == null){
%>
		<p>parameter id not found</p>
<%			
			}
			else{
%>
		<h3>Resource type associations for PTM with the id: <%=ptmid%></h3>
<%				
				Ptm ptm = ModelManager.getInstance().getPtm(ptmid);
				List<? extends ResourceSpec> ptmResources = ptm.getInfo().getResourceSpecs();				
				if(ptmResources.size()>0){
%>
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
				<tr>
					<th bgcolor="#dddddd" style="height:25px">type</th>
					<th bgcolor="#dddddd">provider</th>
					<th bgcolor="#dddddd">description</th>
					<th bgcolor="#dddddd">price</th>
				</tr>
			</thead>
<%
					for (ResourceSpec r: ptmResources) {
						//int availability = RM.getAvailability(r, ptm);
%>
			<tr>
				<td bgcolor="#efefef"><%=r.getType()%></td>
				<td bgcolor="#efefef"><strong><%=r.getProvider()%></strong></td>
				<td bgcolor="#efefef"><%=r.getDescription()%></td>
				<td bgcolor="#efefef"><%=r.getPrice()%></td>
				<td bgcolor="#efefef"><a href="?action=delete&ptmid=<%=ptmid%>&resname=<%=r.getCommonName()%>">delete</a></td>	 
			</tr>
<%
					}
%>
		</table>
		</div>
<%
				}
				else{
%>
		<p>There are no resource types associated with this PTM.</p>
<%
				}
%>
		<br>
		<a href="?action=add&ptmid=<%=ptmid%>">Add</a> a new resource type association.
<%
			}
		}
//*********************************
// ADD
//*********************************
		if("add".equals(action)){
			Ptm ptm = ModelManager.getInstance().getPtm(ptmid);
			List<ResourceSpec> notSupportedResources = ModelManager.getInstance().getResourcesNotSupportedByPtm(ptm);
%>
		<h3>Add new resource type association for PTM with the id: <%=ptmid%></h3>
<%
			if(notSupportedResources.size()>0){
%>
		<p>Please select the resource type and the number of available instances:</p>
		<form action="my_ptm_associations.jsp" method="post">
			<table class="teagle" border="0">
				<tr>
					<td>
						<select name="resname">
<%
				for (ResourceSpec r: notSupportedResources) {
%>
							<option value="<%=r.getCommonName() %>"><%=r.getCommonName() %></option>
<%
					}
%>
						</select>
					</td>
				</tr>
				<tr>
					<td>
						<input type="hidden" name="action" value ="insert"/>
					</td>
				</tr>
				<tr>
					<td>
						<input type="hidden" name="ptmid" value ="<%=ptmid%>"/>
					</td>
				</tr>
			</table>
			<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptm_associations.jsp?ptmid=<%=ptmid%>'"/><input type="submit" value="Add" style="width:<%=buttonbreite%>pt"/></p>
		</form>
<%		
			}
			else{
%>
		<p>There are no resource types to associate with.</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptm_associations.jsp?ptmid=<%=ptmid%>'"/></p>
<%
			}
		}
//*********************************
// DELETE
//*********************************
		if("delete".equals(action)){
			String resNameToDelete = request.getParameter("resname");
			if("yes".equals(request.getParameter("sure"))){
				Ptm ptm = ModelManager.getInstance().getPtm(ptmid);
				boolean allowed = false;
				for(Organisation o : userOrganisationList){
					if(o.getName().equals(ptm.getOrganisation().getName())){
						allowed = true;
					}
				}
				if(allowed){
					ptm.getInfo().removeResourceSpec(ModelManager.getInstance().getResourceSpec(resNameToDelete));
%>
		<p>resource type association successfully removed</p>
<%
				}
				else{
%>
		<p>You are not allowed to do this!</p>
<%
				}
%>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptm_associations.jsp?ptmid=<%=ptmid%>'"/></p>
<%					
			}
			else {
%>
		<h3>Are You sure?</h3>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptm_associations.jsp?ptmid=<%=ptmid%>'"/><input type="button" value="Delete" style="width:<%=buttonbreite%>pt" onClick="window.location='?action=delete&ptmid=<%=ptmid%>&resname=<%=resNameToDelete%>&sure=yes'"/></p>
<%
			}
		}
//*********************************
// INSERT
//*********************************
		if("insert".equals(action)){
			String resname = request.getParameter("resname");
			ResourceSpec resource = ModelManager.getInstance().getResourceSpec(resname);
			Ptm ptm = ModelManager.getInstance().getPtm(ptmid);
			boolean allowed = false;
			for(Organisation o : userOrganisationList){
				if(o.getName().equals(ptm.getOrganisation().getName())){
					allowed = true;
				}
			}
			if(allowed){
				ptm.getInfo().addResourceSpec(resource);
%>
		<p>resource type association successfully added</p>
<%
			}
			else{
%>
		<p>You are not allowed to do this!</p>
<%
			}
%>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptm_associations.jsp?ptmid=<%=ptmid%>'"/></p>
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
%>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>
