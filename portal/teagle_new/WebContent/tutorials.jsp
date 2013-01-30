<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="tutorials" />
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
			<h2>Tutorials</h2>
			<p>
			This page provides tutorials that show in several parts how to make use of Teagle and the Panlab offerings in general. This page will continuously be updated so please check back again later for more tutorials ...
			</p>
			<h4>Part 1: How to access the VCT Tool</h4>
			<p>
			<a href="http://www.fire-teagle.org/teagle/video/How_to_access_VCT_tool_1024x768_controller.swf">-&gt Start the tutorial ...</a>
			</p>
			<h4>Part 2: How to use the VCT Tool to design a simple custom VCT</h4>
			<p>
			<a href="http://www.fire-teagle.org/teagle/video/how_to_use_VCT_tool_final_controller.swf">-&gt Start the tutorial ...</a>
			</p>
			<h4>Part 3: How to use the VCT Tool to design complex custom VCTs</h4>
			<p>
			<a href="http://www.fire-teagle.org/teagle/video/How_use_Teagle_part3_final_3_controller.swf">-&gt Start the tutorial ...</a>
			</p>
			<h4>Live Tutorials</h4>
			<p>
			<a href="http://www.fire-teagle.org/teagle/fis-2010-tutorial.jsp">-&gt at FIS 2010 in Berlin ...</a><BR>
			<a href="http://www.panlab.net/events/training-barcelona-2010.html">-&gt at FIREweek 2010 in Barcelona ...</a>
			</p>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>
