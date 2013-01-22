<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Cost</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Cost List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Cost</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Cost</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${costInstance}">
            <div class="errors">
                <g:renderErrors bean="${costInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${costInstance?.id}" />
                <input type="hidden" name="version" value="${costInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="costAmount">Cost Amount:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:costInstance,field:'costAmount','errors')}">
                                    <input type="text" id="costAmount" name="costAmount" value="${fieldValue(bean:costInstance,field:'costAmount')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="costDenominator">Cost Denominator:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:costInstance,field:'costDenominator','errors')}">
                                    <input type="text" id="costDenominator" name="costDenominator" value="${fieldValue(bean:costInstance,field:'costDenominator')}"/>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
