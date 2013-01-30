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

import org.apache.commons.httpclient.HttpConnection;


/**
 * @author Alexander Fedulov
 * 
 * This class is only created for bypassing the limitations of JavaScript's Same Origin Policy
 *
 */
public class StatusReportTransporter extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String resourceId = request.getParameter("resourceId");
		
		URL url = new URL("http://10.147.65.205:8080/TracesCollectionServer/" +
						  "StatusReporter?resourceId=" + URLEncoder.encode(resourceId,"UTF-8"));
		
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoInput(true);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		PrintWriter answ = response.getWriter();
		String line;
		
		while((line=in.readLine())!=null){
			answ.write(line);
			answ.flush();
//			System.out.println("StatusReportTransporter: " + line);
		}
//		System.out.println("LINE = NULL!");
		in.close();
		answ.close();
	}
}
