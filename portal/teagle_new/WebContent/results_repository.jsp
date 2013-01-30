<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="results_repository" />
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
			<h2>Results Repository</h2>
			<p>
			Here Panlab Users may publish their test results for other users to compare their results with.
			</p>
			<p>
			This area is not yet operational. Please check later ...
			</p>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>