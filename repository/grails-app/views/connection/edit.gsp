<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Connection</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Connection List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Connection</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Connection</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${connectionInstance}">
            <div class="errors">
                <g:renderErrors bean="${connectionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${connectionInstance?.id}" />
                <input type="hidden" name="version" value="${connectionInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="connectionId">Connection Id:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:connectionInstance,field:'connectionId','errors')}">
                                    <input type="text" id="connectionId" name="connectionId" value="${fieldValue(bean:connectionInstance,field:'connectionId')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="destination">Destination:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:connectionInstance,field:'destination','errors')}">
                                    <g:select optionKey="id" from="${Dst.list()}" name="destination.id" value="${connectionInstance?.destination?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="rules">Rules:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:connectionInstance,field:'rules','errors')}">
                                    <input type="text" id="rules" name="rules" value="${fieldValue(bean:connectionInstance,field:'rules')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="source">Source:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:connectionInstance,field:'source','errors')}">
                                    <g:select optionKey="id" from="${Src.list()}" name="source.id" value="${connectionInstance?.source?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="type">Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:connectionInstance,field:'type','errors')}">
                                    <g:select  from="${ConnectionType?.values()}" value="${connectionInstance?.type}" name="type" ></g:select>
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
