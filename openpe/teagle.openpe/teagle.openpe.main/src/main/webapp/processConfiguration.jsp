<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page import="de.tub.av.pe.configuration.ConfigurationBean"%>
<%@page import="de.tub.av.pe.configuration.ServiceConfiguration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>

<%
	String srvconfig = request.getParameter("configuration");
	String task = request.getParameter("hiddenimsconfig");
	if (task != null && task.equals("saveapply")) {
		ServiceConfiguration srv = ConfigurationBean.getServiceConfiguration(srvconfig);
		srv.postSave();
		Map<String, String[]> params = (Map<String, String[]>)request.getParameterMap();
		Iterator<Map.Entry<String, String[]>> entries = params.entrySet().iterator();
		while (entries.hasNext()) {
	Map.Entry<String, String[]> entry = entries.next();
	ServiceConfiguration.Parameter param = srv.getParameter(entry.getKey());
	if (param != null) {
		param.setValue(entry.getValue()[0]);
	}
		}
		srv.save();
		for (ServiceConfiguration subsrv : srv.getServiceConfigs()) {
	subsrv.postSave();
	Iterator<Map.Entry<String, String[]>> subentries = params.entrySet().iterator();
	while (subentries.hasNext()) {
		Map.Entry<String, String[]> entry = subentries.next();
		ServiceConfiguration.Parameter param = subsrv.getParameter(entry.getKey());
		if (param != null) {
	param.setValue(entry.getValue()[0]);
		}
	}
	subsrv.save();			
		}
	}
%>

<jsp:forward page="configuration.jsp" >
<jsp:param name="configuration" value="<%= srvconfig %>" />
</jsp:forward> 
