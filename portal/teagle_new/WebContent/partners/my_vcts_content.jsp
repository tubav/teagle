<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, teagle.vct.model.*, java.net.URLEncoder"  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

		<h2>VCT control page</h2>
<%
	String action = request.getParameter("action");
	int buttonbreite = 50;
	String user = request.getUserPrincipal().getName();
	List<? extends Vct> vctList;
	
	try {

//*********************************
//START - only display
//*********************************
		if(action == null){
%>
		<h4>VCTs from <%=user %></h4>
<%
			vctList = ModelManager.getInstance().findVctsByUserName(user);
%>	
		<table class="teagle" border="0">
			<colgroup>
				<col width="10"/>
				<col/>
				<col/>
				<col width="10"/>
			</colgroup>
<%
			for(Vct v : vctList){
				String userName = v.getPerson().getUserName();
				String vctCommonName = v.getCommonName();

				if (! userName.equals(user))
					continue;
%>
			<tr>
				<td colspan="2" bgcolor="#dddddd"><strong><%=v.getCommonName()%></strong></td>
<%
				String state = "n/a";
				String id = "";
				List<? extends ResourceInstance> components = null;
				try{
					Vct vct = ModelManager.getInstance().findVct(user, vctCommonName);
					components = v.getResourceInstances();
					state = vct.getState().name();
				}
				catch(Exception e){
					
				}
				String vct_bgcolor = "#dddddd";
				if("UNPROVISIONED".equals(state)){
					vct_bgcolor = "#ff7070";
				}
				else if("UNBOOKED".equals(state)){
					vct_bgcolor = "#ffff70";
				}
				else if("BOOKED".equals(state)){
					vct_bgcolor = "#70ff70";
				}
%>
				<td colspan="2" bgcolor="<%=vct_bgcolor %>" align="center"><%=state%></td>
				
	  		</tr>
<%
				if(components != null){
					try{
					
						Iterator<? extends ResourceInstance> it = components.iterator();
						String encodedResId;
						
						while(it.hasNext()){
							ResourceInstance c = it.next();
							//encodedResId = URLEncoder.encode(c.getId(), "UTF-8");
%>
			<tr>
				<td></td>
				<td bgcolor="#efefef"><%=c.getCommonName() %></td>
<%
							
							String cstate = "n/a";
							cstate = c.getState().name();
							String c_bgcolor = "#efefef";
							if("UNPROVISIONED".equals(cstate)){
								c_bgcolor = "#ffbebe";
							}
							else if("PROVISIONED".equals(cstate)){
								c_bgcolor = "#beffbe";
							}
							else if("NEW".equals(cstate)){
								c_bgcolor = "#ffffbe";
							}
%>
				<td bgcolor="<%=c_bgcolor %>" align="center"><%=cstate %></td>
			</tr>
<%  			
						}
					}
					catch(Exception e){	
					}
				}
				else{
%>
			<tr>
				<td></td>
				<td bgcolor="#efefef">definition is out of date</td>
				<td bgcolor="#efefef"></td>
				<td></td>
			</tr>
<%  					
				}
%>
			<tr>
				<td style="height:10px"></td>
			</tr>
<%		
			}
%>
		</table>
<%
		}
	}
	catch (Exception e) {
		e.printStackTrace();
%>
	<h3>Something went wrong!</h3>
	<p><input type="button" value="Back" style="width:<%=buttonbreite%>pt" onClick="history.go(-1);"/></p>
	<h3>Exception:</h3>
	<code><%=e.toString()%></code>
<%
 	}
%>