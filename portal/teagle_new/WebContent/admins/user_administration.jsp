<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*, de.fhg.fokus.teaglewebsite.*, teagle.vct.model.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="user_administration" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	if(request.isUserInRole("authAdmin")){
		int buttonbreite = 50;
		int update_count = 0;
		String action = request.getParameter("action");
		//JDBC-Stuff
		java.sql.Connection cn = null;
		Statement st = null;
		Statement st2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/teagle", "teagle", "*4teagle#");

//Startseite ohne action
//*********************************
// START
//*********************************
			if(action == null){
%>
		<h2>New Users</h2>
		<br>
<%
				st = cn.createStatement();
				rs = st.executeQuery("select user_name, user_fullname, email, organization_name from new_users where confirmed='0'");
				int rs_count = 0;
%>
		<table class="teagle" border="0">
			<colgroup>
				<col/>
				<col/>
				<col/>
				<col/>
				<col/>
				<col/>
				<col/>
			</colgroup>
			<thead>
    			<tr>
					<th bgcolor="#dddddd" style="height:25px">user_name</th>
					<th bgcolor="#dddddd">user_fullname</th>
					<th bgcolor="#dddddd">email</th>
					<th bgcolor="#dddddd">organization_name</th>
				</tr>
			</thead>
<%
				while (rs.next()){
					rs_count++;
%>
  			<tr>
  				<td bgcolor="#efefef"><strong><%= rs.getString("user_name") %></strong></td>
  				<td bgcolor="#efefef"><%= rs.getString("user_fullname") %></td>
  				<td bgcolor="#efefef"><%= rs.getString("email") %></td>
   				<td bgcolor="#efefef"><%= rs.getString("organization_name") %></td>
  				<td bgcolor="#efefef" align="center"><a href="?action=confirm&role=user&user_name=<%= rs.getString("user_name") %>">confirm as user</a></td>
  				<td bgcolor="#efefef" align="center"><a href="?action=confirm&role=partner&user_name=<%= rs.getString("user_name") %>">confirm as partner</a></td>
  				<td bgcolor="#efefef" align="center"><a href="?action=delete&user_name=<%= rs.getString("user_name") %>">delete</a></td>
			</tr>
<%
				}
%>
		</table>
<%
				if(rs_count == 0){
%>
		<p>No new users.</p>
<%
				}
%>
		<br>
		<br>
		<h2>Shortly Confirmed Users in temporary table</h2>
		<br>
<%
				st = cn.createStatement();
				rs = st.executeQuery("select user_name, user_fullname, email, organization_name from new_users where confirmed='1'");
				int rs_count2 = 0;
%>
		<table class="teagle" border="0">
		<colgroup>
				<col/>
				<col/>
				<col/>
				<col/>
				<col/>
			</colgroup>
			<thead>
    			<tr>
					<th bgcolor="#dddddd" style="height:25px">user_name</th>
					<th bgcolor="#dddddd">user_fullname</th>
					<th bgcolor="#dddddd">email</th>
					<th bgcolor="#dddddd">organization</th>
				</tr>
			</thead>
<%
				while (rs.next()){
					rs_count2++;
%>
  			<tr>
  				<td bgcolor="#efefef"><strong><%= rs.getString("user_name") %></strong></td>
  				<td bgcolor="#efefef"><%= rs.getString("user_fullname") %></td>
  				<td bgcolor="#efefef"><%= rs.getString("email") %></td>
  				<td bgcolor="#efefef"><%= rs.getString("organization_name") %></td>
  				<td bgcolor="#efefef" align="center"><a href="?action=delete&user_name=<%= rs.getString("user_name") %>">delete from temporary table</a></td>
			</tr>
<%
				}
%>
		</table>
<%
				if(rs_count2 == 0){
%>
		<p>No shortly confirmed users.</p>	
<%
				}
%>
		<br>
		<br>
		<h2>All Users</h2>
		<br>
<%
				st = cn.createStatement();
				rs = st.executeQuery("select user_email.user_name, user_email.email, user_info.user_fullname, user_info.organization_name from user_email left join user_info on user_email.user_name=user_info.user_name");
				int rs_count3 = 0;
%>
		<table class="teagle" border="0">
		<colgroup>
				<col/>
				<col/>
				<col/>
				<col/>
				<col/>
			</colgroup>
			<thead>
    			<tr>
					<th bgcolor="#dddddd" style="height:25px">user_name</th>
					<th bgcolor="#dddddd">user_fullname</th>
					<th bgcolor="#dddddd">email</th>
					<th bgcolor="#dddddd">organization</th>
					<th bgcolor="#dddddd">roles</th>
				</tr>
			</thead>
<%
				while (rs.next()){
					rs_count2++;
					
					st2 = cn.createStatement();
					rs2 = st2.executeQuery("select role_name from user_roles where user_name='" + rs.getString("user_name") + "'");
					String roleString = "";
					
					while (rs2.next()){
						roleString += ", " + rs2.getString("role_name");
					}
					roleString = roleString.substring(1);
					
%>
  			<tr>
  				<td bgcolor="#efefef"><strong><%= rs.getString("user_name") %></strong></td>
  				<td bgcolor="#efefef"><%= rs.getString("user_fullname") %></td>
  				<td bgcolor="#efefef"><%= rs.getString("email") %></td>
  				<td bgcolor="#efefef"><%= rs.getString("organization_name") %></td>
  				<td bgcolor="#efefef"><%=roleString %></td>
			</tr>
<%
				}
%>
		</table>
<%
				if(rs_count2 == 0){
%>
		<p>No confirmed users.</p>	
<%
				}
			}
//*********************************
// INSERT
//*********************************
			if("confirm".equals(action)){
				st = cn.createStatement();
				rs = st.executeQuery("select * from new_users where user_name='" + request.getParameter("user_name") + "'");
				rs.next();
				
				String user_name = rs.getString("user_name");
				String user_fullname = rs.getString("user_fullname");
				String user_pass = rs.getString("user_pass");
				String email = rs.getString("email");
				String organization = rs.getString("organization_name");
				String role = request.getParameter("role");
				
				if("user".equals(role)){
					st.execute("insert into user_roles values ('" + user_name + "', 'authUser')");	
				}
				else if("partner".equals(role)){
					st.execute("insert into user_roles values ('" + user_name + "', 'authPartner')");
					st.execute("insert into user_roles values ('" + user_name + "', 'authUser')");
				}
				st.execute("insert into users (user_name, user_pass) values ('" + user_name + "', '" + user_pass + "')");
				st.execute("insert into user_email values ('" + user_name +"', '" + email + "')");
				st.execute("insert into user_info values ('" + user_name +"', '" + user_fullname +"', '" + organization + "')");
				st.execute("update new_users set confirmed='1' where user_name='" + user_name + "'");
				
				update_count = st.getUpdateCount();
				
				//send email to user
				
				String[] recipients = {email};
				String subject = "Your account for fire-teagle.org was activated";
				String content = "You can now use your account with the username " + user_name + ".";
				String from = "teagle@panlab.net";
				Mail mail = new Mail();
				mail.sendMail(from, recipients, subject, content);
				
				//notify the repository here!
				
				Person person = ModelManager.getInstance().createPerson();
				person.setUserName(user_name);
				person.setFullName(user_fullname);
				person.setPassword(user_pass);
				
				person = ModelManager.getInstance().persist(person);
				
				Email email1 = ModelManager.getInstance().createEmail();
				email1.setAddress(email);
								
				person.addEmail(email1);
								
				Organisation organisation = ModelManager.getInstance().getOrganisation(organization);
				organisation.addPerson(person);
				person.addOrganisation(organisation);
				
				if("user".equals(role)){
					person.addRole(ModelManager.getInstance().getCustomerRole());	
				}
				else if("partner".equals(role)){
					person.addRole(ModelManager.getInstance().getPartnerRole());
				}
								
				person = ModelManager.getInstance().persist(person);
				organisation = ModelManager.getInstance().persist(organisation);
%>
		<p><strong><%=update_count %> user </strong> successfully confirmed, email sent</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='user_administration.jsp'"/></p>	
<%
				
	        }
//*********************************
// DELETE
//*********************************
			if("delete".equals(action)){
				if("yes".equals(request.getParameter("sure"))){
					st = cn.createStatement();
					st.execute("delete from new_users where user_name='" + request.getParameter("user_name") + "'");
					//update_count = st.getUpdateCount();
%>
		<p>entry successfully deleted</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='user_administration.jsp'"/></p>
<%					
				}
				else {
%>
		<h3>Are You sure?</h3>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='user_administration.jsp'"/><input type="button" value="Delete" style="width:<%=buttonbreite%>pt" onClick="window.location='?action=delete&user_name=<%=request.getParameter("user_name") %>&sure=yes'"/></p>
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
		finally {
	 		try { if (null != rs) rs.close(); } catch (Exception ex) {}
	 		try { if (null != st) st.close(); } catch (Exception ex) {}
	 		try { if (null != cn) cn.close(); } catch (Exception ex) {}
	 	}
	}
	else{
%>
	Nothing to see here. Go <a href="http://www.fire-teagle.org">home</a>!
<%
	}


%>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>
