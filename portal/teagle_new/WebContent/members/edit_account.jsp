<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*, teagle.vct.model.*, sun.security.provider.*, de.fhg.fokus.teaglewebsite.*, org.apache.commons.codec.digest.DigestUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="edit_account" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	int buttonbreite = 50;
	String action = request.getParameter("action");
	//JDBC-Stuff
	java.sql.Connection cn = null;
	Statement  st = null;
	ResultSet  rs = null;
	try{
		Class.forName("com.mysql.jdbc.Driver");
	    cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/teagle", "teagle", "*4teagle#");

	    
//*********************************
// NO ACTION -> EDIT
//*********************************
		if(action == null){
			st = cn.createStatement();
			rs = st.executeQuery("select users.*, user_email.email from users left join user_email on users.user_name=user_email.user_name where users.user_name='" + request.getUserPrincipal().getName() + "'");
	
			ResultSetMetaData rsmd = rs.getMetaData();
			int n = rsmd.getColumnCount();

			rs.next();
%>
		<h2>Edit account</h2>
		
		<form action="edit_account.jsp" method="post">
		<table class="teagle" border="0">
			<colgroup>
				<col width="150">
				<col width="200">
			</colgroup>
			<tr>
				<td>username</td>
				<td><input name="user_name" type="text" size="30" maxlength="50" value="<%=rs.getString(1) %>" readonly style="background-color:#dddddd"></td>
			</tr>
			<tr>
				<td>email</td>
				<td><input name="email" type="text" size="30" maxlength="50" value="<%=rs.getString(3) %>"></td>
			</tr>
			<tr>
				<td>new password</td>
				<td><input name="user_pass" type="password" size="30" maxlength="50"></td>
			</tr>
			<tr>
				<td>confirm new password</td>
				<td><input name="user_pass2" type="password" size="30" maxlength="50"></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="hidden" name="action" value ="update"></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='secret.jsp'"><input type="submit" value="Edit" style="width:<%=buttonbreite%>pt"></td>
			</tr>
		</table>
		</form>
<%
        }
//*********************************
// UPDATE
//*********************************
		if("update".equals(action)){
			String user_name = request.getParameter("user_name");
			String email = request.getParameter("email");
			String user_pass = request.getParameter("user_pass");
			String user_pass2 = request.getParameter("user_pass2");
			
			if("".equals(user_pass) || !user_pass.equals(user_pass2)){
%>
		<p style="color:red">
			<b>password</b> fields must not be different or empty!
		</p>
		<p><input type="button" value="Try again" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_account.jsp'"></p>
<%
			}
			else{
				st = cn.createStatement();
				st.execute("update users set user_pass = MD5('" + user_pass + "') where user_name='" + user_name + "'");
				if(!"".equals(email)){
					st.execute("update user_email set email='" + email + "' where user_name='" + user_name + "'");
				}
				//inform repo about changes:
				Person person = ModelManager.getInstance().findPersonByUserName(user_name);
				person.setPassword(DigestUtils.md5Hex(user_pass));
				person = ModelManager.getInstance().persist(person);
%>
		<p>account successfully edited</p>
		<p>Go back <a href="..">home</a>.
<%				
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
		<h2>Something went wrong!</h2>
		<p><input type="button" value="Back" onClick="history.go(-1);"></p>
		<h3>Exception:</h3>
		<code><%= e.toString() %></code>
<%		
	}
	finally {
		try { if( null != rs ) rs.close(); } catch( Exception ex ) {}
		try { if( null != st ) st.close(); } catch( Exception ex ) {}
		try { if( null != cn ) cn.close(); } catch( Exception ex ) {}
	}
%>
		<!-- include the boxes in the center -->

		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>