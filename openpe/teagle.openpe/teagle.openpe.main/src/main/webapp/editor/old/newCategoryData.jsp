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

<%@page import="java.util.ArrayList"%>

<script src="js/xmleditor/codemirror.js" type="text/javascript"></script>	
    <script type="text/javascript" src="jsonrpc/jsonrpcPE.js"> </script>
    <script src="js/ajaxEnablerFunctions.js" type="text/javascript"></script>	
    <script type="text/javascript" charset="utf-8">
		function popitup(url) {
			newwindow=window.open(url,'name','height=700,width=600');
			if (window.focus) {newwindow.focus()}
			return false;
		}
		   		
	</script>
	    
	
<jsp:useBean id="editorBean" class="de.tub.av.pe.editor.drools.impl.PEEditorBean" scope="application" />

<%
editorBean.config(this.getServletContext(), OpenPEServlet.PE_CONTEXT_MANAGER);
PEEditor editor = editorBean.getEditor();
%>


<%

								String editorScope = request.getParameter("scope");
								if(editorScope == null)
									editorScope = "ProfilePolicy";
					%>
 			
 			<article>				

				<h1>Create new data of category <%=editorScope%> </h1>

				<%	
					String executionType = request.getParameter("executionType");	
				
					List<EditorDataItem>  newObject;
			
					if (executionType != null)
					{
						newObject = editor.getToSaveNewDataObject(editorScope);
						String errorMessage = request.getParameter("error");
				%>
					<p style="color:red"> <i>Error Message: <%=errorMessage%></i></p>
				<% 		
					}
					else 
						newObject = editor.getNewDataObject(editorScope);
				%>
				
								
				<form accept-charset="utf-8" style="background-color:white" method="post" action="index.jsp?navi=processNewData">
					<fieldset>
				<%	
				
					for (int i = 0; i < newObject.size(); i++)
					{
						EditorDataItem edi = newObject.get(i);
						if(edi.getHtmlElement() == ItemHtmlElementEnum.TEXT)
						{							
				%>
						<label for="<%=edi.getName()%>" title="Name"><%=edi.getName()%>:</label><input type="text" name="<%=edi.getName()%>" value="<%=edi.getValue() instanceof String?(String)edi.getValue():""%>"/>
				<%		
						}
						else if (edi.getHtmlElement() == ItemHtmlElementEnum.SELECTWITHEVENT)
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
						<input type="hidden" id="<%=edi.getName()%>" value="<%=edi.getOldInput()!=null?edi.getOldInput():"" %>"/>
						<div class="ui-widget">
							<input type="hidden" id="suggest_type" value="<%=edi.getName()%>"/>	
							<input type="text" id="suggestAC" value=""/>
						</div>
						
						<script type="text/javascript">
	
						  $(function() {
							  
							var jsonrpc = new JSONRpcClient("JSON-RPC");
							
							var result = jsonrpc.JSONMethods.getSuggestData($( "#suggest_type" ).val());
							var suggestions = result.list;  
							var boxname = $( "#suggest_type" ).val();
					
							$( "#suggestAC" ).autocomplete({
								source: suggestions,
								minLength: 2,
								change: function( event, ui ) {
						             $('#'+boxname ).val(this.value);						             
								}
							});
						});
						</script>
							
				<% 									
						}
						else if (edi.getHtmlElement() == ItemHtmlElementEnum.TEXTAREA)
						{
				%>	
						<input type="submit" value="View Schema" class="submit green" onclick="popitup('content/PE/editor/popupWindow.jsp');return false;"/>
						<div style="border: 1px solid black; padding: 0px;">
							<textarea id="code" name='policy' style="display:none !important" cols="120" rows="60"><%= edi.getValue()== null?"":edi.getValue()%></textarea>
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
					}
				
				%>
				</fieldset>
							<input type="hidden" name="editorScope" value="<%= editorScope%>"/>
						
							<input type="submit"  class="submit button" style="background-color:#f0f0f0" value="Save"/>
							
							<!--<input type="button" class="submit button" onclick="document.location='index.jsp?navi=PolicyEditor&scope=<=editorScope %>'" style="background-color:#f0f0f0" value="Back"/> -->
							<input type="button" class="submit button" onclick="history.go(-1);" style="background-color:#f0f0f0" value="Back"/>
						</form>													
				

			</article>
