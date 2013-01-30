<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@page import="de.tub.av.pe.ReasonOverview"%>
<%@page import="java.util.List"%>
<%@page import="de.tub.av.pe.RulesEvalOverview"%>
<%@page import="de.tub.av.pe.context.OpenPEContext"%>
<%@page import="de.tub.av.pe.LoggingBean"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
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
				background-color: rgb(153, 235, 153);  /* 40% */
			}
		.main a:hover {
			color: rgb(0,204,0);
			border-bottom: 1px solid rgb(0,204,0);*/
			/*color: rgb(200, 230, 30);*/
			border-bottom: 1px solid rgb(0,0,0);
		}
		.content h1, .content h2, .content h3, .content h4 {
			color: #736F6E;
		}
		.content a:hover{
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
	
	<script language="JavaScript">
		function getRuleID(selectObject, id, entryNr)
    	{
   			 var ruleId  = selectObject.selectedIndex;
     		 document.location = "reason.jsp?ruleId="+ruleId+"&id="+id+"&logentry="+entryNr;
    	}
	</script>

</head>

<%@ page import="java.util.ArrayList"%>
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
					<a href="loglist.jsp" title="Log Overview">Log Overview</a>
				</li>
				<li>
					<a href="editor/policyEditor.jsp" title="Editor">Policy Repository</a>
				</li>						
			</ul>
		</div>

		<div class="main">
			
			<div class="breadcrumb">
				<a href="index.html" title="Home">Home</a> > <a href="openpe.html" title="Open PE"><abbr title="Open Policy Engine">Open PE</abbr></a> > <a href="loglist.jsp" title="Open PE"><abbr title="Open PE Log Overview">Log Overview</abbr></a>
				> <abbr title="Open Policy Engine">Evaluation Decision Reason</abbr>
			</div>
 			
 			<div class="content">
 
 
<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
OpenPEContext openpecontext = editorBean.getOpenPEContext();
%> 			
 	<div class="content">
 				<h3> Evaluation Decision Reason Overview</h3>

 				
 <%
  	int id = -1;
 	RulesEvalOverview policyToPrint = null;
 	String policyName = null;
 	
 	String entryNr = request.getParameter("logentry"); 		

 	if(entryNr != null)
 	{ 		
 		LoggingBean logging = editorBean.getOpenPEContext().getLoggingBeanObject();

 		if (logging != null) 
 		{
 			List<LoggingBean.LogEntry> infos = logging.getLogEntries();
 			ReasonOverview reasonBean = infos.get(Integer.parseInt(entryNr) - 1).getReason();
 %>
 				<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" summary="This table contains web services" >
					<thead>
						<tr>
							<th>ID&nbsp;</th>
							<th>PolicyName</th>
							<th>Reason</th>
						</tr>
					</thead>
					<tbody style="font-size:8pt">
 	
 <%	
 		
		 		if(request.getParameter("id") != null )
		 			id = Integer.parseInt(request.getParameter("id"));
					 		
	 		List<ReasonOverview.ReasonEntry> reasonEntryList = reasonBean.getReasonEntries();
	 		System.out.println("!!!!!!!!!!!number of entries :"+reasonEntryList.size());
	 		for(int i = 0; i < reasonEntryList.size(); i++)
	 		{
	 			ReasonOverview.ReasonEntry reasonEntry = reasonEntryList.get(i);
	 			if(i == id -1)
	 			{
	 				policyToPrint = reasonEntry.getPolicy();
	 				policyName = reasonEntry.getPolicyName();
	 			}
				
	 			String bgcolor = ((i % 2) != 0 ? "#f0f0f0" : "#dcdcdc");
 %>
 						<tr style="background-color:<%=bgcolor %>">
							<td>
								 <%=i+1 %>
							</td>
							<td>
								<a href="#" title="Reason" onclick="showRule('<%=i+1 %>','<%=entryNr %>')"><%=reasonEntry.getPolicyName()%> </a>
							</td>
							<td>
								<%=reasonEntry.getReason() %>
							</td>
						</tr>
<%
			}
 		}
%>		
					</tbody>
				</table>
				
<%
		if(policyToPrint != null)
		{	
			int rule_nr = -1;
			String ruleContent = null;
			String ruleId = null;
			String ruleReason = null;
			String ruleActionsExecLog = null;
			
			if(request.getParameter("ruleId") != null)
			{
				rule_nr = Integer.parseInt(request.getParameter("ruleId"));	
			}
			List<RulesEvalOverview.RuleEntry> rules = policyToPrint.getRuleEntries();
%>			
			<br/><br/>
			Policy <i><%=policyName %></i>:<br/>
			<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" summary="This table contains web services" >
				<thead>
					<tr>
						<th>Rule ID</th>
						<th>Reason</th>
					</tr>
				</thead>
				<tbody style="font-size:8pt">
					<tr style="background-color:#f0f0f0">
						<td>						
							<select name="rules" onchange="getRuleID(this, <%=id%>, <%=entryNr%>)">
								<option value="-1"/>
<%			for(int j = 0; j < rules.size(); j++)
			{ 
				if(j == rule_nr - 1)
				{
					ruleContent = rules.get(j).getRuleContent();
					ruleId = rules.get(j).getRuleId();
					ruleReason = rules.get(j).getReason();
					ruleActionsExecLog = rules.get(j).getActionsExecLog();
%>					
								<option value="<%=j %>" selected>Rule <%=rules.get(j).getRuleId() %></option>
<% 
				}
				else
				{
%>		
								<option value="<%=j %>">Rule <%=rules.get(j).getRuleId() %></option>
<%				} 
	
			}		
%>
							</select>&nbsp;
						</td>
						<td>
<% 
			if(ruleReason != null)
				out.print(ruleReason);
%>
						</td>
					</tr>
				</tbody>
			</table>
					
<%			
			if(ruleContent != null)
			{
				if(ruleActionsExecLog != null)
				{
					
%>	
					<br/>
					<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" summary="This table contains web services" >
						<thead>
							<tr>
								<th>Rule <i><%=ruleId %></i> actions:</th>
							</tr>
						</thead>
						<tbody style="font-size:8pt">
							<tr style="background-color:#f0f0f0">
								<td>
									<%=ruleActionsExecLog%>
								</td>
							</tr>
						</tbody>
					</table>
<%
			
				}
%>				
				<br/>
				<dt>Rule <i><%=ruleId %></i> listing:</dt>
				<textarea cols="70%" rows="20" readonly style="font-size:8pt" ><%=ruleContent %></textarea>	
<%				
			}			
		}	
	}
%>	
			<p style="color:white">Hidden Text</p>
			
	</div>
 
 
 
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
