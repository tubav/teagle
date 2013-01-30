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
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	List<? extends Organisation> userOrganisationList;
	List<Ptm> ptmList;
	
	try {
		userOrganisationList = ModelManager.getInstance().findOrganisationsByUserName(user);

//*********************************
//START - only display
//*********************************
		if(action == null){
%>
		<h2>my PTMs</h2>
<%				
			if(userOrganisationList.size() == 0){
%>
		<p>no Organization bound to your User.</p>
<%	
			}
			else{
				for(Organisation o : userOrganisationList){
					ptmList = ModelManager.getInstance().listPtmsByOrganisation(o.getName());
%>
		<h3>PTMs from <%= o.getName()%></h3>
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
	    		<tr>
					<th bgcolor="#dddddd" style="height:25px">name</th>
					<th bgcolor="#dddddd">userName</th>
					<th bgcolor="#dddddd">organization</th>
					<th bgcolor="#dddddd">url</th>
				</tr>
			</thead>
<%
					for (Ptm ptm: ptmList) {
%>
			<tr>
				<td bgcolor="#efefef"><%= ptm.getCommonName()%></td>
	  			<td bgcolor="#efefef"><%= ptm.getPerson().getUserName()%></td>
	  			<td bgcolor="#efefef"><strong><%= o.getName()%></strong></td>
	  			<td bgcolor="#efefef"><%= ptm.getUrl()%></td>
	  			<td bgcolor="#efefef"><a href=?action=edit&id=<%= ptm.getCommonName()%>>edit</a></td>
	  			<td bgcolor="#efefef"><a href=?action=delete&id=<%= ptm.getCommonName()%>>delete</a></td>
	  		</tr>
<%	
					}
				}
			}
%>
		</table>
		</div>
		<br>
		<a href="?action=create">Create</a> a new PTM.
<%
		}
//*********************************
// CREATE
//*********************************
		if("create".equals(action)){
%>
		<h3>Create a new PTM</h3>
		<form action="my_ptms.jsp" method="post">
		<table class="teagle" border="0">
			<colgroup>
				<col width="100"/>
				<col width="200"/>
			</colgroup>
			<tr>
				<td>User</td>
				<td><input name="user_name" value="<%=user %>" size="30" readonly style="background-color:#dddddd"/></td>
			</tr>
			<tr>
				<td>id</td>
				<td><input name="id" type="text" size="30" maxlength="50" /></td>
			</tr>
			<tr>
				<td>organization</td>
				<td>
					<select name="organization_id">
<%
			for (Organisation o : userOrganisationList){
%>
						<option value="<%=o.getName() %>"><%=o.getName() %></option>
<%
			}
%>
					</select>
				</td>
			</tr>
			<tr>
				<td>url</td>
				<td><input name="url" type="text" size="30" maxlength="50" /></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="hidden" name="action" value ="insert"/></td>
			</tr>
		</table>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptms.jsp'"/><input type="submit" value="Create" style="width:<%=buttonbreite%>pt"/></p>
		</form>
<%
		}

//*********************************
// EDIT
//*********************************
		if("edit".equals(action)){
			String newid = request.getParameter("newid");
			Ptm ptm_edit = ModelManager.getInstance().getPtm(request.getParameter("id"));
			//if(user.equals(ptm_edit.userName)){
%>
		<h3>Edit the ptm with the id: <%=ptm_edit.getCommonName() %></h3>
		<form action="my_ptms.jsp" method="post">
			<table class="teagle" border="0">
				<tr>
					<td>User</td>
					<td><input name="user_name" value="<%=user %>" size="30" readonly style="background-color:#dddddd"/></td>
				</tr>
<%
				if("true".equals(newid)){
%>
				<tr>
					<td>old name</td>
					<td><input name="id" type="text" value="<%=ptm_edit.getCommonName() %>" size="30" maxlength="50" readonly style="background-color:#dddddd"/></td>
					<td><a href=?action=edit&newid=true&id=<%=ptm_edit.getCommonName() %>>change name</a></td>
				</tr>
				<tr>
					<td>new name</td>
					<td><input name="new_id" type="text" size="30" maxlength="50"/></td>
				</tr>
<%
				}
				else{
%>
				<tr>
					<td>name</td>
					<td><input name="id" type="text" value="<%=ptm_edit.getCommonName() %>" size="30" maxlength="50" readonly style="background-color:#dddddd"/></td>
					<td><a href=?action=edit&newid=true&id=<%= ptm_edit.getCommonName()%>>change name</a></td>
				</tr>
<%
				}
%>
				<tr>
					<td>organization</td>
					<td>
						<select name="organization_id">
<%
			for (Organisation o : userOrganisationList) {
				if(o.getName().equals(ptm_edit.getOrganisation().getName())){ 
%>
							<option selected value="<%=o.getName() %>"><%=o.getName() %></option>
<%
				}
				else{ 
%>
							<option value="<%=o.getName() %>"><%=o.getName() %></option>
<%
				}
			//}
%>
						</select>
					</td>
				</tr>
				<tr>
					<td>url</td>
					<td><input name="url" value="<%=ptm_edit.getUrl() %>" type="text" size="30" maxlength="50" /></td>
				</tr>
				<tr>
					<td></td>
					<td>&nbsp;&nbsp;&nbsp;<a href=my_ptm_associations.jsp?ptmid=<%= ptm_edit.getCommonName()%>>edit supported resource types</a></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="hidden" name="action" value ="update"/></td>
				</tr>
<%
				if("true".equals(newid)){
%>
				<tr>
					<td></td>
					<td><input type="hidden" name="newid" value ="true"/></td>
				</tr>
<%
				}
%>
			</table>
			<br>
			<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptms.jsp'"/><input type="submit" value="Edit" style="width:<%=buttonbreite%>pt"/></p>
		</form>
<%
			}
			//else{
%>
		
<%
			//}
		}
//*********************************
// DELETE
//*********************************
		if("delete".equals(action)){
			if("yes".equals(request.getParameter("sure"))){
				
				Ptm ptm_delete = ModelManager.getInstance().getPtm(request.getParameter("id"));
				boolean allowed = false;
				for(Organisation o : userOrganisationList){
					if(o.getName().equals(ptm_delete.getOrganisation().getName())){
						allowed = true;
					}
				}
				if(allowed){
					ModelManager.getInstance().delete(ptm_delete);
%>
		<p>successfully deleted</p>
<%
				}
				else{
%>
		<p>You are not allowed to do this!</p>
<%
				}
%>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptms.jsp'"/></p>
<%					
			}
			else {
%>
		<h3>Are You sure?</h3>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptms.jsp'"/><input type="button" value="Delete" style="width:<%=buttonbreite%>pt" onClick="window.location='?action=delete&id=<%=request.getParameter("id") %>&sure=yes'"/></p>
<%
			}
		}
//*********************************
// INSERT
//*********************************
		if("insert".equals(action)){
			if("".equals(request.getParameter("ptm_name"))){
				
%>
		<h3>PTM Name must not be empty!</h3>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
<%
			}
			else {
				Ptm p = ModelManager.getInstance().createPtm();
				p.setCommonName(request.getParameter("id"));
				p.setPerson(ModelManager.getInstance().findPersonByUserName(request.getParameter("user_name")));
				p.setOrganisation(ModelManager.getInstance().getOrganisation(request.getParameter("organization_id")));
				p.setUrl(request.getParameter("url"));
				p.getInfo().setCommonName(request.getParameter("id") + "_info");
				p = ModelManager.getInstance().persist(p);
%>
		<p>entry successfully inserted</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptms.jsp'"/></p>	
<%
			}
        }
//*********************************
// UPDATE
//*********************************
		if("update".equals(action)){
			String newid = request.getParameter("newid");
			if("".equals(request.getParameter("ptm_name"))){
%>
		<h3>ptm_name must not be empty!</h3>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
<%
			}
			else {
				if("true".equals(newid)){
					Ptm ptm_update = ModelManager.getInstance().getPtm(request.getParameter("id"));
					//Ptm ptm_update = ModelManager.getInstance().createPtm();
					ptm_update.setCommonName(request.getParameter("new_id"));
					ptm_update.setPerson(ModelManager.getInstance().findPersonByUserName(request.getParameter("user_name")));
					ptm_update.setOrganisation(ModelManager.getInstance().getOrganisation(request.getParameter("organization_id")));
					ptm_update.setUrl(request.getParameter("url"));
					
					//ModelManager.getInstance().delete(ModelManager.getInstance().getPtm(request.getParameter("id")));
					
					ptm_update = ModelManager.getInstance().persist(ptm_update);
%>
		<p>successfully updated</p>
<%
				}
				else{				
					Ptm ptm_update = ModelManager.getInstance().getPtm(request.getParameter("id"));
					ptm_update.setOrganisation(ModelManager.getInstance().getOrganisation(request.getParameter("organization_id")));
					ptm_update.setUrl(request.getParameter("url"));
					
					ptm_update = ModelManager.getInstance().persist(ptm_update);
%>
		<p>successfully updated</p>
<%
				}
%>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='my_ptms.jsp'"/></p>
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
