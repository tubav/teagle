<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Application</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Application List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Application</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Application</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${applicationInstance}">
            <div class="errors">
                <g:renderErrors bean="${applicationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${applicationInstance?.id}" />
                <input type="hidden" name="version" value="${applicationInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="commonName">Common Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'commonName','errors')}">
                                    <input type="text" id="commonName" name="commonName" value="${fieldValue(bean:applicationInstance,field:'commonName')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="provider">Provider:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'provider','errors')}">
                                    <g:select optionKey="id" from="${Organisation.list()}" name="provider.id" value="${applicationInstance?.provider?.id}" noSelection="['null':'']"></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="owner">Owner:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'owner','errors')}">
                                    <g:select optionKey="id" from="${Person.list()}" name="owner.id" value="${applicationInstance?.owner?.id}" noSelection="['null':'']"></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="configurationData">Configuration Data:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'configurationData','errors')}">
                                    <g:select name="configurationData"
from="${ConfigurationBase.list()}"
size="5" multiple="yes" optionKey="id"
value="${applicationInstance?.configurationData}" />

                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:applicationInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="containsApplicationComponents">Contains Application Components:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'containsApplicationComponents','errors')}">
                                    <g:select name="containsApplicationComponents"
from="${ApplicationComponent.list()}"
size="5" multiple="yes" optionKey="id"
value="${applicationInstance?.containsApplicationComponents}" />

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
