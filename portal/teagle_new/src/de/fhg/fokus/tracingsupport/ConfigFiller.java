package de.fhg.fokus.tracingsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class ConfigFiller {
	

	public static Config fill(String resourceId) throws IOException{
		
		URL url = new URL("http://10.147.67.94:8001" + resourceId);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		StringBuffer sb = new StringBuffer();
		
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		
		try {
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		conn.disconnect();
		
		XStream xs = new XStream(new DomDriver());
		xs.processAnnotations(Config.class);
		xs.alias("tracescollectionclient", Config.class);
        String xml = sb.toString();
        
		Config conf = (Config)xs.fromXML(xml);
		
		return conf;
	}
	
	

}
