<?xml version="1.0" encoding="utf-8"?>
<rss version="2.0">
  <channel>
    <title>Teagle RSS Feed</title>
    <link>http://www.fire-teagle.org/</link>
    <description>The latest Teagle news</description>
    <language>en-us</language>
	<docs>http://blogs.law.harvard.edu/tech/rss</docs>
<%@ page import="java.sql.*"%>
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
		rs = st.executeQuery("select * from news order by news_id desc limit 0,10");
		int rs_count = 0;
		
		while (rs.next()){
			rs_count++;
%>
    <item>
      <title><%=rs.getString("headline")%></title>
      <link>http://www.fire-teagle.org/news.jsp?news_id=<%=rs.getString("news_id")%></link>
      <description><%=rs.getString("description") %></description>
      <pubDate><%=rs.getString("date") %></pubDate>
      <guid>http://www.fire-teagle.org/news.jsp?news_id=<%=rs.getString("news_id")%></guid>
    </item>
<%
		}
	}
	catch (Exception e) {
		e.printStackTrace();
 	} 
	finally {
 		try { if (null != rs) rs.close(); } catch (Exception ex) {}
 		try { if (null != st) st.close(); } catch (Exception ex) {}
 		try { if (null != cn) cn.close(); } catch (Exception ex) {}
 	}
%>
  </channel>
</rss>
