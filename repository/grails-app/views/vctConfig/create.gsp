<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create VctConfig</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">VctConfig List</g:link></span>
        </div>
        <div class="body">
            <h1>Create VctConfig</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${vctConfigInstance}">
            <div class="errors">
                <g:renderErrors bean="${vctConfigInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="ptmVariant">Ptm Variant:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:vctConfigInstance,field:'ptmVariant','errors')}">
                                    <input type="text" id="ptmVariant" name="ptmVariant" value="${fieldValue(bean:vctConfigInstance,field:'ptmVariant')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="libraryCodebase">Library Codebase:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:vctConfigInstance,field:'libraryCodebase','errors')}">
                                    <input type="text" id="libraryCodebase" name="libraryCodebase" value="${fieldValue(bean:vctConfigInstance,field:'libraryCodebase')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="user">User:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:vctConfigInstance,field:'user','errors')}">
                                    <g:select optionKey="id" from="${Person.list()}" name="user.id" value="${vctConfigInstance?.user?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="ptmDelay">Ptm Delay:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:vctConfigInstance,field:'ptmDelay','errors')}">
                                    <input type="text" id="ptmDelay" name="ptmDelay" value="${fieldValue(bean:vctConfigInstance,field:'ptmDelay')}" />
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
