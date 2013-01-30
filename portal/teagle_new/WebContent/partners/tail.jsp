<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
	String resourceId = request.getParameter("resourceId"); 
	pageContext.setAttribute("resourceId",resourceId);
%>

<html>
<head>
<link href="../css/layout_2col_left_vlines.css" rel="stylesheet" type="text/css">
<link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">
<link rel="icon" href="../images/favicon.ico" type="image/x-icon">

<title><c:out value="Following ${resourceId}"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="../js/jquery-1.3.2.js"></script>

<script type="text/javascript">

var follow = true;

function switchFollow(){
	follow = !follow;
	alert(follow);
}

$(document).ready(function() {
		setInterval( "checkStatus()", 4000 );
	});

function checkStatus(resourceId){
	    //alert("CheckStatus called");
		var id = '<c:out value="${resourceId}"/>';
		$.ajax({
	          type: "GET",
//	          url: "/TailServlet",
			  url: "../TailTransporter",
	          data: "resourceId=" + id,
	          success: function(data){
					//		$('#log1')[0].textContent  = $('#log1')[0].textContent + data;
							$('#log1').val( $('#log1').val()+data );
							if(follow){
					        	$('#log1')[0].scrollTop = $('#log1')[0].scrollHeight;        
							}
	                   }
	     });
}	

</script>

</head>

<body>
	<h2>Tailing</h2>
	
	<textarea name="log1" readonly="readonly" class="log1" id="log1" cols="130" rows="30">The trace should appear here</textarea>
	<br>
	<a href="#" onClick="switchFollow()">Following/Stop following</a>
</body>
</html>