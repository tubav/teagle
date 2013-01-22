<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Geometry</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Geometry List</g:link></span>
        </div>
        <div class="body">
            <h1>Create Geometry</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${geometryInstance}">
            <div class="errors">
                <g:renderErrors bean="${geometryInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="w">W:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:geometryInstance,field:'w','errors')}">
                                    <input type="text" id="w" name="w" value="${fieldValue(bean:geometryInstance,field:'w')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="y">Y:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:geometryInstance,field:'y','errors')}">
                                    <input type="text" id="y" name="y" value="${fieldValue(bean:geometryInstance,field:'y')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="h">H:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:geometryInstance,field:'h','errors')}">
                                    <input type="text" id="h" name="h" value="${fieldValue(bean:geometryInstance,field:'h')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="x">X:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:geometryInstance,field:'x','errors')}">
                                    <input type="text" id="x" name="x" value="${fieldValue(bean:geometryInstance,field:'x')}" />
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
