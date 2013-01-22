<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>PhysicalResource List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New PhysicalResource</g:link></span>
        </div>
        <div class="body">
            <h1>PhysicalResource List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="commonName" title="Common Name" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${physicalResourceInstanceList}" status="i" var="physicalResourceInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${physicalResourceInstance.id}">${fieldValue(bean:physicalResourceInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:physicalResourceInstance, field:'commonName')}</td>
                        
                            <td>${fieldValue(bean:physicalResourceInstance, field:'description')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${physicalResourceInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
