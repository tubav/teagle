package de.fhg.fokus.tracingsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TccConfigServlet
 */
public class TccConfigServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TccConfigServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String captureFilter = request.getParameter("captureFilter");
		String displayFilter = request.getParameter("displayFilter");
		String sinkIp = request.getParameter("sinkIp");
		String sinkPort = request.getParameter("sinkPort");
		String tracesType = request.getParameter("tracesType");
		String resourceId = request.getParameter("resourceId");
		
		URL url = new URL("http://10.147.67.94:8001" + resourceId );
		
		System.out.println("\nURL"+url);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<tracescollectionclient action=\"update\">\n");
		sb.append("<sink_ip>" + sinkIp + "</sink_ip>\n");
		sb.append("<sink_port>" + sinkPort + "</sink_port>\n");
		sb.append("<capture_filter>" + captureFilter +"</capture_filter>\n");
		sb.append("<display_filter>" + displayFilter +"</display_filter>\n");
		sb.append("<traces_type>" + tracesType + "</traces_type>\n");
		sb.append("<send_protocol>http</send_protocol>\n");
		sb.append("</tracescollectionclient>\n");
		
		System.out.println("Config update: \n ");
		System.out.println(sb);
	
		String xml = sb.toString();
		System.out.println("\n\n\n" + xml);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);

		PrintWriter out = new PrintWriter(conn.getOutputStream());
//		OutputStream out = conn.getOutputStream();
		out.write(xml);
		out.flush();
		

		
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		
		while ((line = rd.readLine()) != null) {
			 System.out.println(line);
		}
		
		rd.close();
		out.close();
		conn.disconnect();
		
		PrintWriter answer = response.getWriter();
		answer.flush();
		answer.close();
		
	}

}
