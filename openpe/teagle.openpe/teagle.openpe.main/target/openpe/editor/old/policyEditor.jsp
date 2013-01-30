<%@page import="de.tub.av.pe.main.servlet.OpenPEServlet"%>
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
<%@page import="java.util.Map"%>

<%@ page import="java.util.ArrayList" %>
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

				<h1><%=editorScope%> Editor</h1>
				
				<p>View and Edit <%=editorScope%> data</p>
				
			<%
					int pagesize = 10;
				
					int pageNr = request.getParameter("pageNr") == null?0:Integer.parseInt(request.getParameter("pageNr"));
					String filterName = "";
					String filterValue = "";
					
					List<Map<String, String>> dataList = null; 
					
					if(request.getParameter("filterName")!=null)
					{
						filterName = request.getParameter("filterName");
						filterValue = request.getParameter("filterValue");
						dataList = editor.getDataList(editorScope, pageNr*pagesize, pagesize, filterName, filterValue);		
					}
					else
					{
						dataList = editor.getDataList(editorScope, pageNr*pagesize, pagesize);
					}
					
					if(dataList != null)
					{							
					 String[] columnNameList = editor.getDataKeys(editorScope);
			%>

			<form accept-charset="utf-8" style="background-color:white" method="post" name="t1" action="index.jsp?navi=PolicyEditor">
				<fieldset>
								 
								 <label for="filterName"> SelectBy:</label>
								 <select name="filterName"> 
						<%
							for (int i =0; i < columnNameList.length; i++ )
							{
								String columnName = columnNameList[i];
								if(!columnName.equals("policy") && !columnName.equals("id"))
								{
						%>		
								<option <%= filterName.equals(columnNameList[i])?"selected":""%>><%=columnNameList[i]%></option>					
						<%
								}				
							}
						%>		
								</select>
								<input type="text" name="filterValue" value="<%=filterValue %>" size="10"></input>
								<input type="hidden" name="scope" value="<%=editorScope %>"/>
								<input type="submit"  class="submit" value="GO"/>		
								</fieldset>			
						</form>
							<%if(!editorScope.equals("User")){ %>
						<form accept-charset="utf-8" style="background-color:white display:inline" name="Form" method="post" action="index.jsp?navi=newCategoryData">
							<fieldset>
							<input type="hidden" name="scope" value="<%=editorScope %>"/>
						
							<input type="submit"  class="submit" value="Create New <%=editorScope %>"/>		
							
							</fieldset>			
						</form>
<% } %>
				
				<form>
				<table style="table-layout:auto;border-spacing:3px" rules="groups" class="example" summary="This table contains web services" >
					<thead>
						<tr>
							
							<% 
								
								for (int i =0; i < columnNameList.length; i++ )
								{
									String columnName = columnNameList[i];
									if(!columnName.equals("policy") && !columnName.equals("id"))
									{
							%>
									<th><%=columnName%></th>		
							<%
									}
								}
							%>		
							<th style="text-align:right;">operations</th>				
						</tr>
					</thead>
					<tbody >					
						<%
							for (int i = 0 ; i < dataList.size(); i++)
							{
								/*CCF5CC*/
								String bgcolor = ((i % 2) != 0 ? "#f0f0f0" : "#dcdcdc");
						%>
							<tr style="background-color:#f0f0f0">
								
								<%
									Map<String, String> map = dataList.get(i);
	
									for (int k = 0; k < columnNameList.length; k++ )
									{
										if(!columnNameList[k].equals("policy") && !columnNameList[k].equals("id"))
										{
											String value = map.get(columnNameList[k]);								
								%>
									<td><%= value%></td>
								<%
										}
									}
								%>
								<td style="text-align:right;">
									<input class="submit green" type="submit" onclick="submitPE('index.jsp?navi=categoryEdit&scope=<%=editorScope%>&pageNr=<%=pageNr%>&id=<%= map.get("id")%>');return false;" value="Edit"> &nbsp;
									<input type="submit" class="submit red" onclick="submitPE('index.jsp?navi=categoryDelete&scope=<%=editorScope%>&pageNr=<%=pageNr%>&id=<%=map.get("id")%>');return false;" value="Delete" >
								</td>								
							</tr>
							<tr style="font-size:3pt">
								<td>&nbsp;</td>								
							</tr>
						<%									
							}
						%>
					</tbody>
				</table>
				</form>
				<ul style="list-style-image:none; list-style-type:none;padding: 0px;">
				<%		
				
					int resultDataSize = dataList.size();				
					 if(pageNr == 0)
						{
							if(pagesize < resultDataSize)
							{					
					%> 
								<li style="float:right">						
								     <a href="index.jsp?navi=PolicyEditor&scope=<%=editorScope%>&pageNr=<%=pageNr+1%> "+ title="forward" class="images"><img src="img/next.png"></img></a>					
								</li>
					<%
							}
						} else if (pageNr*pagesize+ pagesize >= resultDataSize)
						{				
					%>
								<li style="float:left">												
								    <a href="index.jsp?navi=PolicyEditor&scope=<%=editorScope%>&pageNr=<%=pageNr-1%> "+ title="back" class="images"><img src="img/back.png"></img></a>												
								 
								</li>						
									
					<%									
						}else{
					%>
							<li style="float:left">																			
							   <a href="index.jsp?navi=PolicyEditor&scope=<%=editorScope%>&pageNr=<%=pageNr-1%> "+ title="back" class="images"><img src="img/back.png"></img></a>
							</li>
							<li style="float:right">
							   <a href="index.jsp?navi=PolicyEditor&scope=<%=editorScope%>&pageNr=<%=pageNr+1%> "+ title="forward" class="images"><img src="img/next.png"></img></a>
							</li>
					<%
						}				
					%>
				</ul>

				<%
					}else {
				%>
					<p style="color:red"> <i>Error: Cannot establish connection with repository!</i></p>
				<% 		
					}
				%>

			</article>
<script>
function submitPE(link){
	
	window.location=link;
}
</script>