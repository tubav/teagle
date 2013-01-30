
<%@page import="de.tu.av.teagle.openpe.editor.utils.EditorUtils"%>
<%@page import="gen.openpe.elements.policy.OutputOverviews"%>
<%@page import="gen.openpe.elements.policy.OutputOverview"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Map"%>
<%@page import="gen.openpe.elements.policy.EvaluationOutput"%>
<%@page import="de.fhg.fokus.teaglewebsite.policyeditor.TeaglePolicyEditorViewer"%>
<%@page import="gen.openpe.identifiers.policy.PolicyIdentifier"%>
<%@page import="gen.openpe.elements.policy.RuleEvaluationOutput"%><jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../" />
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_policies_enforcement" />
	<jsp:param name="depth" value="../" />
</jsp:include>

<jsp:useBean
	id="policyViewer"
	class="de.fhg.fokus.teaglewebsite.policyeditor.TeaglePolicyEditorViewer"
	scope="session"/>

	
<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
<div id="col3_innen" class="floatbox">


<%
	String user = request.getUserPrincipal().getName();
	policyViewer.setServletConfig(config);	
	policyViewer.setUserFilter(null);
	String action = request.getParameter("action");
	int buttonbreite = 50;
	HashMap<Long, OutputOverview> ooMap = null;
	if(request.isUserInRole("authAdmin"))
		ooMap = policyViewer.loadNewOutputOverviews(null);
	else
		ooMap = policyViewer.loadNewOutputOverviews(user);		
	if(action == null)
	{
%>
		<h2>policy enforcement overview</h2>
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
			<tr>
				<th bgcolor="#dddddd">id</th>							
				<th bgcolor="#dddddd">decision</th>				
				<th bgcolor="#dddddd">originator</th>
				<th bgcolor="#dddddd">target</th>
				<th bgcolor="#dddddd">operation</th>
				<th bgcolor="#dddddd">time</th>
		     </tr>
		</thead>	
		

<%
	 	int i = 0;
		Iterator<Map.Entry<Long, OutputOverview>> it = ooMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<Long, OutputOverview> entry = it.next();
			OutputOverview oo = entry.getValue();
			Long key = entry.getKey();
			String color = "green";
			i++;
			if(oo.getDecision().equalsIgnoreCase("denied"))
				color = "red";
%>
		<tr>
			<td bgcolor="#efefef"><%=i%></td>
			<td bgcolor="#efefef" ><a href="?action=reason&key=<%=key%>" style="color:<%=color%>"><%=oo.getDecision() %></a></td>
			<td bgcolor="#efefef"><%=oo.getOriginatortype()%>:<%=oo.getOriginator()%></td>
			<td bgcolor="#efefef"><%=oo.getTargettype()%>:<%=oo.getTarget() %></td>
			<td bgcolor="#efefef"><%=oo.getEvent() %></td>					
			<td bgcolor="#efefef"><%=oo.getTime()%></td>					
		</tr>
<%		
		}
%>
	</table>
	</div>
<%	
	}else if(action.equals("reason") || action.equals("polreason"))
	{
		HashMap<Long, OutputOverview> hm = policyViewer.getCurrentOutputOverview();
		String key = request.getParameter("key");
		if(key != null)
		{
			OutputOverview oo = hm.get(Long.parseLong(key));
%>
		<h2>associated policies overview</h2>
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
				<tr>
					<th bgcolor="#dddddd">id</th>
					<th bgcolor="#dddddd">type</th>				
					<th bgcolor="#dddddd">identity</th>				
					<th bgcolor="#dddddd">scope</th>
					<th bgcolor="#dddddd">operation</th>
					<th bgcolor="#dddddd">reason</th>
			     </tr>
			</thead>	
<%
			List<EvaluationOutput> evalOList =  oo.getEvaluationOutput();

			int id = -1; 
			PolicyIdentifier selectedPI = null;
			EvaluationOutput selectedEO = null;
			if (action.equals("polreason"))
			{
				String idS = request.getParameter("id");
				id = idS!= null?Integer.parseInt(idS):-1;
			}

				for (int i = 0; i < evalOList.size(); i++)
			 {
			  	 EvaluationOutput eo = evalOList.get(i);
				 PolicyIdentifier pi = TeaglePolicyEditorViewer.toPolicyIdentifier(eo);
				 if(id == i)
				 {
					 selectedPI = pi;
					 selectedEO = eo;
				}
%>	
				<tr>
					<td bgcolor="#efefef"><a href="?action=polreason&key=<%=key%>&id=<%=i%>"> <%=pi.getId()%></a></td>
					<td  bgcolor="#efefef"><%=pi.getIdType()%></td>
					<td  bgcolor="#efefef"><strong><%=pi.getIdentity()%></strong></td>
					<td  bgcolor="#efefef"><%=pi.getScope()%></td>
					<td  bgcolor="#efefef"><%=pi.getEvent()%></td>
					<td  bgcolor="#efefef"><%=eo.getReason()%></td>
				</tr>						
<%
			 }			
%>
		</table>
		</div>		
<% 
			if(selectedPI != null && selectedEO != null)
			{
				String policyContent = EditorUtils.toDrl(policyViewer.getPolicyDBClient().getPolicyContent(selectedPI));
				List<RuleEvaluationOutput> rules = selectedEO.getRuleEvaluationOutput();

%>
		<h2>content policy <%=selectedPI.getId()%></h2>
<%
			if(rules.size() != 0)
			{
%>		
		
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
				<tr>
					<th bgcolor="#dddddd">Rule</th>
					<th bgcolor="#dddddd">Reason</th>
				</tr>
			</thead>
<% 
			for (RuleEvaluationOutput rule:rules)
			{
%>			
			<tr>
				<td bgcolor="#efefef"><%=rule.getRuleId()%></td>
				<td bgcolor="#efefef"><%=rule.getReason()%></td>	
			</tr>
<%
			}			
%>				
		</table>
		</div>	
<%
			}
%>			
		<form><textarea cols="70" rows="4" style="height:400px" readonly><%=policyContent %></textarea></form>
		<br/>
<%			
		   }
		}else 
		{
%>
	<p><i>No information available.</i></p>
<%			
		}
%>
	<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="window.location='policy_enforcement.jsp'" /></p>
<%
	}
%>

</div>
</div>
<div class="clear">&nbsp;</div>
</div>

<!-- end: center column -->

<%@ include file="../fragments/footer.jsp"%>
