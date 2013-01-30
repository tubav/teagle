<%
	String depth;
	if(request.getParameter("depth") == null){
		depth = "";
	}
	else{
		depth = request.getParameter("depth");
	}
%>

<html>
<head>
<title>Teagle</title>
<meta name="description" content="">
<meta name="keywords" content="">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="robots" content="index, follow">
<meta name="revisit-after" content="7 days">

<!--  CodeMirror necessary for the policy editor textarea parsing-->
 <script src="../xmleditor/js/codemirror.js" type="text/javascript"></script>	
 <style type="text/css">
      .CodeMirror-line-numbers {
        width: 2.2em;
        color: #aaa;
        background-color: #eee;
        text-align: right;
        padding-right: .3em;
        font-size: 10pt;
        font-family: monospace;
        padding-top: .4em;
      }
 </style>
 

 

<%
	if("no".equals(request.getParameter("boxes"))){
%>
<link href="<%=depth%>css/layout_2col_left_vlines.css" rel="stylesheet" type="text/css">
<%
	}
	else{
%>
<link href="<%=depth%>css/layout_3col_vlines.css" rel="stylesheet" type="text/css">
<%
	}
%>

<link rel="shortcut icon" href="<%=depth%>images/favicon.ico" type="image/x-icon">
<link rel="icon" href="<%=depth%>images/favicon.ico" type="image/x-icon">

<%
	if("true".equals(request.getParameter("login"))){
%>
</head>
<body onLoad="document.loginform.j_username.focus();">
<%
	}
	else{
%>
</head>
<body>
<%
	}
%>

<div id="page_margins"><!--###page_margins### begin -->
<div id="page" class="hold_floats"><!--###page### begin -->
<div id="header">
<div id="topnav">
<a class="skip" href="#navigation" title="Direkt zur Navigation springen">Skip to navigation</a><a class="skip" href="#content" title="Direkt zum Content springen">Zum Content springen</a><a href="http://www.panlab.net/contact.html">Contact</a>&nbsp;&#124;&nbsp;<a href="http://www.panlab.net/search.html" title="inside of this page">Search</a>&nbsp;&#124;&nbsp;<a href="http://www.panlab.net/sitemap.html">Sitemap</a>
</div>
<img src="<%=depth%>images/PII_7_01.jpg" alt="Panlab, Pan European Laboratory for Next Generation Networks and Services" title="Panlab, Pan European Laboratory Infrastructure Implementation" >
</div>

<!-- #nav: Hauptnavigation -->
<div id="nav"><!--###nav### begin -->
	<a id="navigation" name="navigation"></a> <!-- Skiplink-Anker: Navigation -->
	<div id="nav_main"><ul><li id="current"><a href="http://www.panlab.net/" title="Zurück zur Startseite" target="_top"><span>back to Panlab</span></a></li><li><a href="<%=depth%>." title="Teagle Home"><span>Teagle Home</span></a></li></ul></div>
<!--###nav### end --></div>
<!-- #nav: - Ende -->

<!-- #main: Beginn Inhaltsbereich -->
<div id="main"><!--###main### begin -->
<div id="teaser" class="clearfix"><div class="block1">You are Here:&nbsp;<a href="<%=depth%>." title="Teagle">Teagle</a></div></div>
<a id="content" name="content"></a> <!-- Skiplink-Anker: Content -->