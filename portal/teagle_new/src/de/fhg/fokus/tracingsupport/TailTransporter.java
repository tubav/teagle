package de.fhg.fokus.tracingsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TailTransporter
 */
public class TailTransporter extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TailTransporter() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("TailTransporter was called");
		
		String resourceId = request.getParameter("resourceId");
			
		URL url = new URL("http://10.147.65.205:8080/TracesCollectionServer/" +
						  "TailServlet?resourceId=" + URLEncoder.encode(resourceId,"UTF-8"));
		
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		PrintWriter answ = response.getWriter();
		String line;
		
		while((line=in.readLine())!=null){
			answ.write(line + "\n");
			answ.flush();
//			System.out.println("TailTransporter: " + line);
		}
//		System.out.println("LINE = NULL!");
		in.close();
		answ.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
}
