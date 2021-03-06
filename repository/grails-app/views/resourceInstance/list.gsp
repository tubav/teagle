<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>ResourceInstance List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New ResourceInstance</g:link></span>
        </div>
        <div class="body">
            <h1>ResourceInstance List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="commonName" title="Common Name" />
                        
                   	        <th>Geometry</th>
                   	    
                   	        <g:sortableColumn property="shared" title="Shared" />
                        
                   	        <th>Resource Spec</th>
                   	    
                   	        <g:sortableColumn property="description" title="Description" />

                                <g:sortableColumn property="state" title="State" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${resourceInstanceInstanceList}" status="i" var="resourceInstanceInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${resourceInstanceInstance.id}">${fieldValue(bean:resourceInstanceInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:resourceInstanceInstance, field:'commonName')}</td>
                        
                            <td>${fieldValue(bean:resourceInstanceInstance, field:'geometry')}</td>
                        
                            <td>${fieldValue(bean:resourceInstanceInstance, field:'shared')}</td>
                        
                            <td>${fieldValue(bean:resourceInstanceInstance, field:'resourceSpec')}</td>
                        
                            <td>${fieldValue(bean:resourceInstanceInstance, field:'description')}</td>

                            <td>${fieldValue(bean:resourceInstanceInstance, field:'state.commonName')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${resourceInstanceInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
