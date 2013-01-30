package de.tub.av.pe.pem1.input.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import oma.xml.fokus.pem1_input_template.PolicyInputData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PEM1InputUtils 
{
	private static Logger log = LoggerFactory.getLogger(PEM1InputUtils.class);

	private static JAXBContext PEM1INPUTCONTEXT_WS = getJAXBContext("oma.xml.fokus.soap_pem1_input_template:oma.xml.fokus.pem1_input_template");

	private static JAXBContext getJAXBContext(String packageName)
	{		
		JAXBContext jaxbContext  = null;
			try {
				jaxbContext = JAXBContext.newInstance(packageName, PEM1InputUtils.class.getClassLoader());
			} catch (JAXBException e) {
				log.error("Error generating JAXB Context for package "+packageName, e);
			}			
		return jaxbContext;
	}
		
	public static String toString(PolicyInputData policyInputData) {
		Marshaller marshaller;
		StringWriter swriter = new StringWriter();
		
		if(PEM1INPUTCONTEXT_WS == null)
			return "";
		try 
		{
			marshaller = PEM1INPUTCONTEXT_WS.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					new Boolean(true));
			marshaller.marshal(policyInputData, swriter);			
		} catch (JAXBException e) 
		{
			log.error(
					"JAXB failed: it was not possible to parse... guess it's a namespace problem", e);
			return "";
		}
		return swriter.toString();
	}

	public static PolicyInputData toPolicyInputData(String xmlString)
	{
		Unmarshaller unmarshaller;
		if (PEM1INPUTCONTEXT_WS == null)
			return null;
		try 
		{       
			unmarshaller = PEM1INPUTCONTEXT_WS.createUnmarshaller();
			Object obj = unmarshaller.unmarshal(new StreamSource(
					new StringReader(xmlString)));
			return (PolicyInputData)obj;
		} catch (JAXBException e) {
			log.error(
					"JAXB failed: it was not possible to parse... guess it's a namespace problem", e);
		}
		return null;
	}
}
