<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page import="de.tub.av.pe.configuration.ConfigurationBean"%>
<%@page import="de.tub.av.pe.configuration.ServiceConfiguration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.List" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

	<meta name="description" content="XPOSER"/>
	<title>OpenPE</title>

	<link rel="stylesheet" href="stylesheets/screen.css" type="text/css" media="screen" title="no title" charset="utf-8"/>
	<!--[if gte IE 7]><link rel="stylesheet" href="stylesheets/ie7-screen.css" type="text/css" media="screen" title="no title" charset="utf-8"/><![endif]-->
	<style type="text/css" media="screen">
	
		/* new colors for xposer - greenish rgb(0,204,0)*/
		.navi, .navi > ul > li a:hover, .navi > ul > li.active > a:hover, .navi > ul > li.active > a, .content .button:hover {
			background-color: rgb(204, 245, 204); /* 20% */
		}
			.navi > ul > li a, .content .button {
				background-color: rgb(153, 235, 153);  /* 40% */
			}
		.main a:hover {
			color: rgb(0,204,0);
			/*color: rgb(200, 230, 30);*/
			border-bottom: 1px solid rgb(0,0,0);
		}
		.content h1, .content h2, .content h3, .content h4 {
			/*color: rgb(0,204,0);*/
			color: #736F6E;
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

	<script src="javascripts/jquery-1.2.6.js" type="text/javascript"></script>
	<script src="javascripts/jquery.tooltip.js" type="text/javascript"></script>
	<script type="text/javascript" charset="utf-8">
		jQuery(function(){
		tooltip();
		});
	</script>

</head>

<jsp:useBean id="configHandler" class="de.tub.av.pe.configuration.ConfigurationBean" scope="application" />

<body>

	<div class="app">
		
		<div class="navi">

			<a class="app_logo" href="#" title="Home">
				<img src="images/logo_opensoatelco-xposer.png" alt="OPEN SOA Telco XPOSER"/>
			</a>

			<ul>
				<li>
					<a href="index.html" title="Open PE - Policy Evaluation Engine">OpenPE</a>
				</li>
<%
	ServiceConfiguration srvconfig = null;
	String activeConfig = request.getParameter("configuration");
	if (activeConfig == null || activeConfig.equals("")) {
		activeConfig = "Open Policy Engine";
	} 
	srvconfig = ConfigurationBean.getServiceConfiguration(activeConfig);
%>

				<li class="active">
					<a href="configuration.jsp" title="Configuration - Edit settings">Configuration</a>
					<ul>
<%
	List<ServiceConfiguration> srvconfigs = ConfigurationBean.getConfigs();
	for (ServiceConfiguration srv : srvconfigs) {
		if (srv == srvconfig) {
%>
						<li class="active">
<%
	} else {
%>
						<li>
<%
	}
%>
							<a href="?configuration=<%=srv.getServiceName()%>" title="<%=srv.getServiceName()%>"><%=srv.getServiceName()%></a>		
						</li>
<%
	}
%>						
					</ul>
				</li>
				<li>
					<a href="loglist.jsp" title="Log Overview">Log Overview</a>
				</li>
				<li>
					<a href="editor/policyEditor.jsp" title="Editor">Policy Repository</a>
				</li>
			</ul>
		</div>
		
		<div class="main">

			<div class="content">				

				<h1><%=srvconfig.getServiceName()%> Configuration</h1>
<%
	if (srvconfig != null) {
%>

				<form action="processConfiguration.jsp" method="post" name="imsconfig" accept-charset="utf-8">
					<fieldset>
<%
	for (ServiceConfiguration.Parameter parameter : srvconfig.getParameters()) {
	String name = parameter.getName();
	String label = parameter.getLabel();
	String value = parameter.toString();
	String description = parameter.getDescription();
	String type = parameter.getType();
	if (type.equals("String") || type.equals("Integer")) {
%>
						<div>
							<label for='<%=name%>'><%=label%>:</label>
							<input id='<%=name%>'name='<%=name%>' type="text" title='<%=label%>' value='<%=value%>'/>
						</div>
<%
	} else if (type.equals("Boolean")) {
%>
						<div>
							<label for='<%=name%>'><%=label%>:</label>
							<input id='<%=name%>' name='<%=name%>' type="checkbox" title='<%=label%>' value="true" <%=(value.equalsIgnoreCase("true") ? "checked" : "")%> />
						</div>
<%
	}
	}
%>
					</fieldset>
<%
	List<ServiceConfiguration> subconfigs = srvconfig.getServiceConfigs();
		for (ServiceConfiguration sub : subconfigs) {
%>
					<fieldset>
						<legend><%=sub.getServiceName()%></legend>
<%
	for (ServiceConfiguration.Parameter parameter : sub.getParameters()) {
		String name = parameter.getName();
		String label = parameter.getLabel();
		String value = parameter.toString();
		String description = parameter.getDescription();
		String type = parameter.getType();
		if (type.equals("String") || type.equals("Integer")) {
%>
						<div>
							<label for='<%= name %>'><%= label %></label>
							<input id='<%= name %>' name='<%= name %>' type="text" title='<%= label %>' value='<%= value %>'/>
							<a href="#" class="submit button inline" title='<%= description %>'><img src="images/icon_help.png" alt="?"/></a>
						</div>
<%				} else if (type.equals("Boolean")) { %>
						<div>
							<label for='<%= name %>'><%= label %></label>
							<input id='<%= name %>' name='<%= name %>' type="checkbox" title='<%= label %>' value="true" <%= (value.equalsIgnoreCase("true") ? "checked" : "") %> />
							<a href="#" class="submit button inline" title='<%= description %>'><img src="images/icon_help.png" alt="?"/></a>
						</div>
<%				}
			} %>
					</fieldset>
<%		} %>
					<input type="hidden" name="hiddenimsconfig" value="saveapply"/>
					<input type="hidden" name="configuration" value='<%= srvconfig.getServiceName() %>'/>
					<a href="#" class="submit button"  onclick="document.imsconfig.hiddenimsconfig.value='reload'; document.imsconfig.submit(); return false;">Reload</a>
					<a href="#" class="submit button" onclick="document.imsconfig.submit(); return false;">Save & Apply</a>
				</form>
<%	} %>
			</div>
		</div>

		<div class="opt">
		</div>
	
	</div>


	<div class="corp">
		<a class="logo_client" href="http://www.fokus.fraunhofer.de/de/ngni/" title="Homepage NGNI">
			<img src="images/logo_NGNI.png" alt="NGNI Logo"/>
		</a>

		<a class="logo_fhg" href="http://fokus.fraunhofer.de" title="Homepage Fraunhofer FOKUS">
			<img src="images/logo_focus.png" alt="Fraunhofer FOKUS"/>
		</a>

		<div class="corp-bg-l"><img src="images/bg_corp-l.png" alt=""/></div>
		<div class="corp-bg-r"><img src="images/bg_corp-r.png" alt=""/></div>
	</div>

</body>
</html>
