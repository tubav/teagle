
<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<%@page import="de.tub.av.pe.editor.PEEditor"%>
<%@page import="java.util.ArrayList"%>
<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>

<%
String enablerName = request.getParameter("EnablerMethod");
//ArrayList<String>  list = PolicyDataObject.;	

//for (int i =0; i < list.size(); i++)
//	out.print(list.get(i)+",");	
%>