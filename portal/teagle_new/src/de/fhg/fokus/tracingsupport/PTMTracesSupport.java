package de.fhg.fokus.tracingsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class PTMTracesSupport {
	public static List<String> getTccList(String resourceId){
		BufferedReader rd = null;
		ReferenceList refList = null;
		List<String> tccList = new ArrayList<String>();
		
		try { 
//			URL url = new URL("http://193.174.152.195:8001" + resourceId + "/");
			URL url = new URL("http://10.147.67.94:8001" + resourceId + "/");
			
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			
			// Get the response
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			//This is only to fix the problem with using referense-ARRAY in the xml - it is a keyword in XStream
			StringBuffer line = new StringBuffer();
		    String chunk;
		    
		    while ((chunk=rd.readLine())!=null) {
		    	line.append(chunk);
			}
		    
		    String xml = line.toString();
		    xml = xml.replaceAll("reference-array", "references");
		    System.out.println(xml);
			
		    XStream xs = new XStream(new DomDriver());
			xs.processAnnotations(ReferenceList.class);
			xs.alias("references", ReferenceList.class);
			refList = (ReferenceList)xs.fromXML(xml);

			for (String reference : refList.getReferences()) {
				if(reference.contains("tracescollectionclient")){
					tccList.add(reference);
					System.out.println(reference);
				}
			}
			
			}catch (Exception e) {
				
			}finally{
				if(rd!=null){
					try {
						rd.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return tccList;
		}
	}
