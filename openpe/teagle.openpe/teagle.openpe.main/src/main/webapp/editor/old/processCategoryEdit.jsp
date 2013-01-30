
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
	String scopeName = request.getParameter("scope");
	String id = request.getParameter("id");
	String pageNr = request.getParameter("pageNr") == null?"0":request.getParameter("pageNr");
	String message = "";


	boolean res = false;
	try{
		editor.saveEditedDataObject(scopeName, id, request.getParameterMap());
		message = "Data successfully uploaded ...";
		res = true;
	}catch (DuplicateValueException e)
	{
		message = e.getMessage();
		res = false;
	}
	catch (RepositoryException e)
	{
		message = "Error access repository";
		res = false;
	}
	catch (ValidationException e)
	{
		 message = e.getMessage();
		 res = false;		
	}
		
	if (res)
	{
		out.print("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;URL=" + "index.jsp?navi=PolicyEditor&scope=" + scopeName + "&pageNr="+pageNr+"\">");
%>

<%
	}else
	{
		out.print("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;URL=" + "index.jsp?navi=categoryEdit&scope=" + scopeName + "&error="+message+"\">");
%>
	
<% 		
	}
%>
