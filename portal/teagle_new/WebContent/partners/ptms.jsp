<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, teagle.vct.model.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_ptms" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	String action = request.getParameter("action");
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	List<? extends Ptm> ptmList;
		
	try {
		ptmList = ModelManager.getInstance().listPtms();
		
//*********************************
//START - only display
//*********************************
		if(action == null){
%>
		<h2>registered PTMs</h2>

		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
	    		<tr>
	    			<th bgcolor="#dddddd" style="height:25px">name</th>
					<th bgcolor="#dddddd">userName</th>
					<th bgcolor="#dddddd">organization</th>
				</tr>
			</thead>
<%
			for (Ptm p: ptmList) {
%>
			<tr>
				<td bgcolor="#efefef"><%= p.getCommonName()%></td>
	  			<td bgcolor="#efefef"><%= p.getPerson().getUserName()%></td>
	  			<td bgcolor="#efefef"><strong><%= p.getOrganisation().getName()%></strong></td>
	  		</tr>
<%
			}
%>
		</table>
		</div>
		<br>
<%
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
	<h3>Something went wrong!</h3>
	<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
	<h3>Exception:</h3>
	<code><%=e.toString()%></code>
<%
 	}
%>

		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>
