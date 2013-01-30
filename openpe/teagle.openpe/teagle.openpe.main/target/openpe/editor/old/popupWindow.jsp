<%@page import="de.tub.av.pe.editor.drools.utils.EditorUtils"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<%@page import="de.tub.av.pe.editor.PEEditor"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

	<meta name="description" content="Open PE Website"/>

	<title>OpenPE-Policies Schema Listing</title>
	<script src="js/xmleditor/codemirror.js" type="text/javascript"></script>	
	<script src="js/ajaxEnablerFunctions.js" type="text/javascript"></script>	

	<link rel="stylesheet" href="css/screen.css" type="text/css" media="screen" title="no title" charset="utf-8"/>
	<!--[if gte IE 7]><link rel="stylesheet" href="stylesheets/ie7-screen.css" type="text/css" media="screen" title="no title" charset="utf-8"/><![endif]-->
	<style type="text/css" media="screen">
		body>div{position:absolute;top:0;bottom:0;left:0;right:0;}
		
		/* new colors for broker - greenish rgb(0,204,0)*/
		.navi, .navi > ul > li a:hover, .navi > ul > li.active > a:hover, .navi > ul > li.active > a, .content .button:hover {
			background-color: rgb(204, 245, 204); /* 20% */
		}
			.navi > ul > li a, .content .button {
				background-color: rgb(153, 235, 153); /* 40% */
			}
		.main a:hover {
			color: rgb(0,204,0);
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
	

</head>

<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>

<body>			
				<div style="border: 1px solid black; padding: 0px;">
					<textarea id="code" name='policy' cols="140" rows="60"><%=EditorUtils.GetPolicySchemaContent(editor.getProperty("openpe.basedir"))%></textarea>
				</div>
					
				<script type="text/javascript">
					  var editor = CodeMirror.fromTextArea('code', {
					    height: "680px",
					    parserfile: "parsexml.js",
					    stylesheet: "css/xmleditor/xmlcolors.css",
					    path: "js/xmleditor/",
					    continuousScanning: 500,
					    lineNumbers: true,
					    textWrapping: false
					  });
				</script>						
</body>
</html>