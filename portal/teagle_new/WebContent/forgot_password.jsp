<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*, teagle.vct.model.*, de.fhg.fokus.teaglewebsite.*, org.apache.commons.codec.digest.DigestUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="" />
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
	Statement st = null;
	ResultSet rs = null;
	ResultSet rs2 = null;
	try{
		Class.forName("com.mysql.jdbc.Driver");
	    cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/teagle", "teagle", "*4teagle#");

	    
//*********************************
// NO ACTION -> ENTER CREDITS
//*********************************
		if(action == null){
%>
			<h1>Password recovery</h1>
			<p>Please enter your username and you will receive an email on your address with which you have created your account. Please follow the link in this email.</p>
			<form action="forgot_password.jsp" method="post">
				<table class="teagle" border="0">
					<tr>
						<td>username</td>
						<td><input name="user_name" type="text" size="30" maxlength="50" <%if(request.getParameter("user_name") != null){%>value="<%=request.getParameter("user_name")%>"<%}%>></td>
					</tr>
					<tr>
						<td></td>
						<td><input type="hidden" name="action" value ="send"></td>
					</tr>
					<tr>
						<td></td>
						<td><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='members/secret.jsp'"><input type="submit" value="Send" style="width:<%=buttonbreite%>pt"></td>
					</tr>
				</table>
			</form>
<%
		}
//*********************************
// SEND MAIL
//*********************************
		if("send".equals(action)){
			String user_name = request.getParameter("user_name");
			st = cn.createStatement();
			
			rs = st.executeQuery("select email from user_email where user_name ='" + user_name + "'");
			rs.next();
			String email = rs.getString("email");
			
			Token token = new Token();
			String token_string = token.getToken();
			
			st = cn.createStatement();
			st.execute("insert into forgot_password (user_name, token) values ('" + user_name + "', '" + token_string + "');");
						
			String[] recipients = {email};
			String subject = "[TEAGLE_WEBSITE] reset password?"; 
			String content = "There was a request to reset the password of your teagle account with the user name \"" + user_name + "\".\n\nIf you want to reset your password follow the link, otherwise ignore this email.\n\nhttp://www.fire-teagle.org/forgot_password.jsp?action=reset&user_name=" + user_name + "&token=" + token_string + "\n(This link is valid for 24 hours)";
			String from = "teagle@panlab.net";
			Mail mail = new Mail();
			mail.sendMail(from, recipients, subject, content);
%>
			<p>
				<br>Email sent. Please check your mailbox!
			</p>
			<p><input type="button" value="back" style="width:<%=buttonbreite%>pt" onClick="window.location='.'"></p>
<%
		}
//*********************************
// RESET PASSWORD
//*********************************
		if("reset".equals(action)){
			String user_name = request.getParameter("user_name");
			String token = request.getParameter("token");
			
			st = cn.createStatement();
			rs = st.executeQuery("select * from forgot_password where user_name ='" + user_name + "' and UNIX_TIMESTAMP(now())-UNIX_TIMESTAMP(timestamp)<86400 order by timestamp desc limit 1");
			if(rs.next()){
				if(token.equals(rs.getString("token"))){
					//reset password
					int rand = (int)(100 + (900 * Math.random()));
					st.execute("update users set user_pass = MD5('teagle" + rand + "') where user_name='" + user_name + "'");
					//im Repo auch ändern!! 
					
					Person person = ModelManager.getInstance().findPersonByUserName(user_name);
					person.setPassword(DigestUtils.md5Hex("teagle" + rand));
					person = ModelManager.getInstance().persist(person);
					
					//fetch email address
					rs2 = st.executeQuery("select email from user_email where user_name ='" + user_name + "'");
					rs2.next();
					String email = rs2.getString("email");
					//send mail
					String[] recipients = {email};
					String subject = "[TEAGLE_WEBSITE] new password"; 
					String content = "Your password for the teagle portal was set to\n\nteagle" + rand + "\n\nPlease change it the next time you log in!";
					String from = "teagle@panlab.net";
					Mail mail = new Mail();
					mail.sendMail(from, recipients, subject, content);
					//delete entry from forgot_password
					st.execute("delete from forgot_password where user_name = '" + user_name + "'");
					int update_count = st.getUpdateCount();
%>
			<p>
				<br>Your password has been reset. You will receive an email with the new one. Please change it on the next login.
			</p>
			<p><input type="button" value="back" style="width:<%=buttonbreite%>pt" onClick="window.location='.'"></p>
<%
				}
				else{//token wrong
%>
			<p>
				<br>Token to old or already used!
			</p>
			<p><input type="button" value="back" style="width:<%=buttonbreite%>pt" onClick="window.location='.'"></p>
<%			
				}
			}
			else{//no entry in db
%>
			<p>
				<br>Token to old or already used! 
			</p>
			<p><input type="button" value="back" style="width:<%=buttonbreite%>pt" onClick="window.location='.'"></p>
<%
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
			<h2>Something went wrong!</h2>
			<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"></p>
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


		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>