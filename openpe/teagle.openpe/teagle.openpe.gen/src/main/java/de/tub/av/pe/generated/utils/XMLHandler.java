
package de.tub.av.pe.generated.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Operations for XML Handling through JAXB context like marshalling and demarshalling
 * @author Irina Boldea
 *
 */
public class XMLHandler {
	private final static Logger log = LoggerFactory.getLogger(XMLHandler.class);

	private static Properties contextPropertiesMap = new Properties();
	public static JAXBContext getJAXBContext(String packageName) {
		JAXBContext jaxbContext = null;
		if (!contextPropertiesMap.containsKey(packageName)) {
			log.warn("Context for package {} was build", packageName);
			try {
				jaxbContext = JAXBContext.newInstance(packageName);
				contextPropertiesMap.put(packageName, jaxbContext);
			} catch (JAXBException e) {
				log.error("Error generating JAXB Context for package " + packageName, e);
			}
		} else
			jaxbContext = (JAXBContext) contextPropertiesMap.get(packageName);
		return jaxbContext;
	}

	/**
	 * Transforms a JAXB object into an XML string.
	 * @param policyObject a JAXB object describing a policy
	 * @return the same policy as an XML string ready to be sent to the XDMS
	 * @see #printToJavaObject(String, String)
	 */
	public static String printToString(Object policyObject, JAXBContext context) {
		Marshaller marshaller;
		StringWriter swriter = new StringWriter();

		try {
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					new Boolean(true));
			marshaller.marshal(policyObject, swriter);
		} catch (JAXBException e) {
			log.error(
					"JAXB failed: it was not possible to parse... guess it's a namespace problem");
			e.printStackTrace();
			return "";
		}

		String policyString = swriter.toString();
		//log.debug("marshalling successfull; following document was created:");

		return policyString;
	}

	/**
	 * Transforms a JAXB object that is not XmlRootElement into an XML string.This function can be used
	 * in order to generate XML documents from JAXB Objects that are don't represent the root element. (e.g.
	 * <code>rule</code> is a child of the root <code>ruleset</code>)
	 * @param policyObject a JAXB object describing a policy
	 * @param context JAXB context
	 * @param elementName no JAXB contex's root element name e.g.<code>rule</code> 
	 * @param namespace JAXB context namespace
	 * @return the same policy as an XML string ready to be sent to the XDMS
	 * @see #printToJavaObject(String, String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String printToStringNoRootElement(Object policyObject, JAXBContext context, String elementName, String namespace)
	{
		Marshaller marshaller;
		StringWriter swriter = new StringWriter();
		if(context == null)
			return "";

		try {
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					new Boolean(true));
			marshaller.marshal(new JAXBElement(new QName(namespace, elementName), policyObject.getClass(), policyObject), swriter);            
		} catch (JAXBException e) {
			log.error(
					"JAXB failed: it was not possible to parse... guess it's a namespace problem");
			e.printStackTrace();

			return "";
		}

		String policyString = swriter.toString();

		return policyString;
	}


	/**
	 * Transforms a XML string into a JAXB object.
	 * @param xmlString string that is transformed
	 * @param pack the package name of the JAXB generated objects
	 * @return JAXB object
	 * @see #printToString(Object, String)
	 */
	public static Object printToJavaObject(String xmlString, JAXBContext jaxbContext) {
		Unmarshaller unmarshaller;

		if (jaxbContext == null)
			return null;
		try 
		{       
			unmarshaller = jaxbContext.createUnmarshaller();

			Object obj = unmarshaller.unmarshal(new StreamSource(
					new StringReader(xmlString)));
			return obj;
		} catch (JAXBException e) {
			log.error(
					"JAXB failed: it was not possible to parse... guess it's a namespace problem", e);
			log.error(xmlString);
		}

		return null;
	}

}
