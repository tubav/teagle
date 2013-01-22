<!-- Generated by Grails v1.2.1 -->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Cost List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Cost</g:link></span>
        </div>
        <div class="body">
            <h1>Cost List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="costAmount" title="Cost Amount" />
                        
                   	        <g:sortableColumn property="costDenominator" title="Cost Denominator" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${costInstanceList}" status="i" var="costInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${costInstance.id}">${fieldValue(bean:costInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:costInstance, field:'costAmount')}</td>
                        
                            <td>${fieldValue(bean:costInstance, field:'costDenominator')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${costInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
