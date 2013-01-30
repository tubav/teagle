<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_my_vcts" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<script type="text/javascript" src="../js/jquery-1.3.2.js"></script>
<script type="text/javascript">
	$(document).ready(function(){
		$("#col3_content").load("my_vcts_content_trace.jsp");
	});
</script>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
			<h2>VCT control page</h2>
			<h4>VCTs from <%=request.getUserPrincipal().getName() %></h4>
			<p>
			<br>
			<img src="../images/ajax-loader.gif">
			</p>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="../fragments/footer.jsp" %>
