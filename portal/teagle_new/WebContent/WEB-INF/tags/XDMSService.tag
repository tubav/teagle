<%-- XDMS Service tag --%>
<%@tag description="showing the XDMS service row" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>

<%@attribute name="serviceIfcName" required="true" %>
<%@attribute name="serviceName" required="true"%>
<%@attribute name="serviceManagementTask" required="true"%>
<%@attribute name="serviceDescription" required="true"%>
<%@attribute name="serviceProvisioned" required="true"%>

<tr>
<td>

<html:multibox property="selectedServices">
	${serviceIfcName} 
</html:multibox>
</td>
<td>${serviceName}</td><td>Description: ${serviceDescription} : ${serviceProvisioned }</td>
</tr>
<!-- 
<tr><td>&nbsp;</td><td>Service management task:</td><td> ${serviceManagementTask}</td></tr>
 -->
<tr><td colspan="3"><hr width="100%" /></td></tr>
