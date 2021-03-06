<!-- Generated by Grails v1.2.1 -->



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Booking List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Booking</g:link></span>
        </div>
        <div class="body">
            <h1>Booking List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="startDate" title="Start Date" />
                        
                   	        <th>Vct</th>
                   	    
                   	        <g:sortableColumn property="endDate" title="End Date" />
                        
                   	        <g:sortableColumn property="rated" title="Rated" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${bookingInstanceList}" status="i" var="bookingInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${bookingInstance.id}">${fieldValue(bean:bookingInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:bookingInstance, field:'startDate')}</td>
                        
                            <td>${fieldValue(bean:bookingInstance, field:'vct')}</td>
                        
                            <td>${fieldValue(bean:bookingInstance, field:'endDate')}</td>
                        
                            <td>${fieldValue(bean:bookingInstance, field:'rated')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${bookingInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
