package de.fhg.fokus.tracingsupport;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class PTMControlServlet
 */

public class TracesControlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	public enum actions{ 
		stop, start, deploy, delete; 
		
		public static actions toActions(String str){
			try{
				return valueOf(str);
			}catch(Exception e){
				return null;
			}
		}
	};
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TracesControlServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("I GOT A REQUEST");
		String resourceId = request.getParameter("resourceId");
		String action = request.getParameter("action");
		System.out.println(resourceId);
		
//		response.getOutputStream();
		
/*		URL url = new URL("http://193.174.152.195:8001"); 
		
		HttpURLConnection conn = (HttpURLConnection) url
				.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-type","text/plain");
		String xml = "<tracescollectionclient>"+ resourceId + "</tracescollectionclient>";
*/
		
//		URL url = new URL("http://193.174.152.195:8001" + resourceId);
		URL url = new URL("http://10.147.67.94:8001" + resourceId );
		
		System.out.println(url);
		StringBuffer sb = new StringBuffer();
		String message = null;
			
		HttpURLConnection conn; 
		
		switch (actions.toActions(action)){
			case start:
				System.out.println("start");
			
				sb.append("<tracescollectionclient action=\"update\">\n");
				sb.append("<started type=\"boolean\">true</started>\n");
				sb.append("</tracescollectionclient>\n");
				message = "started";
				break;

			case stop:
				System.out.println("stop");
				sb.append("<tracescollectionclient action=\"update\">\n");
				sb.append("<started type=\"boolean\">false</started>\n");
				sb.append("</tracescollectionclient>\n");
				message = "stoped";
				break;
				
			case deploy:
				System.out.println("deploy");
				
				sb.append("<tracescollectionclient>\n");
				sb.append("<sink_ip>10.147.65.205</sink_ip>\n");
				sb.append("<sink_port>8080</sink_port>\n");
				sb.append("<capture_filter> </capture_filter>\n");
				sb.append("<display_filter> </display_filter>\n");
				sb.append("<traces_type>R</traces_type>\n");
				sb.append("<send_protocol>http</send_protocol>\n");
				sb.append("<started type=\"boolean\">false</started>\n");
				sb.append("</tracescollectionclient>\n");
				
				message = "deployed";
				break;
				
			case delete:
				System.out.println("delete");
				
				message = "deleted";
				break;
		}
		
		String xml = sb.toString();
		System.out.println("\n\n\n" + xml);
		
		PrintWriter out = null;
		
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		
		if(action.equals("delete")){
			
			conn.setRequestProperty(
			    "Content-Type", "application/x-www-form-urlencoded" );
			conn.setRequestMethod("DELETE");
			System.out.println("Method: " + conn.getRequestMethod());
			int responseCode = conn.getResponseCode();
			System.out.println("Response code: " + responseCode);
		}else{
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());
			out.write(xml);
			out.flush();
			out.close();
		}
		
		
//		OutputStream out = conn.getOutputStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		
		while ((line = rd.readLine()) != null) {
			 System.out.println(line);
		}
		
		rd.close();

		conn.disconnect();
		
		PrintWriter answer = response.getWriter();
		answer.write(message);
		answer.flush();
		answer.close();
		
		System.out.println("end");
		
	}

	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

