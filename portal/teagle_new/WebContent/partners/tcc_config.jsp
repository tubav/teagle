<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.net.URLEncoder, java.net.URL, java.net.HttpURLConnection, java.io.*, de.fhg.fokus.tracingsupport.*, de.fhg.fokus.tracingsupport.ConfigFiller "%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
	String resourceId = request.getParameter("resourceId"); 
	pageContext.setAttribute("resourceId",resourceId);
	Config currentConfig = ConfigFiller.fill(resourceId);
	pageContext.setAttribute("config", currentConfig);
%>


<html>
<head>
<link href="../css/layout_2col_left_vlines.css" rel="stylesheet" type="text/css">
<link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">
<link rel="icon" href="../images/favicon.ico" type="image/x-icon">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>TCC Configuration</title>
<script>
function show_elements()
{
 var elementNames = show_elements.arguments;
 for (var i=0; i<elementNames.length; i++)
  {
    var elementName = elementNames[i];
    document.getElementById(elementName).style.display='block';
  }
}
function hide_elements()
{
 var elementNames = hide_elements.arguments;
 for (var i=0; i<elementNames.length; i++)
  {
    var elementName = elementNames[i];
    document.getElementById(elementName).style.display='none';
  }
}

</script>
</head>

<body style="background-color:#FFFFFF;" onload="hide_elements('filter_label', 'filter_input')">
<div>
	<h2>Configuration of the TCC</h2>
	
	Please, define the new configuration:
	<br/>
	<form method="post" action="../TccConfigServlet" onsubmit="window.close()">
		<label>Sink IP:</label><br/>
		<input type="text" name="sinkIp" value="${config.sinkIp}"/> <br/> 
		<label>Sink port:</label><br/>
		<input type="text" name="sinkPort" value="${config.sinkPort}"/> <br/> 
		<label>Traces type:</label><br/>
		<select size="1" name="tracesType"/>
			<option value="R" onclick="hide_elements('filter_label', 'filter_input')"<c:if test='${config.tracesType=="raw"}'><c:out value="selected"/></c:if>>raw</option>
			<option value="F" onclick="show_elements('filter_label', 'filter_input')"<c:if test='${config.tracesType=="processed"}'><c:out value="selected"/></c:if>>processed</option>
		</select><br>
		<label>Capture filter:</label><br/>
		<input type="text" name="captureFilter" value="${config.captureFilter}"/> <br/> 
		<label id="filter_label">Display filter:</label>
		<input id="filter_input" type="text" name="displayFilter" value="${config.displayFilter}"/>
		<input type="hidden" name="resourceId" value="${resourceId}"/>
		<br/> 
		<br/> 
		<input type="submit" value = "Send"/>
	</form>
</div>
</body>
</html>