<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
	<jsp:param name="login" value="true"/>
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="members" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
		
			<!-- include the boxes in the center -->
			<center>
				<div style="width:25em; margin-top:6em">
					<h2>Login</h2>
					<div>
						<form action='<%= response.encodeURL("j_security_check") %>' method="POST" name="loginform">
			 				<table class="teagle">
				 				<tr>
				 					<td>Username:</td><td><input type="text" name="j_username"></td>
				 				</tr>
				 				<tr>
				 					<td>Password:</td><td><input type="password" name="j_password"></td>
				 				</tr>
				 				<tr>
				 					<td></td><td><input type="submit" value="login">&nbsp;&nbsp;&nbsp;&nbsp;<a href="../forgot_password.jsp">forgot password?</a> </td>
				 				</tr>
			 				</table>
		 				</form>
					</div>
				</div>
			</center>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>