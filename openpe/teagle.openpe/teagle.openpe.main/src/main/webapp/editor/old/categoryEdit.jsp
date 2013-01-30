<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
<%@page import="de.tub.av.pe.editor.ItemHtmlElementEnum"%>
<%@page import="de.tub.av.pe.editor.EditorDataItem"%>
<%@page import="de.tub.av.pe.editor.PEEditor"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>



<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>


<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Map"%>

<%@ page import="java.util.ArrayList" %>


	<script src="js/xmleditor/codemirror.js" type="text/javascript"></script>	
    <script type="text/javascript" src="jsonrpc/jsonrpcPE.js"> </script>
    <script src="js/ajaxEnablerFunctions.js" type="text/javascript"></script>	

<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>

<%
	
	String editorScope = request.getParameter("scope");
	if(editorScope == null)
		editorScope = "Groups Policies";
%>

 			
 			<article>				

				<h1><%=editorScope%> Editor</h1>
				
				<%				
					String id = request.getParameter("id");	
				%>
				
				<p></>Edit <%= editor.getEditPageTitle(editorScope, id)%></p>

				<%
					if(request.getParameter("error") != null)
					{
				%>
					<p style="color:red"><i></>Error Message: <%=request.getParameter("error")%></i></p><br/>	
				<% 		
					}
				%>			
				<form accept-charset="utf-8" style="background-color:white" name="editIdentityForm" method="post" action="index.jsp?navi=processCategoryEdit">
					<fieldset>
				<%
					List<EditorDataItem> lst = editor.getToEditObject(editorScope, id);
									
					for(EditorDataItem edi:lst)
					{
						if(edi.getHtmlElement() == ItemHtmlElementEnum.TEXTAREA)
						{
							
				%>
						<div style="border: 1px solid black; padding: 0px;">
							<textarea id="code" name='policy' cols="120" rows="60" style="display:none !important"><%= edi.getValue() instanceof String?(String)edi.getValue():""%></textarea>
						</div>
							
						<script type="text/javascript">
							  var editor = CodeMirror.fromTextArea('code', {
							    height: "350px",
							    width: "50em",
							    parserfile: "parsexml.js",
							    stylesheet: "css/xmleditor/xmlcolors.css",
							    path: "js/xmleditor/",
							    continuousScanning: 500,
							    lineNumbers: true,
							    textWrapping: false
							  });
						</script>
							
				<%
						}
						else if (edi.getHtmlElement() == ItemHtmlElementEnum.CHECKBOX)
						{
									
							if(edi.getValue() instanceof Map)
							{
								
								Map map = (Map)edi.getValue();
				%>
						<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" >
						<tbody style="font-size:8pt">
							<%			
									Iterator it = map.entrySet().iterator();	
										int i = 0;
										while(it.hasNext())
										{
											Entry<?, ?> entry = (Entry<?, ?>)it.next();
											String bgcolor = "#f0f0f0";
											if (entry.getValue().equals("checked"))
											{
							%>	
								<tr style="background-color:<%=bgcolor %>"><td><b><%= entry.getKey()%></b></td> <td><input type="checkbox" name="option<%=i %>" value="<%=entry.getKey() %>" checked/></td></tr>		
							<% 
												
											}
											else
											{
							%>
								<tr style="background-color:<%=bgcolor %>"><td><%= entry.getKey()%></td> <td><input type="checkbox" name="option<%=i %>" value="<%=entry.getKey() %>"/></td></tr>		
						
							<% 					
											}
											i++;
											
							%>
								<tr style="font-size:3pt"><td>&nbsp;</td></tr>
							<% 				
										}
							}
							%>
						</tbody>
						</table>
						
						
						
				<%		
				String pageNr = request.getParameter("pageNr")== null?"0":request.getParameter("pageNr");
				%>
							<input type="hidden" name="pageNr" value ="<%= pageNr%>"/>

				<% 	
						}else if (edi.getHtmlElement() == ItemHtmlElementEnum.SELECTWITHEVENT)
						{
							EditorDataItem triggeredEdi = edi.getTrigeredEditorItem();
							
				%>
							<select style="width:8em" id=<%=edi.getName()%> name="<%=edi.getName()%>" onchange="getAssociatedData(<%=triggeredEdi.getName()%>, this.value)">
								<% 
									Object value = edi.getValue();
									if(value instanceof List<?>)
									{
										List<?> list = (List<?>)value;
										for (int k = 0; k < list.size(); k++)
										{
											if(list.get(k) instanceof String)
											{
												String input = (String)list.get(k);
								%>
											<option <%=input.equals(edi.getOldInput())?"selected":"" %>><%=input %></option>

								<%			
											}
										}
									}
								%>	
							</select>
				<%			
						}else if (edi.getHtmlElement() == ItemHtmlElementEnum.SELECT)
						{
				%>
						<select style="width:8em" id=<%=edi.getName()%> name="<%=edi.getName()%>">
							<% 
								Object value = edi.getValue();
								if(value instanceof List<?>)
								{
									List<?> list = (List<?>)value;
									for (int k = 0; k < list.size(); k++)
									{
										if(list.get(k) instanceof String)
										{
											String input = (String)list.get(k);
							%>
									<option <%=input.equals(edi.getOldInput())?"selected":""%>><%=input%></option>
							<%
										}
									}
								}
							%>
						</select>
				<%									
						}else if(edi.getHtmlElement() == ItemHtmlElementEnum.INPUTWITHSUGGESTIONS)
						{
				%>			
						<div class="ui-widget">
							<input type="hidden" id="suggest-type" value="<%=edi.getName()%>"/>	
							<input id="suggest" value="<%=edi.getOldInput()!=null?edi.getOldInput():"" %>"/>
						</div>
							
				<% 									
						}
					}
				%>
				

							<input type="hidden" name="scope" value="<%= editorScope%>"/>
							<input type="hidden" name="id" value ="<%= id%>"/>
			</fieldset>
							<input type="submit"  class="submit" value="Save"/>							
								<input type="button" class="submit" onclick="history.go(-1)" value="Back"/>
						</form>													
				
				

			</article>
