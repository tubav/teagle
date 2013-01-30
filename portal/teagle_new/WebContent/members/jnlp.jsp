<%@ page language="java" contentType="application/x-java-jnlp-file"
    pageEncoding="ISO-8859-1"%>

<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%
	String user    = request.getParameter("user");
	String cookie  = request.getParameter("cookie");
	String version = request.getParameter("version");

	if (user==null || cookie==null || version==null)
		throw new ServletException("jnlp parameters missing");
	
	if (! (version.equals("stable") || version.equals("devel")))
		throw new ServletException("invalid version parameter");
		
	response.setHeader("Content-Disposition", "attachment; filename=\"vct.jnlp\"");
	
	InputStream jnlpRes = config.getServletContext()
			.getResourceAsStream("/teagle-" + version + ".jnlp");
	if (jnlpRes==null)
		throw new ServletException("Can't load jnlp file");
	
	// read jnlp
	DataInputStream dis = new DataInputStream(jnlpRes);
	ByteArrayOutputStream o = new ByteArrayOutputStream();
	byte [] buf = new byte[1024];
	int len;
	while ((len = dis.read(buf, 0, 1024)) > 0)
		o.write(buf, 0, len);
	String text = new String(o.toByteArray());
	dis.close();
	
	String jarbase = "http://193.175.132.210:8080/web-start-" + version;
	//String jarbase = "http://www.fire-teagle.org/web-start-" + version;
	//String jarbase = "http://192.168.144.11/web-start-" + version;//for teagle playground
	String resbase = "http://193.175.132.210:8080";
	//String resbase = "http://www.fire-teagle.org";
	//String resbase = "http://192.168.144.11";//for teagle playground
	
	String arguments = 
		    "<argument>" + user + "</argument>\n" +
		"\t\t<argument>" + cookie + "</argument>\n" +
		"\t\t<argument>" + resbase + "</argument>";
	
	text = text.replace("codebase=\".\"", "codebase=\"" + jarbase + "\"");
	text = text.replace("<argument>.</argument>", arguments);

	%><%=text%><%
%>
