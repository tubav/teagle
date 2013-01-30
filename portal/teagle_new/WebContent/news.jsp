<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="news" />
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
			<h2>News</h2>
<%
//JDBC-Stuff
	Connection cn = null;
	Statement st = null;
	ResultSet rs = null;
	try {
		Class.forName("com.mysql.jdbc.Driver");
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/teagle", "teagle", "*4teagle#");
		st = cn.createStatement();
		String news_id = request.getParameter("news_id");
		if(news_id == null){
			rs = st.executeQuery("select * from news order by news_id desc");
			int rs_count = 0;
			
			while (rs.next()){
				rs_count++;
%>
			<div style="margin-top:2em">
				<h4><%=rs.getString(2)%> - <%=rs.getString(3)%></h4>
				<div>
					<p style="text-align: justify;">
					<%=rs.getString(4) %>
					</p>
				</div>
			</div>
<%
			}
		}
		else{
			try{
				rs = st.executeQuery("select * from news where news_id='" + news_id + "'");
				int rs_count = 0;
				rs.next();
				String temp = rs.getString(2); 
%>
			<div style="margin-top:2em">
				<h4><%=temp%> - <%=rs.getString(3)%></h4>
				<div>
					<p style="text-align: justify;">
					<%=rs.getString(4) %>
					</p>
				</div>
			</div>
<%	
			}
			catch (Exception e){
%>
			<p>No news found for news_id <%=news_id%>.</p>
<%
			}
%>
			<p>See <a href="news.jsp">all news</a>.</p>
<%
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
			<h3>Something went wrong!</h3>
			<p><a href="javascript:history.back();">Back</a></p>
			<h3>Exception:</h3>
			<code><%=e.toString()%></code> 
<%
 	} 
	finally {
 		try { if (null != rs) rs.close(); } catch (Exception ex) {}
 		try { if (null != st) st.close(); } catch (Exception ex) {}
 		try { if (null != cn) cn.close(); } catch (Exception ex) {}
 	}
%>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>