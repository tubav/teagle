/*
 * Copyright (C) 2007 FhG FOKUS, Institute for Open Communication Systems
 *
 * This file is part of the OpenSE - a Parlay X Web Services implementation
 *
 * OpenSE is proprietary software that is licensed
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
 *     info@open-ims.org
 *
 */
package de.fhg.fokus.ngni.openpe.pem1;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import oma.xml.fokus.pem1_input_template.FOKUSInputTemplateType;
import oma.xml.fokus.pem1_input_template.PolicyIdentifiers;
import oma.xml.fokus.pem1_input_template.PolicyInputData;
import oma.xml.fokus.pem1_output_template.PolicyOutputData;
import oma.xml.fokus.soap_pem1_input_template.Event;
import oma.xml.fokus.soap_pem1_input_template.EventParameter;
import oma.xml.fokus.soap_pem1_input_template.FOKUSSOAPInputTemplateType;
import oma.xml.fokus.soap_pem1_input_template.ObjectFactory;

import org.openmobilealliance.schema.pem1.v1_0.DenyPolicyResponseException;
import org.openmobilealliance.schema.pem1.v1_0.InformationalException;
import org.openmobilealliance.schema.pem1.v1_0.PermanentErrorException;
import org.openmobilealliance.schema.pem1.v1_0.PolicyEnginePortType;
import org.openmobilealliance.schema.pem1.v1_0.PolicyEngineService;
import org.openmobilealliance.schema.pem1.v1_0.ProtocolErrorException;
import org.openmobilealliance.schema.pem1.v1_0.TransientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sim, ibo
 * 
 */
public class PolicyEvaluation {

	public static final int ALLOWED = 2101;
	public static final int DENIED = 2401;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private PolicyEnginePortType port;

	private static JAXBContext itjc;
	private static JAXBContext otjc;

	private String endpoint;

	private Holder<String> policyDataHolder = new Holder<String>();
	private Holder<String> correlatorHolder = new Holder<String>();
	private Holder<Integer> statusCodeHolder = new Holder<Integer>();
	private Holder<String> statusTextHolder = new Holder<String>();

	private FOKUSSOAPInputTemplateType defaultInputTemplate;

	private PolicyInputData policyData = new oma.xml.fokus.pem1_input_template.ObjectFactory()
			.createPolicyInputData();

	static {

		try {
			itjc = JAXBContext.newInstance("oma.xml.fokus.pem1_input_template");
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		try {
			otjc = JAXBContext
					.newInstance("oma.xml.fokus.pem1_output_template");
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	protected PolicyEvaluation(String endpoint) {
		this.endpoint = endpoint;
	}

	protected PolicyEvaluation(String endpoint, boolean request) {
		this(endpoint);
		defaultInputTemplate = createInputTemplate(request);
		defaultInputTemplate.setPolicyIdentifiers(new PolicyIdentifiers());
	}

	public void setOriginatorID(String originatorID) {
		defaultInputTemplate.getPolicyIdentifiers().setOriginatorID(
				originatorID);
	}

	public void addTargetID(String targetID) {
		defaultInputTemplate.getPolicyIdentifiers().getTargetID().add(targetID);
	}

	public void setEvent(String eventName) {
		Event event = new Event();
		event.setName(eventName);
		defaultInputTemplate.setEvent(event);
	}

	public boolean addParameter(String name, String value) {

		Event event = defaultInputTemplate.getEvent();

		if (event == null)
			return false;

		EventParameter parameter = new EventParameter();
		parameter.setName(name);
		parameter.setValue(value);

		event.getEventParameter().add(parameter);
		return true;
	}

	//
	// private void printPolicyInputTemplateContent()
	// {
	//
	// Marshaller marshaller;
	// StringWriter swriter = new StringWriter();
	//
	// try {
	// marshaller = itjc.createMarshaller();
	// marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
	// new Boolean(true));
	// marshaller.marshal(this.policyData, swriter);
	// } catch (JAXBException e) {
	// log.error(
	// "JAXB failed: it was not possible to parse... guess it's a namespace problem");
	// e.printStackTrace();
	// }
	//
	// String srt = swriter.toString();
	// log.debug("Created document {}", srt);
	// }

	public boolean evaluate(EvaluationHandler handler) {
		if (policyData.getPolicyInputTemplate().isEmpty()) {
			policyData.getPolicyInputTemplate().add(defaultInputTemplate);
		}
		return evaluatePolicyInputData(handler);
	}

	/**
	 * @param policyTemplate
	 * @return
	 */
	public boolean evaluate(FOKUSSOAPInputTemplateType policyTemplate,
			EvaluationHandler handler) {
		policyData.getPolicyInputTemplate().clear();
		policyData.getPolicyInputTemplate().add(policyTemplate);
		return evaluatePolicyInputData(handler);
	}

	/**
	 * @param policyTemplate
	 * @return
	 */
	private boolean evaluatePolicyInputData(EvaluationHandler handler) {

		if (port == null) {
			if (endpoint != null) {
				URL url = null;
				try {
					url = new URL(endpoint);
				} catch (MalformedURLException e1) {
					log.error("peem endpoint {} is not a URL", endpoint, e1);
				}

				QName serviceName = new QName(
						"http://www.openmobilealliance.org/schema/PEM1/v1_0",
						"PolicyEngineService");
				port = new PolicyEngineService(url, serviceName)
						.getPolicyEnginePort();
			} else {
				port = new PolicyEngineService().getPolicyEnginePort();
			}
		}

		StringWriter output = new StringWriter();

		try {
			Marshaller m = itjc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			m.marshal(policyData, output);

		} catch (JAXBException e) {
			e.printStackTrace();
			return false;
		}
		return evaluate(output.toString(), handler);
	}

	public boolean evaluate(String policyData, EvaluationHandler handler) {
		System.out.println(policyData);
		policyDataHolder.value = policyData;

		XMLGregorianCalendar timeStamp = null;
		try {
			timeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(
					new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		long time = System.currentTimeMillis();
		try {
			port.evaluatePolicy("", timeStamp, policyDataHolder,
					correlatorHolder, statusCodeHolder, statusTextHolder);
		} catch (DenyPolicyResponseException e) {
			log.debug("elapsed policy evaluation time in milliseconds is {}",
					System.currentTimeMillis() - time);
			log.debug("Access Denied: {} - {}", e.getFaultInfo()
					.getStatusCode(), e.getFaultInfo().getStatusText());
			if (handler != null)
				handler.setMessage("Evaluation Result: "
						+ e.getFaultInfo().getStatusText());
			statusCodeHolder.value = e.getFaultInfo().getStatusCode();
			statusTextHolder.value = e.getFaultInfo().getStatusText();
			policyDataHolder.value = (String) e.getFaultInfo().getPolicyData();
			return false;
		} catch (InformationalException e) {
			e.printStackTrace();
		} catch (PermanentErrorException e) {
			e.printStackTrace();
		} catch (ProtocolErrorException e) {
			e.printStackTrace();
		} catch (TransientErrorException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.debug("elapsed policy evaluation time in milliseconds is {}",
				System.currentTimeMillis() - time);
		log.debug("Access Allowed: {}- {}", statusCodeHolder.value,
				statusTextHolder.value);
		if (handler != null)
			handler.setMessage("Access Allowed: " + statusTextHolder.value);
		return true;
	}

	/**
	 * @return
	 */
	public int getLastStatusCode() {
		return statusCodeHolder.value;
	}

	/**
	 * @return
	 */
	public String getLastStatusText() {
		return statusTextHolder.value;
	}

	/**
	 * @return
	 */
	public String getLastPolicyData() {
		return policyDataHolder.value;
	}

	/**
	 * @return
	 */
	public PolicyOutputData getPolicyOutputData() {
		try {
			Unmarshaller um = otjc.createUnmarshaller();

			StringReader input = new StringReader(policyDataHolder.value);
			PolicyOutputData policyOutputData = (PolicyOutputData) um
					.unmarshal(input);
			return policyOutputData;
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return
	 */
	public FOKUSSOAPInputTemplateType createRequestInputTemplate() {
		return createInputTemplate(true);
	}

	/**
	 * @return
	 */
	public FOKUSInputTemplateType createResponseInputTemplate() {
		return createInputTemplate(false);
	}

	private FOKUSSOAPInputTemplateType createInputTemplate(boolean request) {
		FOKUSSOAPInputTemplateType template = new ObjectFactory()
				.createFOKUSSOAPInputTemplateType();
		template.setRequestMessage(request);
		template.setTemplateID("FOKUSEnablerInputTemplate_ID1");
		template.setTemplateVersion("v1.0.0");
		return template;
	}

	/**
	 * Add InputTemplate to current list of templates
	 * 
	 * @param template
	 */
	public void addInputTemplate(FOKUSInputTemplateType template) {
		policyData.getPolicyInputTemplate().add(template);
	}

	/**
	 * Clear the InputTemplate list in order to make place for the next
	 * "to evaluate" data
	 */
	public void clearInputTemplateList() {
		policyData.getPolicyInputTemplate().clear();
	}
}
