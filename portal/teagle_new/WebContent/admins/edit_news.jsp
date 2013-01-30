<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="edit_news" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	String user = request.getUserPrincipal().getName();
	if(!"admin".equals(user)){
%>
		Nothing to see here. Go <a href="/teagle/">home</a>!
<%
	}
	else{
		int buttonbreite = 50;
		int update_count = 0;
		String action = request.getParameter("action");
		//JDBC-Stuff
		Connection cn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/teagle", "teagle", "*4teagle#");

//Startseite ohne action
//*********************************
// START
//*********************************
			if(action == null){
%>
		<h2>News (admin view)</h2>
		<br>
<%
				st = cn.createStatement();
				rs = st.executeQuery("select headline, date, news_id from news order by news_id desc");
				int rs_count = 0;
%>
		<table class="teagle" border="0">
		<colgroup>
				<col width="350"/>
				<col width="10"/>
				<col width="30"/>
				<col width="50"/>
				<col width="50"/>
			</colgroup>
			<thead>
    			<tr>
					<th bgcolor="#dddddd" style="height:25px">headline</th>
					<th bgcolor="#dddddd">date</th>
					<th bgcolor="#dddddd">id</th>
				</tr>
			</thead>
<%
				while (rs.next()){
					rs_count++;
%>
  			<tr>
  				<td bgcolor="#efefef"><strong><%= rs.getString("headline") %></strong></td>
  				<td bgcolor="#efefef"><strong><%= rs.getString("date") %></strong></td>
  				<td bgcolor="#efefef"><strong><%= rs.getString("news_id") %></strong></td>
  				<td bgcolor="#efefef" align="center"><a href="?action=edit&news_id=<%= rs.getString("news_id") %>">edit</a></td>
  				<td bgcolor="#efefef" align="center"><a href="?action=delete&news_id=<%= rs.getString("news_id") %>">delete</a></td>
			</tr>
<%
				}
%>
		</table>
<%
				if(rs_count == 0){
%>
		<p>No news found.</p>
<%
				}
%>
		<br>
		<a href="?action=create">Create</a> a news.
<%
			}
//*********************************
// CREATE
//*********************************
			if("create".equals(action)){
%>
		<h3>Create a news</h3>
		<form action="edit_news.jsp" method="post">
		<table class="teagle" border="0">
			<colgroup>
				<col width="165"/>
				<col />
			</colgroup>
			<tr>
				<td>Headline</td>
				<td><input name="headline" type="text" size="58" maxlength="200"/></td>
			</tr>
			<tr>
				<td>Description (for RSS feed)</td>
				<td><textarea name="description" cols="50" rows="5"></textarea></td>
			</tr>
			<tr>
				<td>Text (html capable)</td>
				<td><textarea name="text" cols="50" rows="15"></textarea></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="hidden" name="action" value ="insert"/></td>
			</tr>
		</table>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_news.jsp'"/><input type="submit" value="Create" style="width:<%=buttonbreite%>pt"/></p>
		</form>
<%
			}

//*********************************
// EDIT
//*********************************
			if("edit".equals(action)){
				st = cn.createStatement();
				rs = st.executeQuery("select * from news where news_id='" + request.getParameter("news_id") + "'");
		
				ResultSetMetaData rsmd = rs.getMetaData();
				int n = rsmd.getColumnCount();
	
				rs.next();
%>
		<h3>Edit News (news_id = <%=rs.getString("news_id")%>)</h3>
		<form action="edit_news.jsp" method="post">
			<table class="teagle" border="0">
				<colgroup>
					<col width="165"/>
					<col />
				</colgroup>
				<tr>
					<td>News ID</td>
					<td><input name="news_id" type="text" size="5" maxlength="11" value="<%=rs.getString("news_id") %>" readonly style="background-color:#dddddd"/></td>
				</tr>				
				<tr>
					<td>Headline</td>
					<td><input name="headline" type="text" size="58" maxlength="200" value="<%=rs.getString("headline") %>"/></td>
				</tr>
				<tr>
					<td>Description (for RSS feed)</td>
					<td><textarea name="description" cols="50" rows="5"><%=rs.getString("description")%></textarea></td>
				</tr>
				<tr>
					<td>Text (html capable)</td>
					<td><textarea name="text" cols="50" rows="15"><%=rs.getString("text")%></textarea></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="hidden" name="action" value ="update"/></td>
				</tr>
			</table>
			<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_news.jsp'"/><input type="submit" value="Edit" style="width:<%=buttonbreite%>pt"/></p>
		</form>
<%		
			}
//*********************************
// DELETE
//*********************************
			if("delete".equals(action)){
				if("yes".equals(request.getParameter("sure"))){
					st = cn.createStatement();
					st.execute("delete from news where news_id='" + request.getParameter("news_id") + "'");
					update_count = st.getUpdateCount();
%>
		<p><strong><%=update_count %> entry </strong> successfully deleted</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_news.jsp'"/></p>
<%					
				}
				else {
%>
		<h3>Are You sure?</h3>
		<p><input type="button" value="Cancel" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_news.jsp'"/><input type="button" value="Delete" style="width:<%=buttonbreite%>pt" onClick="window.location='?action=delete&news_id=<%=request.getParameter("news_id") %>&sure=yes'"/></p>
<%
				}
			}
//*********************************
// INSERT
//*********************************
			if("insert".equals(action)){
				if("".equals(request.getParameter("headline"))){
%>
		<h3>Headline must not be empty!</h3>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
<%
				}
				else {
					st = cn.createStatement(); 
					st.execute("insert into news (news_id, date, headline, text, description) values (null, now(), '" + request.getParameter("headline") + "','" + request.getParameter("text") + "', '" + request.getParameter("description") + "' )");
					update_count = st.getUpdateCount();
%>
		<p><strong><%=update_count %> entry </strong> successfully inserted</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_news.jsp'"/></p>	
<%
				}
	        }
//*********************************
// UPDATE
//*********************************
			if("update".equals(action)){
				if("".equals(request.getParameter("headline"))){
%>
		<h3>Headline must not be empty!</h3>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
<%
				}
				else {
					st = cn.createStatement();
					st.execute("update news set headline='" + request.getParameter("headline") + "', text='" + request.getParameter("text") + "', description='" + request.getParameter("description") + "' where news_id='" + request.getParameter("news_id") + "'");
					update_count = st.getUpdateCount();
%>
		<p><strong><%=update_count %> entry </strong> successfully updated</p>
		<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='edit_news.jsp'"/></p>
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
%>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>