package de.tub.av.pe.generated.utils;

import gen.openpe.elements.policy.PolElements;
import gen.openpe.identifiers.policy.PoliciesIdentifiers;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenUtils {

	private static Logger log = LoggerFactory.getLogger(GenUtils.class);

	private static JAXBContext PoliciesIdentifierJAXBContext;

	private static JAXBContext PolElementsJAXBContext;

	public static String toString(PoliciesIdentifiers policiesIdentifier) {
		try {
			if (PoliciesIdentifierJAXBContext == null) {
				PoliciesIdentifierJAXBContext = JAXBContext.newInstance(
						"gen.openpe.identifiers.policy",
						GenUtils.class.getClassLoader());
			}

			return toString(policiesIdentifier, PoliciesIdentifierJAXBContext);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
			return "";
		}
	}

	public static String toString(PolElements pels) {

		try {
			if (PolElementsJAXBContext == null) {
				PolElementsJAXBContext = JAXBContext.newInstance(
						"gen.openpe.elements.policy",
						GenUtils.class.getClassLoader());
			}
			return toString(pels, PolElementsJAXBContext);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
			return "";
		}
	}

	private static String toString(Object obj, JAXBContext context)
			throws JAXBException {
		Marshaller marshaller;
		StringWriter swriter = new StringWriter();

		marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(
				true));
		marshaller.marshal(obj, swriter);
		return swriter.toString();
	}

	public static PolElements toPolElements(String xml) {
		try {
			if (PolElementsJAXBContext == null) {
				PolElementsJAXBContext = JAXBContext.newInstance(
						"gen.openpe.elements.policy",
						GenUtils.class.getClassLoader());
			}
			return (PolElements)toObject(xml, PolElementsJAXBContext);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static PoliciesIdentifiers toPoliciesIdentifiers(String xml) {
		try {
			if (PoliciesIdentifierJAXBContext == null) {
				PoliciesIdentifierJAXBContext = JAXBContext.newInstance(
						"gen.openpe.identifiers.policy",
						GenUtils.class.getClassLoader());
			}

			return (PoliciesIdentifiers)toObject(xml, PoliciesIdentifierJAXBContext);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	
	private static Object toObject(String xmlString, JAXBContext jaxbContext) throws JAXBException {
		Unmarshaller unmarshaller;
		if (jaxbContext == null)
			return null;
		unmarshaller = jaxbContext.createUnmarshaller();

		Object obj = unmarshaller.unmarshal(new StreamSource(new StringReader(
				xmlString)));
		return obj;
	}
}
