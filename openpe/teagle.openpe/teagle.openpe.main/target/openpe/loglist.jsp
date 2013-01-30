<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@page import="de.tub.av.pe.rule.utils.LoggingBean"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />
<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="refresh" content="10"/>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

	<meta name="description" content="Open PE Website"/>

	<title>OpenPE</title>

	<link rel="stylesheet" href="stylesheets/screen.css" type="text/css" media="screen" title="no title" charset="utf-8"/>
	<!--[if gte IE 7]><link rel="stylesheet" href="stylesheets/ie7-screen.css" type="text/css" media="screen" title="no title" charset="utf-8"/><![endif]-->
	<style type="text/css" media="screen">

		/* new colors for xposer - greenish rgb(0,204,0)*/
		.navi, .navi > ul > li a:hover, .navi > ul > li.active > a:hover, .navi > ul > li.active > a, .content .button:hover {
			background-color: rgb(204, 245, 204); /* 20% */
		}
			.navi > ul > li a, .content .button {
				background-color: rgb(153, 235, 153); /* 40% */
			}
		.main a:hover {
			color: rgb(0,204,0);
			/*color: rgb(200, 230, 30);*/
			border-bottom: 1px solid rgb(0,0,0);
		}
		.content h1, .content h2, .content h3, .content h4 {
			color: #736F6E;
		}

		.content a:visited{
			color:#00008b;
			border-bottom: 1px solid rgb(0,0,0);
		}

		/* custom logo */
		.app_logo {
			display: block;
			margin: 3.5em 1.5em 0 0;
		}
		.app_logo img {
			width: 187px;
		}

		/*  location && size of clientlogo */
		.logo_client img {
			right: 130px;
			bottom: 12px;
			width: 82px;
		}

	</style>

</head>

<%@ page import="java.util.ArrayList" %>

<body>

	<div class="app">
		
		<div class="navi">

			<a class="app_logo" href="#" title="Home">
				<img src="images/logo_opensoatelco-xposer.png" alt="OpenSOATelco XPOSER"/>
			</a>

			<ul>
				<li>
					<a href="index.html" title="Open PE - Policy Evaluation Engine">OpenPE</a>
				</li>
				<li>
					<a href="configuration.jsp?configuration=Open%20Policy%20Engine" title="Configuration">Configuration</a>
				</li>
				<li class="active">
					<a href="#" title="Log Overview">Log Overview</a>
				</li>
				<li>
					<a href="editor/policyEditor.jsp" title="Editor">PolicyRepository</a>
				</li>	
			</ul>
		</div>

		<div class="main">
			
			<div class="breadcrumb">
				<a href="index.html" title="Home">Home</a> > <a href="openpe.html" title="Open PE"><abbr title="Open Policy Engine">Open PE</abbr></a> > <abbr title="Open PE Log Overview">Log Overview</abbr>
			</div>
 			
 			<div class="content">	

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
LoggingBean logging = editorBean.getOpenPEContext().getLoggingBeanObject();
%>

	<% if(request.getParameter("refresh")==null) {%>	
<div id="dialog-PEReason" class="dialog" style="display:none" title="Reason"></div>
 			
 			<div class="content">	
					
				<h1>Policy Engine Log Overview</h1>
				<p>In order to find more information about the policy evaluation result click on the decision link of the interested message </p>
				<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" summary="This table contains web services" id="policyTable" >
					<thead>
						<tr>
							<th>id</th>
							<th>decision</th>
							<th>originator</th>
							<th>target</th>
							<th>event</th>	
						</tr>
					</thead>
					<tbody style="font-size:8pt" class="policyTableBody">
<%
	
	if (logging != null) {
		
	
		List<LoggingBean.LogEntry> infos = logging.getLogEntries();		
		for (int i = 0; i < infos.size(); i++) 
		{
			LoggingBean.LogEntry info = infos.get(i);
			/*CCF5CC*/
			String bgcolor = ((i % 2) != 0 ? "#f0f0f0" : "#dcdcdc");
%>				
						<tr style="background-color:<%=bgcolor %>">
							<td>
								 <%=i+1 %>
							</td>
							<td>
<%
			String color = "green";
			if (info.getFinalDecission().equals("DENIED")) {
				color = "red";
			}
			if (info.isError()) {
				color = "blue";
			}
%>
								<a href="#" onclick="showReason('<%=i+1 %>')" title="Reason" style="color:<%=color %>"> <%=info.getFinalDecission() %></a>
							</td>
							<td>
								<%=info.getOriginator() %>
							</td>
							<td>
								<%=info.getTarget() %>
							</td>
							<td>
								<%=info.getEvent()%>&nbsp;
							</td>

						</tr>
<%
		}
	}
%>
					</tbody>
				</table>

	
			</div>
	<script type="text/javascript" >

var t=setTimeout("timedCount()",10000);
function timedCount()
{
	
$.post("content/PE/loglist.jsp",{refresh:"refresh"}, function(data){

	$(".policyTableBody").html(data);
	
});
t=setTimeout("timedCount()",10000);
}


</script>
<% } else { %>
	
<%
	
	if (logging != null) {
		
	
		List<LoggingBean.LogEntry> infos = logging.getLogEntries();		
		synchronized(infos)
		{
		for (int i = 0; i < infos.size(); i++) 
		{
			LoggingBean.LogEntry info = infos.get(i);
			/*CCF5CC*/
			String bgcolor = ((i % 2) != 0 ? "#f0f0f0" : "#dcdcdc");
%>				
						<tr style="background-color:<%=bgcolor %>">
							<td>
								 <%=i+1 %>
							</td>
							<td>
<%
			String color = "green";
			if (info.getFinalDecission().equals("DENIED")) {
				color = "red";
			}
			if (info.isError()) {
				color = "blue";
			}
%>
								<a href="#" onclick="showReason('<%=i+1 %>')" title="Reason" style="color:<%=color %>"> <%=info.getFinalDecission() %></a>
							</td>
							<td>
								<%=info.getOriginator() %>
							</td>
							<td>
								<%=info.getTarget() %>
							</td>
							<td>
								<%=info.getEvent()%>&nbsp;
							</td>
						</tr>
<%
		}
		}
	}
%>
					
<% }%>
	
	
	
	
			</div>
		</div>	

		<div class="opt">
		</div>
	
	</div>

	<div class="corp">
		<a class="logo_client" href="http://www.fokus.fraunhofer.de/de/ngni/" title="Homepage NGNI">
			<img src="images/logo_NGNI.png" alt="NGNI Logo"/>
		</a>

		<a class="logo_fhg" href="http://fokus.fraunhofer.de" title="Homepage Fraunhofer FOCUS">
			<img src="images/logo_focus.png" alt="Fraunhofer FOCUS"/>
		</a>

		<div class="corp-bg-l"><img src="images/bg_corp-l.png" alt=""/></div>
		<div class="corp-bg-r"><img src="images/bg_corp-r.png" alt=""/></div>
	</div>


</body>
</html>
