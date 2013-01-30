
<%@page import="de.tub.av.pe.db.ValidationException"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<%@page import="de.tub.av.pe.editor.RepositoryException"%>
<%@page import="de.tub.av.pe.context.DuplicateValueException"%>
<%@page import="de.tub.av.pe.editor.PEEditor"%>
<%@page import="javax.xml.bind.JAXBException"%>
<%@page import="java.util.Enumeration"%>

<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>

<%
%>
<%
	String scopeName = request.getParameter("editorScope");
	System.out.println(scopeName);	
	String message = "";
	boolean res = true;
	try{
		editor.saveNewData(scopeName, request.getParameterMap());
	}catch(DuplicateValueException e)
	{
		res = false;
		message = e.getMessage();
	}catch(RepositoryException e)
	{
		res = false;
		message = e.getMessage();		
	}catch(ValidationException e)
	{
		res = false;
		message = e.getMessage();
	}
	
	if (res)
	{
		out.print("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;URL=" + "index.jsp?navi=PolicyEditor&scope=" + scopeName +"\">");


	}else
	{
		out.print("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;URL=" + "index.jsp?navi=newCategoryData&scope=" + scopeName + "&executionType=save&error="+message+"\">");

	}
%>
