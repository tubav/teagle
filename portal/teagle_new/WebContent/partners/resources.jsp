<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List, teagle.vct.model.*"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_resources" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
<%
	int buttonbreite = 50;
	List<? extends ResourceSpec> resourceSpecList;
		
	try {
%>
		<h2>registered Resources</h2>
<%
		resourceSpecList = ModelManager.getInstance().listResourceSpecs();
%>
		<div style="max-width:100%; overflow:auto">
		<table class="teagle" border="0">
			<thead>
	    		<tr>
					<th bgcolor="#dddddd"  style="height:25px">provider</th>
					<th bgcolor="#dddddd">type</th>
					<th bgcolor="#dddddd">description</th>
					<th bgcolor="#dddddd">price</th>
				</tr>
			</thead>
<%
		for (ResourceSpec r: resourceSpecList) {
%>
			<tr>
	  			<td bgcolor="#efefef"><strong><%=r.getProvider()%></strong></td>
	  			<td bgcolor="#efefef"><%= r.getType()%></td>
	  			<td bgcolor="#efefef"><%= r.getDescription()%></td>
	  			<td bgcolor="#efefef"><%= r.getPrice()%></td>
	  		</tr>
<%
		}
%>
		</table>
		</div>
		<br>
<%
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
