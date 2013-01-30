/*
 * Copyright (C) 2010 FhG FOKUS, Institute for Open Communication Systems
 *
 * This file is part of OpenPE (Open Policy Engine) - an implementation of
 * the OMA PEEM specification represents the request- and service-specific
 * policies enforcement entity
 *
 * The OpenPE is proprietary software that is licensed
 * under the FhG FOKUS "SOURCE CODE LICENSE for FOKUS IMS COMPONENTS".
 * You should have received a copy of the license along with this
 * program; if not, write to Fraunhofer Institute FOKUS, Kaiserin-
 * Augusta Allee 31, 10589 Berlin, GERMANY
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * It has to be noted that this software is not intended to become
 * or act as a product in a commercial context! It is a PROTOTYPE
 * IMPLEMENTATION for IMS technology testing and IMS application
 * development for research purposes, typically performed in IMS
 * test-beds. See the attached license for more details.
 *
 * For a license to use this software under conditions
 * other than those described here, please contact Fraunhofer FOKUS
 * via e-mail at the following address:
 *     info@fokus.fraunhofer.de
 *
 */
package de.tub.av.pe.pem1.output.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import oma.xml.fokus.pem1_output_template.PolicyOutputData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PEM1OutputUtils {
	
	private static Logger log = LoggerFactory.getLogger(PEM1OutputUtils.class);

	private static JAXBContext PEM1OUTPUTCONTEXT = getJAXBContext("oma.xml.fokus.pem1_output_template");

	private static JAXBContext getJAXBContext(String packageName)
	{		
		JAXBContext jaxbContext  = null;
			try {
				jaxbContext = JAXBContext.newInstance(packageName, PEM1OutputUtils.class.getClassLoader());
			} catch (JAXBException e) {
				log.error("Error generating JAXB Context for package "+packageName, e);
			}			
		return jaxbContext;
	}
		
	public static String toString(PolicyOutputData policyOutputData)
	{
		 Marshaller marshaller;
	        StringWriter swriter = new StringWriter();

	        try {
	            marshaller = PEM1OUTPUTCONTEXT.createMarshaller();
	            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
	                new Boolean(true));
	            marshaller.marshal(policyOutputData, swriter);
	        } catch (JAXBException e) {
	            log.error(
	                "JAXB failed: it was not possible to parse... guess it's a namespace problem", e);
	            return "";
	        }

	        return swriter.toString();
	}
	
	public static PolicyOutputData toPolicyOutputData(String xmlString) 
	{
        
		Unmarshaller unmarshaller;
        if (PEM1OUTPUTCONTEXT == null)
        	return null;
        try 
        {       
            unmarshaller = PEM1OUTPUTCONTEXT.createUnmarshaller();

            Object obj = unmarshaller.unmarshal(new StreamSource(
                        new StringReader(xmlString)));
            return (PolicyOutputData)obj;
        } catch (JAXBException e) {
            log.error(
                "JAXB failed: it was not possible to parse... guess it's a namespace problem", e);
        }
        return null;
    }
}
