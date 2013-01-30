

<%@page import="de.tub.av.pe.editor.PEEditor"%>
<%@page import="de.tub.av.pe.rule.utils.LoggingBean"%>
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>

<%
	String scopeName = request.getParameter("scope");	
	String id  = request.getParameter("id");
	String pageNr = request.getParameter("pageNr") == null?"0":request.getParameter("pageNr");
	editor.deleteData(scopeName, id);		
	out.print("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;URL=" + "index.jsp?navi=PolicyEditor&scope=" + scopeName + "&pageNr="+pageNr+"\">");
%>

	