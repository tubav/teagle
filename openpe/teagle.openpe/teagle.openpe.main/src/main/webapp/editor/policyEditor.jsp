<%@page import="de.tub.av.pe.editor.PEEditor"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">


<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>


<%@page import="java.util.Map"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

	<meta name="description" content="Open PE Website"/>

	<title>XPOSER</title>

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
		
		/*added by Daniel */
	.navi > ul > li li li a {
					font-size: 1.0em;
					font-weight: normal !important;
				}
			.navi > ul > li li li.active a {
						font-weight: bold !important;
				}
			.navi > ul > li li li:first-child a {
					background:#fff url(../images/bg_nav-item_active.png) repeat-x scroll 0 -2.2em !important;
				}


	</style>

</head>

<%@ page import="java.util.ArrayList" %>

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
						<li >
							<a href="policyEditor.jsp" title="Policy Editor">Repository Management</a>
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

				<h1><%=editorScope%> Editor</h1>
				
				<p>View and Edit <%=editorScope%> data</p>
				
			<%
					int pagesize = 10;
				
					int pageNr = request.getParameter("pageNr") == null?0:Integer.parseInt(request.getParameter("pageNr"));
					String filterName = "";
					String filterValue = "";
					
					List<Map<String, String>> dataList = null; 
					
					if(request.getParameter("filterName")!=null)
					{
						filterName = request.getParameter("filterName");
						filterValue = request.getParameter("filterValue");
						dataList = editor.getDataList(editorScope, pageNr*pagesize, pagesize, filterName, filterValue);		
					}
					else
					{
						dataList = editor.getDataList(editorScope, pageNr*pagesize, pagesize);
					}
					
					if(dataList != null)
					{							
					 String[] columnNameList = editor.getDataKeys(editorScope);
			%>

			<form accept-charset="utf-8" style="background-color:white" method="post" name="t1" action="policyEditor.jsp">
				<fieldset>
								 
								 <label for="filterName"> SelectBy:</label>
								 <select name="filterName"> 
						<%
							for (int i =0; i < columnNameList.length; i++ )
							{
								String columnName = columnNameList[i];
								if(!columnName.equals("policy") && !columnName.equals("id"))
								{
						%>		
								<option <%= filterName.equals(columnNameList[i])?"selected":""%>><%=columnNameList[i]%></option>					
						<%
								}				
							}
						%>		
								</select>
								<input type="text" name="filterValue" value="<%=filterValue %>" size="10"></input>
								<input type="hidden" name="scope" value="<%=editorScope %>"/>
								<input type="submit"  class="submit" value="GO"/>		
								</fieldset>			
						</form>
							<%if(!editorScope.equals("User")){ %>
						<form accept-charset="utf-8" style="background-color:white display:inline" name="Form" method="post" action="newCategoryData.jsp">
							<fieldset>
							<input type="hidden" name="scope" value="<%=editorScope %>"/>
						
							<input type="submit"  class="submit" value="Create New <%=editorScope %>"/>		
							
							</fieldset>			
						</form>
<% } %>
				
				<form>
				<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" summary="This table contains web services" >
					<thead>
						<tr>
							
							<% 
								
								for (int i =0; i < columnNameList.length; i++ )
								{
									String columnName = columnNameList[i];
									if(!columnName.equals("policy") && !columnName.equals("id"))
									{
							%>
									<th><%=columnName%></th>		
							<%
									}
								}
							%>		
							<th style="text-align:right;">operations</th>				
						</tr>
					</thead>
					<tbody >					
						<%
							for (int i = 0 ; i < dataList.size(); i++)
							{
								/*CCF5CC*/
								String bgcolor = ((i % 2) != 0 ? "#f0f0f0" : "#dcdcdc");
						%>
							<tr style="background-color:#f0f0f0">
								
								<%
									Map<String, String> map = dataList.get(i);
	
									for (int k = 0; k < columnNameList.length; k++ )
									{
										if(!columnNameList[k].equals("policy") && !columnNameList[k].equals("id"))
										{
											String value = map.get(columnNameList[k]);								
								%>
									<td><%= value%></td>
								<%
										}
									}
								%>
								<td style="text-align:right;">
									<input class="submit green" type="submit" onclick="submitPE('categoryEdit.jsp?scope=<%=editorScope%>&pageNr=<%=pageNr%>&id=<%= map.get("id")%>');return false;" value="Edit"> &nbsp;
									<input type="submit" class="submit red" onclick="submitPE('categoryDelete.jsp?scope=<%=editorScope%>&pageNr=<%=pageNr%>&id=<%=map.get("id")%>');return false;" value="Delete" >
								</td>								
							</tr>
							<tr style="font-size:3pt">
								<td>&nbsp;</td>								
							</tr>
						<%									
							}
						%>
					</tbody>
				</table>
				</form>
				<ul style="list-style-image:none; list-style-type:none;padding: 0px;">
				<%		
				
					int resultDataSize = dataList.size();				
					 if(pageNr == 0)
						{
							if(pagesize < resultDataSize)
							{					
					%> 
								<li style="float:right">						
								     <a href="policyEditor.jsp?scope=<%=editorScope%>&pageNr=<%=pageNr+1%> "+ title="forward" class="images"><img src="img/next.png"></img></a>					
								</li>
					<%
							}
						} else if (pageNr*pagesize+ pagesize >= resultDataSize)
						{				
					%>
								<li style="float:left">												
								    <a href="policyEditor.jsp?scope=<%=editorScope%>&pageNr=<%=pageNr-1%> "+ title="back" class="images"><img src="img/back.png"></img></a>												
								 
								</li>						
									
					<%									
						}else{
					%>
							<li style="float:left">																			
							   <a href="policyEditor.jsp?scope=<%=editorScope%>&pageNr=<%=pageNr-1%> "+ title="back" class="images"><img src="img/back.png"></img></a>
							</li>
							<li style="float:right">
							   <a href="policyEditor.jsp?scope=<%=editorScope%>&pageNr=<%=pageNr+1%> "+ title="forward" class="images"><img src="img/next.png"></img></a>
							</li>
					<%
						}				
					%>
				</ul>

				<%
					}else {
				%>
					<p style="color:red"> <i>Error: Cannot establish connection with repository!</i></p>
				<% 		
					}
				%>

			</article>
<script>
function submitPE(link){
	
	window.location=link;
}
</script>
				
				
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
