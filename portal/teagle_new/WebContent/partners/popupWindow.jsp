
<jsp:useBean
	id="policyViewer"
	class="de.fhg.fokus.teaglewebsite.policyeditor.TeaglePolicyEditorViewer"
	scope="session"/>


<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

	<meta name="description" content="Open PE Website"/>

	<title>OpenPE-Policies Schema Listing</title>
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
	

</head>


<body>			
<%
	policyViewer.setServletConfig(config);
%>
				<div style="border: 1px solid black; padding: 0px;">
					<textarea id="code" name='policy' cols="140" rows="60"><%=policyViewer.getPolicySchemaContent()%></textarea>
				</div>
					
				<script type="text/javascript">
					  var editor = CodeMirror.fromTextArea('code', {
					    height: "680px",
					    parserfile: "parsexml.js",
					    stylesheet: "../xmleditor/css/xmlcolors.css",
					    path: "../xmleditor/js/",
					    continuousScanning: 500,
					    lineNumbers: true,
					    textWrapping: false
					  });
				</script>						
</body>
</html>