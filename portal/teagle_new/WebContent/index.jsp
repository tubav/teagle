<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Properties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ include file="fragments/header.jsp" %>

<script type="text/javascript" src="js/script.js"></script>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="home"/>
</jsp:include>

<!-- #col2: zweite Flaot-Spalte des Inhaltsbereiches -->
<div id="col2"><!--###col2### begin -->

	<div id="col2_content" class="clearfix">
	<!-- include the boxes on the right side -->
	
		<div class="csc-header csc-header-n1">
			<h1 class="csc-firstHeader">hosted by</h1>
		</div>
		<div> 
			<dl style="list-style-position:inside;list-style-type:none;text-align:center;margin:0px;padding:0px;"><dd style="text-align:center;margin:0px; margin-top:10px;" id="supportpic1"><a target="_blank" href="http://www.fokus.fraunhofer.de/en/fokus/index.html"><img src="images/fokus_logo_en.gif" width="130" height="39" alt="fokus_logo" border="0"></a></dd>
			</dl>
		</div>
		
		<div class="csc-header csc-header-n2">
			<h1 class="csc-firstHeader">supported by</h1>
		</div>
		<div> 
			<dl style="list-style-position:inside;list-style-type:none;text-align:center;margin:0px;padding:0px;"><dd style="text-align:center;margin:0px; margin-top:10px;" id="supportpic2"><p align="center"><a href="http://www.tu-berlin.de/menue/home/parameter/en/" target="_blank" title="TU Berlin"><img src="images/tub_logo.gif" alt="TU Berlin" /></a>&nbsp;&nbsp;<a href="http://www.av.tu-berlin.de" target="_blank" title="TU Berlin - AV"><img src="images/av_logo_en_small.jpg" alt="AV TU Berlin" /></a></p></dd>
			</dl>
		</div>


		
		<div class="csc-header csc-header-n3">
			<h1 class="csc-firstHeader">News</h1>
		</div>
		<div class="news-latest-container">
			<div class="news-latest-item">
<%
//JDBC-Stuff
	Connection cn = null;
	Statement st = null;
	ResultSet rs = null;
	try {
		Properties p = new Properties();
		p.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("mysql.properties"));
		
		Class.forName("com.mysql.jdbc.Driver");
		cn = DriverManager.getConnection(
				String.format("jdbc:mysql://%s:%s/%s", 
						p.getProperty("mysql.host"),
						p.getProperty("mysql.port"),
						p.getProperty("mysql.database"))
						, p.getProperty("mysql.user")
						, p.getProperty("mysql.password"));
		st = cn.createStatement();
		String news_id = request.getParameter("news_id");
		rs = st.executeQuery("select * from news order by news_id desc limit 0,2");
		
		while (rs.next()){
			String edescr    = rs.getString("description");
			String edate     = rs.getString("date");
			String eheadline = rs.getString("headline");
			String enews_id  = rs.getString("news_id");

			if(edescr.length()>90) edescr = edescr.substring(0,90) + " ...";
			String tmpl = 
"				<span class='news-latest-date'>%s</span>" +
"				<h3><a href='news.jsp?news_id=%s'>%s</a></h3>" +
"				<p class='bodytext'>%s<span class='news-morelink'><a href='news.jsp?news_id=%s'>more</a></span></p>" +
"				<hr class='clearer'>";

			String entry = String.format(tmpl, edate, enews_id, eheadline, edescr, enews_id);
			%><%=entry%><%
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
				<p class='bodytext'>
					No more news available.
				</p>
				<hr class='clearer'>
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
		
		<div class="csc-header csc-header-n3">
			<h1 class="csc-firstHeader">RSS Feed</h1>
		</div>
		<div class="news-latest-container">
			<div class="news-latest-item">
				<p class='bodytext'>
				<img src="images/feed-icon-14x14.png" style="position: relative; bottom: 4px;" alt=""> <a href="feed.rss">Teagle RSS Feed</a>
				</p>
			</div>
		</div>
	</div>
</div>
<!-- end: right column -->

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
			<br>
			<br>
			<p class="bodytext">
				Welcome to the Teagle Portal. This is a project related to the <a href="http://www.panlab.org/">Panlab concept</a>. The Teagle Portal provides information about our partner testbeds and allows you to manage your Private Virtual Test Lab.
			</p>
			<br>
			<p><a href="#" onClick="javascript:toggledisplay('hiddendiv_1'); return false" title="show/hide hidden div"><b>What is a Private Virtual Test Lab?</b></a></p>
			<div class="hiddendiv" id="hiddendiv_1">
				<p>
					A Private Virtual Test Lab is a testbed that is composed by a number of distributed software and hardware components situated in existing testbeds across Europe. It provides you with a testing and prototyping environment that supports your very specific requirements.
				</p>
				<p>
					For example use cases, please see the <a href="http://www.panlab.net/use-cases.html">Panlab use case page</a>.
			</div>
			<br>
			<p><a href="#" onClick="javascript:toggledisplay('hiddendiv_2'); return false" title="show/hide hidden div"><b>What is Teagle?</b></a></p>
			<div class="hiddendiv" id="hiddendiv_2">
				<p>
					Teagle is the central coordination instance that holds together all our partner test labs that are needed to provide you with the maximum range of testing and prototyping possibilities. All our partner labs form a federation of testbeds.
				</p>
				<p>
					<b>Via Teagle you can:</b>
				</p>
				<ul>
					<li>Browse resources provided by Panlab Partner labs</li>
					<li>Configure and deploy your own Private Virtual Test Lab</li>
					<li>Register new resources to be provided by the federation</li>
				</ul>
			</div>
			<br>
			
			For latest information please access our <a href="http://trac.panlab.net/trac/wiki">WIKI</a>. 
			
			
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>