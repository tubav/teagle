<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Ptm</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Ptm List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Ptm</g:link></span>
        </div>
        <div class="body">
            <h1>Show Ptm</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:ptmInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Common Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:ptmInstance, field:'commonName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Provider:</td>
                            
                            <td valign="top" class="value"><g:link controller="organisation" action="show" id="${ptmInstance?.provider?.id}">${ptmInstance?.provider?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Owner:</td>
                            
                            <td valign="top" class="value"><g:link controller="person" action="show" id="${ptmInstance?.owner?.id}">${ptmInstance?.owner?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Configuration Data:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="c" in="${ptmInstance.configurationData}">
                                    <li><g:link controller="configurationBase" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:ptmInstance, field:'description')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Contains Application Components:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="c" in="${ptmInstance.containsApplicationComponents}">
                                    <li><g:link controller="applicationComponent" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Described By Ptm Info:</td>
                            
                            <td valign="top" class="value"><g:link controller="ptmInfo" action="show" id="${ptmInstance?.describedByPtmInfo?.id}">${ptmInstance?.describedByPtmInfo?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Url:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:ptmInstance, field:'url')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${ptmInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
