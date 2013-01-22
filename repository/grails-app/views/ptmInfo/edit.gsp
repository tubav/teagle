<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit PtmInfo</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">PtmInfo List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PtmInfo</g:link></span>
        </div>
        <div class="body">
            <h1>Edit PtmInfo</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${ptmInfoInstance}">
            <div class="errors">
                <g:renderErrors bean="${ptmInfoInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${ptmInfoInstance?.id}" />
                <input type="hidden" name="version" value="${ptmInfoInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="commonName">Common Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:ptmInfoInstance,field:'commonName','errors')}">
                                    <input type="text" id="commonName" name="commonName" value="${fieldValue(bean:ptmInfoInstance,field:'commonName')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:ptmInfoInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:ptmInfoInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="resourceSpecs">Resource Specs:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:ptmInfoInstance,field:'resourceSpecs','errors')}">
                                    <g:select name="resourceSpecs"
from="${ResourceSpec.list()}"
size="5" multiple="yes" optionKey="id"
value="${ptmInfoInstance?.resourceSpecs}" />

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