<%--
deprecated denke ich
--%>
<%@tag description="showing the request results" pageEncoding="UTF-8"%>

<%@attribute name="source"%>
<%@attribute name="requestid"%>
<%@attribute name="errorcode"%>
<%@attribute name="status" required="true"%>
<table class="as">
    <tr class="odd">
        <td align="center">${source}:</td>
    </tr>
    <tr class="even">
        <td>request id: </td><td align="center">${requestid}</td>
    </tr>
    <tr class="odd">
        <td>status: </td><td align="center">${status}</td>
    </tr>
    <tr class="even">
        <td>error code: </td><td align="center">${errorcode}</td>
    </tr>
</table>
