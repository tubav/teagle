
<%@page import="de.tub.av.pe.editor.ItemHtmlElementEnum"%>
<%@page import="de.tub.av.pe.editor.EditorDataItem"%>
<%@page import="de.tub.av.pe.editor.PEEditor"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">


<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>


<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Map"%>


<%@page import="java.util.ArrayList"%><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

	<meta name="description" content="Open PE Website"/>

	<title>XPOSER</title>
	<script src="xmleditor/js/codemirror.js" type="text/javascript"></script>	
	<script type="text/javascript" src="lib/jsonrpc.js"> </script>
	<script src="lib/ajaxEnablerFunctions.js" type="text/javascript"></script>	
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
    
    <script type="text/javascript" charset="utf-8">
		function popitup(url) {
			newwindow=window.open(url,'name','height=700,width=600');
			if (window.focus) {newwindow.focus()}
			return false;
		}
		   		
	</script>
    
	

</head>

<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>

<body>




	<div class="app">
		
		<div class="navi">

			<a class="app_logo" href="#" title="Home">
				<img src="images/logo_opensoatelco-xposer.png" alt="OpenSOATelco XPOSER"/>
			</a>

			<ul>
				<li>
					<a href="../../index.html" title="XPOSER - The FOKUS SOA Broker"><abbr title="SOA Broker">XPOSER</abbr></a>
				</li>
				<li class="active">
					<a href="../openpe.html" title="Open PE - Policy Evaluation Engine">OpenPE</a>
					<ul>
						<li>
							<a href="../loglist.jsp" title="Log Overview">Log Overview</a>
						</li>
						<li class="active">
							<a href="policyEditor.jsp" title="Policy Editor">Policy Editor</a>
							<%
								String editorScope = request.getParameter("scope");
								if(editorScope == null)
									editorScope = "ProfilePolicy";
								String[] editorScopes = editor.getAvailableEditorsScopes();
								for (int i = 0; i < editorScopes.length; i++)
								{
									if(editorScope.equals(editorScopes[i]))
									{
							%>
										<ul class=active>	
							<% 			
									}else{
							%>
										<ul>
							<%
									}
							%>		
										<a href="policyEditor.jsp?scope=<%= editorScopes[i]%>"+ title="<%= editorScopes[i]%>"><%= editorScopes[i] + " Editor"%></a>
									</ul>
							<% 
									
								}
							%>
						</li>						
					</ul>
				</li>
			</ul>
		</div>

		<div class="main">
			
			<div class="breadcrumb">
				<a href="index.html" title="Home">Home</a> > <a href="openpe.html" title="Open PE"><abbr title="Open Policy Engine">Open PE</abbr></a> > <abbr title="Open PE Log Overview">Log Overview</abbr>
			</div>
 			
 			<div class="content">				

 			
 			<article>				

				<h1>Create new data of category <%=editorScope%> </h1>

				<%	
					String executionType = request.getParameter("executionType");	
				
					List<EditorDataItem>  newObject;
			
					if (executionType != null)
					{
						newObject = editor.getToSaveNewDataObject(editorScope);
						String errorMessage = request.getParameter("error");
				%>
					<p style="color:red"> <i>Error Message: <%=errorMessage%></i></p>
				<% 		
					}
					else 
						newObject = editor.getNewDataObject(editorScope);
				%>
				
								
				<form accept-charset="utf-8" style="background-color:white" method="post" action="index.jsp?navi=processNewData">
					<fieldset>
				<%	
				
					for (int i = 0; i < newObject.size(); i++)
					{
						EditorDataItem edi = newObject.get(i);
						if(edi.getHtmlElement() == ItemHtmlElementEnum.TEXT)
						{							
				%>
						<label for="<%=edi.getName()%>" title="Name"><%=edi.getName()%>:</label><input type="text" name="<%=edi.getName()%>" value="<%=edi.getValue() instanceof String?(String)edi.getValue():""%>"/>
				<%		
						}
						else if (edi.getHtmlElement() == ItemHtmlElementEnum.SELECTWITHEVENT)
						{
							EditorDataItem triggeredEdi = edi.getTrigeredEditorItem();
							
				%>
							<select style="width:8em" id=<%=edi.getName()%> name="<%=edi.getName()%>" onchange="getAssociatedData(<%=triggeredEdi.getName()%>, this.value)">
								<% 
									Object value = edi.getValue();
									if(value instanceof List<?>)
									{
										List<?> list = (List<?>)value;
										for (int k = 0; k < list.size(); k++)
										{
											if(list.get(k) instanceof String)
											{
												String input = (String)list.get(k);
								%>
											<option <%=input.equals(edi.getOldInput())?"selected":"" %>><%=input %></option>

								<%			
											}
										}
									}
								%>	
							</select>
				<%			
						}else if (edi.getHtmlElement() == ItemHtmlElementEnum.SELECT)
						{
				%>
						<select style="width:8em" id=<%=edi.getName()%> name="<%=edi.getName()%>">
							<% 
								Object value = edi.getValue();
								if(value instanceof List<?>)
								{
									List<?> list = (List<?>)value;
									for (int k = 0; k < list.size(); k++)
									{
										if(list.get(k) instanceof String)
										{
											String input = (String)list.get(k);
							%>
									<option <%=input.equals(edi.getOldInput())?"selected":""%>><%=input%></option>
							<%
										}
									}
								}
							%>
						</select>
				<%									
						}else if(edi.getHtmlElement() == ItemHtmlElementEnum.INPUTWITHSUGGESTIONS)
						{
				%>			
						<input type="hidden" id="<%=edi.getName()%>" value="<%=edi.getOldInput()!=null?edi.getOldInput():"" %>"/>
						<div class="ui-widget">
							<input type="hidden" id="suggest_type" value="<%=edi.getName()%>"/>	
							<input type="text" id="suggestAC" value=""/>
						</div>
						
						<script type="text/javascript">
	
						  $(function() {
							  
							var jsonrpc = new JSONRpcClient("JSON-RPC");
							
							var result = jsonrpc.JSONMethods.getSuggestData($( "#suggest_type" ).val());
							var suggestions = result.list;  
							var boxname = $( "#suggest_type" ).val();
					
							$( "#suggestAC" ).autocomplete({
								source: suggestions,
								minLength: 2,
								change: function( event, ui ) {
						             $('#'+boxname ).val(this.value);						             
								}
							});
						});
						</script>
							
				<% 									
						}
						else if (edi.getHtmlElement() == ItemHtmlElementEnum.TEXTAREA)
						{
				%>	
						<input type="submit" value="View Schema" class="submit green" onclick="popitup('content/PE/editor/popupWindow.jsp');return false;"/>
						<div style="border: 1px solid black; padding: 0px;">
							<textarea id="code" name='policy' style="display:none !important" cols="120" rows="60"><%= edi.getValue()== null?"":edi.getValue()%></textarea>
						</div>
							
						<script type="text/javascript">
							  var editor = CodeMirror.fromTextArea('code', {
							    height: "350px",
							    width: "50em",
							    parserfile: "parsexml.js",
							    stylesheet: "css/xmleditor/xmlcolors.css",
							    path: "js/xmleditor/",
							    continuousScanning: 500,
							    lineNumbers: true,
							    textWrapping: false
							  });
						</script>						
				<% 		
							
						}
					}
				
				%>
				</fieldset>
							<input type="hidden" name="editorScope" value="<%= editorScope%>"/>
						
							<input type="submit"  class="submit button" style="background-color:#f0f0f0" value="Save"/>
							
							<!--<input type="button" class="submit button" onclick="document.location='index.jsp?navi=PolicyEditor&scope=<=editorScope %>'" style="background-color:#f0f0f0" value="Back"/> -->
							<input type="button" class="submit button" onclick="history.go(-1);" style="background-color:#f0f0f0" value="Back"/>
						</form>													
				

			</article>
				
				
				
				

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
