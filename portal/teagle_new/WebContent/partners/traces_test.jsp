<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, de.fhg.fokus.tracingsupport.PTMTracesSupport, java.net.URLEncoder, org.apache.jasper.JspCompilationContext" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="../fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<jsp:include page="../fragments/nav.jsp">
	<jsp:param name="current" value="vct_control_my_vcts" />
	<jsp:param name="depth" value="../"/>
</jsp:include>

<script type="text/javascript" src="../js/jquery-1.3.2.js"></script>
<script type="text/javascript" src="../js/urlEncode.js"></script>

<script>
			function tccControl(id, act){
				
				if(act=="deploy"){
					$('#deploy_tcc_link').html('<img src="../images/ajax-loader.gif"/>');
				}
				
				$.ajax({
				       type: "GET",
				       url: "../TccControl",
				       data: "resourceId=" + $.URLEncode(id) + "&action=" + $.URLEncode(act),
				       success: function(msg){
						 			location.reload(true);
//								  	if(act=="deploy"){
								  		
//								  	}
				                 // alert(msg);
				                }
				  });

				 return false;
			}

			var newwindow;
			function showPopup(id, height, width){
				var l = (screen.width/2)-(width/2);
				var t = (screen.height/2)-(height/2);
				newwindow=window.open(id,'name','height='+height+',width='+width+',left='+l+',top='+t);
				if (window.focus) {newwindow.focus()}
			}	

</script>


<script type="text/javascript">

	$(document).ready(function(){
	
		$(".btn-slide").click(function(){
			$("#panel").slideToggle("slow");
			$(this).toggleClass("active"); return false;
		});

	    $('a.new-window').click(function(){
	        window.open(this.href);
	        return false;
	    });
});

</script>

<script type="text/javascript">

function checkStatus(){
	$(".status").each( function(){
		var div = this;
		$.ajax({
	          type: "GET",
	          url: "../StatusReportTransporter",
	          data: "resourceId=" + $.URLEncode(this.id),
	          success: function(msg){
			  				$(div).html(msg);
	                   }
	     });
		});
    return false;
}	

setInterval( "checkStatus()", 2000 );

</script>


<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">

	<h2>Traffic tracing control page</h2>
<%
	String action = request.getParameter("action");
	String resourceId = request.getParameter("resourceId");
	String encodedResId = URLEncoder.encode(resourceId, "UTF-8");
	
	List<String> tccList = PTMTracesSupport.getTccList(resourceId);
	//List<String> tccList = new ArrayList<String>();
	//tccList.add("//xfokus/pnode_0/tracescollectionclient_g41Z8NKmEQInlTKR");
	//tccList.add("//xfokus/pnode_0/tracescollectionclient_Hhkp8nhxgRj1Ghz4");
	
	pageContext.setAttribute("tccList", tccList);
	pageContext.setAttribute("resourceId", resourceId);
	
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	
	try {

//*********************************
//START - only display
//*********************************
		
%>
		<h4>Resource: <%=resourceId %></h4>
		
<%		if(resourceId==null){
%>	
		<h4>RESOURCE ID IS NOT FOUND</h4>
<%
		}
%> 
				<p><a id="deploy_tcc_link" href="#" OnClick="tccControl('<c:out value="${resourceId}"/>', 'deploy');" >New</a></p>
		<table class="teagle" border="0">
			
			
			<tr>
				<td colspan="2" bgcolor="#dddddd"><strong>Traces collection clients</strong></td>
				<td colspan="7" bgcolor="#dddddd" align="center">Actions</td>
	  		</tr>
	  		
  			<c:forEach var="tcc" items='${tccList}'>
  			
				<tr>
					<td></td>
					<td bgcolor="#efefef">
						<c:out value='${tcc}'/>
					</td>
<%
		String c_bgcolor = "#efefef";
%>
					<td>
						<a href="#" OnClick="tccControl('<c:out value="${tcc}"/>', 'start');" >
							Start 
						</a>
					</td>
					<td>
						<a href="#" OnClick="tccControl('<c:out value="${tcc}"/>', 'stop');" >
							Stop 
						</a>
					</td>
					<td>
						<a href="javascript:showPopup('
							<c:url value='tcc_config.jsp'>
								<c:param name='resourceId' value='${tcc}'/> 
								<c:param name='action' value='configure'/> 
							</c:url>', 400, 400);">
							Configure 
						</a>
					</td>
					<!-- LOAD CONTENT THROUGH JS ON CLICK -->
					<!-- USE PROPERTIES FILE FOR THE CONFIGURATION OR AN IP OF A TRACES SERVER FROM A USER'S VCT  -->
					<td>
						<a href="javascript:showPopup('
							<c:url value='http://10.147.65.205:8080/TracesCollectionServer/TracesExposer'>
								<c:param name='resourceId' value='${tcc}'/> 
							</c:url>',400,600);">
							Stored traces 
						</a>
					</td>
				<!-- 	
					<td>
						<a href="javascript:showPopup('
							<c:url value='http://10.147.65.205:8080/TracesCollectionServer/StatusReporter'>
								<c:param name='resourceId' value='${tcc}'/> 
							</c:url>');">
							Status
						</a>
					</td>
				-->
					<td>
						<div class="status" id='${tcc}'><img src="../images/ajax-loader.gif"/>
						</div>
					</td>
					<td>
						<a href="javascript:showPopup('
							<c:url value='tail.jsp'>
								<c:param name='resourceId' value='${tcc}'/> 
							</c:url>',600,980);">
							Tail
						</a>
					</td>
					<td>
						<a href="#" OnClick="tccControl('<c:out value="${tcc}"/>', 'delete');" >
							Delete 
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
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
