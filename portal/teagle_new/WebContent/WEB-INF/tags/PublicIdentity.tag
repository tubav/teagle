<%--
public identity tag 
@deprecated
--%>
<%@tag description="showing the public identity row" pageEncoding="UTF-8"%>

<%@attribute name="number" required="true" %>
<%@attribute name="identity" required="true"%>
<tr class='header'><td>public id # " + ${number} + "</td><td>" + ${identity} + "</td></tr>
