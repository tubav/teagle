<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*, java.util.List, de.fhg.fokus.teaglewebsite.*, teagle.vct.model.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="" />
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
		<h2>Create a new account</h2>
			<p>
			This functionality is disabled. The <a href="http://www.panlab.net/">PanlabII Project</a> has finished. For the further development of the FITeagle Framework please refer to <a href="http://fiteagle.org/">fiteagle.org</a>.
			</p>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>
