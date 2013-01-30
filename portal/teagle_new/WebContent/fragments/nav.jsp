<%
	String depth;
	if(request.getParameter("depth") == null){
		depth = "";
	}
	else{
		depth = request.getParameter("depth");
	}
	String current = request.getParameter("current");
	
%>

<!-- #col1: Erste Float-Spalte des Inhaltsbereiches -->
<div id="col1"><!--###col1### begin -->
	<div id="col1_content" class="clearfix">
		<h3><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Teagle</h3>
		<div class="mainMenu">
			<ul>
				<li><%if("home".equals(current)){%><strong><dfn>1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="">Home</strong><%} else{%><dfn>1: </dfn><a href="<%=depth%>."><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Home</a><%}%><span class="hidden">.</span></li>
				<li><%if("news".equals(current)){%><strong><dfn>2: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="">News</strong><%} else{%><dfn>2: </dfn><a href="<%=depth%>news.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >News</a><%}%><span class="hidden">.</span>
<%
	if(request.isUserInRole("authAdmin")){
%>
				<ul>
					<li><%if("edit_news".equals(current)){%><strong><dfn>2.1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" />Edit News</strong><%} else{%><dfn>2.1: </dfn><a href="<%=depth%>admins/edit_news.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Edit News</a><%}%><span class="hidden">.</span>
				</ul>
<%
	}
%>
				</li>
				<li><%if("tutorials".equals(current)){%><strong><dfn>5: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Tutorials</strong><%} else{%><dfn>5: </dfn><a href="<%=depth%>tutorials.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Tutorials</a><%}%><span class="hidden">.</span></li>
				<li><%if("members".equals(current)){%><strong><dfn>6: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Members Area</strong><%} else{%><dfn>6: </dfn><a href="<%=depth%>members/secret.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Members Area</a><%}%><span class="hidden">.</span>
<%
	//if(name!=null && !"admin".equals(name) && !"tomcat".equals(name) && !"both".equals(name)){
	if(request.isUserInRole("authUser")){
%>
				<ul>
					<li><%if("edit_account".equals(current)){%><strong><dfn>6.1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Edit Account</strong><%} else{%><dfn>6.1: </dfn><a href="<%=depth%>members/edit_account.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Edit Account</a><%}%><span class="hidden">.</span></li>
<%
		if(request.isUserInRole("authAdmin")){//provisioning and sms only shown to admins for now, but theoretically accessible for normal users
%>
					<li><%if("user_administration".equals(current)){%><strong><dfn>6.2: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >User Administration</strong><%} else{%><dfn>6.2: </dfn><a href="<%=depth%>admins/user_administration.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >User Administration</a><%}%><span class="hidden">.</span></li>
<%
		}
%>
					<li><%if("vct".equals(current)){%><strong><dfn>6.5: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >VCT design</strong><%} else{%><dfn>6.5: </dfn><a href="<%=depth%>members/vct.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >VCT design</a><%}%><span class="hidden">.</span></li>
<%
		if(request.isUserInRole("authPartner")){
%>
					<li><%if("vct_control".equals(current)){%><strong><dfn>6.6: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >VCT control</strong><%} else{%><dfn>6.6: </dfn><a href="<%=depth%>partners/vct_control.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >VCT control</a><%}%><span class="hidden">.</span>
<%
			if(current.contains("vct_control")){
%>
					<ul>
						<li><%if("vct_control_my_vcts".equals(current)){%><strong><dfn>6.6.1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >VCTs</strong><%} else{%><dfn>6.6.1: </dfn><a href="<%=depth%>partners/my_vcts.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >VCTs</a><%}%><span class="hidden">.</span></li>
						<li><%if("vct_control_resources".equals(current)){%><strong><dfn>6.6.2: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Resources</strong><%} else{%><dfn>6.6.2: </dfn><a href="<%=depth%>partners/resources.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Resources</a><%}%><span class="hidden">.</span>
<%
				if(current.contains("resources")){
%>		
	 					<ul>
	 						<li><%if("vct_control_my_resources".equals(current)){%><strong><dfn>6.6.2.1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >My Resources</strong><%} else{%><dfn>6.6.2.1: </dfn><a href="<%=depth%>partners/my_resources.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >My Resources</a><%}%><span class="hidden">.</span></li>
 							<li><%if("vct_control_resources_reg".equals(current)){%><strong><dfn>6.6.2.2: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Resource Registration</strong><%} else{%><dfn>6.6.2.2: </dfn><a href="<%=depth%>partners/resource_registration.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Resource Registration</a><%}%><span class="hidden">.</span></li>
 						</ul>
<%
				}
%>
						</li>
						<li><%if("vct_control_ptms".equals(current)){%><strong><dfn>6.6.3: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >PTMs</strong><%} else{%><dfn>6.6.3: </dfn><a href="<%=depth%>partners/ptms.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >PTMs</a><%}%><span class="hidden">.</span>
<%
				if(current.contains("ptm")){
%>		
	 					<ul>
	 						<li><%if("vct_control_ptm_organizations".equals(current)){%><strong><dfn>6.6.3.1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Organizations</strong><%} else{%><dfn>6.6.3.1: </dfn><a href="<%=depth%>partners/ptm_organizations.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Organizations</a><%}%><span class="hidden">.</span></li>
	 						<li><%if("vct_control_my_ptms".equals(current)){%><strong><dfn>6.6.3.2: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >My PTMs</strong><%} else{%><dfn>6.6.3.2: </dfn><a href="<%=depth%>partners/my_ptms.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >my PTMs</a><%}%><span class="hidden">.</span></li>
 						</ul>
<%
				}
%>
						</li>
						<li><%if("vct_control_policies".equals(current)){%><strong><dfn>6.6.4: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Policies</strong><%} else{%><dfn>6.6.3: </dfn><a href="<%=depth%>partners/policies.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Policies</a><%}%><span class="hidden">.</span>
<%
				if(current.contains("policies")){
%>		
	 					<ul>
	 						<li><%if("vct_control_policies_enforcement".equals(current)){%><strong><dfn>6.6.4.1: </dfn><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Enforcement</strong><%} else{%><dfn>6.6.4.1: </dfn><a href="<%=depth%>partners/policy_enforcement.jsp"><img src="<%=depth%>images/node.gif" width="9" height="9" border="0" alt="" >Enforcement</a><%}%><span class="hidden">.</span></li>
 						</ul>
<%
				}
%>
						</li>						
					</ul>
<%
			}
%>
					</li>
				</ul>
<%
		}
	}
%>
				</li>
			</ul>
		</div>

		<div>
			<h3>Info</h3>
			
			
<%
	String name = null;
	try{
		name = request.getUserPrincipal().getName();
	}
	catch(Exception e){
	}
	if(name!=null){
		
		if(request.getSession().getAttribute("cookieSend") == null){
			//send cookie to repository here
			request.getSession().setAttribute("cookieSend", true);
		}
%>
				<p>You are logged in as <b><%=request.getUserPrincipal().getName() %></b>.</p>
				<p><a href="<%=depth%>logout.jsp">logout</a></p>
				
<%
	}
	else{
%>
				<p>
					You are not logged in.<br>
					<a href="<%=depth%>create_account.jsp">Create account</a> or <a href="<%=depth%>members/secret.jsp">login</a>
				</p>
<%
	}
%>
		</div>
	</div>
</div>
<!-- #col1: - Ende -->