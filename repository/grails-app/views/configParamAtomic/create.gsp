<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create ConfigParamAtomic</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">ConfigParamAtomic List</g:link></span>
        </div>
        <div class="body">
            <h1>Create ConfigParamAtomic</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${configParamAtomicInstance}">
            <div class="errors">
                <g:renderErrors bean="${configParamAtomicInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="commonName">Common Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:configParamAtomicInstance,field:'commonName','errors')}">
                                    <input type="text" id="commonName" name="commonName" value="${fieldValue(bean:configParamAtomicInstance,field:'commonName')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:configParamAtomicInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:configParamAtomicInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="defaultParamValue">Default Param Value:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:configParamAtomicInstance,field:'defaultParamValue','errors')}">
                                    <input type="text" id="defaultParamValue" name="defaultParamValue" value="${fieldValue(bean:configParamAtomicInstance,field:'defaultParamValue')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="configParamType">Config Param Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:configParamAtomicInstance,field:'configParamType','errors')}">
                                    <input type="text" id="configParamType" name="configParamType" value="${fieldValue(bean:configParamAtomicInstance,field:'configParamType')}"/>
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
