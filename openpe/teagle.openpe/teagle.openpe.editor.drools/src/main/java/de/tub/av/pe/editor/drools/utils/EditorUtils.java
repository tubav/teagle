package de.tub.av.pe.editor.drools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.lang.DrlDumper;
import org.drools.lang.descr.PackageDescr;
import org.drools.xml.XmlDumper;
import org.drools.xml.XmlPackageReader;
import org.xml.sax.SAXException;
public class EditorUtils {

	public static String generatePolicyTemplate(String basefolder) {
		StringBuilder contents = new StringBuilder();
		try {

			BufferedReader input = new BufferedReader(new FileReader(new File(
					basefolder+ "/PolicyTemplate.xml")));
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

	public static String GetPolicySchemaContent(String basefolder) {
		StringBuilder contents = new StringBuilder();
		try {

			BufferedReader input = new BufferedReader(new FileReader(new File(
					basefolder
							+ "/org.openmobilealliance.policy_commonpol.xsd")));
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

	public static ArrayList<String> getAvailableScopeNames(String name) {
		ArrayList<String> list = new ArrayList<String>();

		if (name.equals("Global")) {
			list.add("All");
		} else {
			list.add("Originator");
			list.add("Target");
		}
		return list;
	}
	
	public static String toXML(String content) throws DroolsParserException
	{
		Reader in = new StringReader(content);
		DrlParser parser = new DrlParser();
		PackageDescr pckd = parser.parse(in);
		System.setProperty("drools.schema.validating", "false");
		XmlDumper dumper = new XmlDumper();
		String str = dumper.dump(pckd);
		
		return str;
	}
	
	public static String toDrl(String xml) throws SAXException, IOException
	{
		
	  System.setProperty("drools.schema.validating", "false");
      PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
      XmlPackageReader reader = new XmlPackageReader(conf.getSemanticModules());      
      PackageDescr sd = reader.read(new StringReader(xml));        
      DrlDumper drlDumper = new DrlDumper();
      return drlDumper.dump(sd);		
	}

}
