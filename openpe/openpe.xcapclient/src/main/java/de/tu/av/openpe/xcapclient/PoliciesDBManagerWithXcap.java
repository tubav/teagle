package de.tu.av.openpe.xcapclient;

import gen.openpe.elements.policy.OutputOverviews;
import gen.openpe.elements.policy.PolElements;
import gen.openpe.identifiers.policy.PoliciesIdentifiers;
import gen.openpe.identifiers.policy.PolicyIdentifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.xcap.XCAPClient;
import de.fhg.fokus.xcap.XCAPResponse;
import de.tub.av.pe.generated.utils.GenUtils;

public class PoliciesDBManagerWithXcap implements PoliciesDBManager{

	private static Logger log = LoggerFactory
			.getLogger(PoliciesDBManagerWithXcap.class);
	private XCAPClient xcapC = null;

	// constants used for ruleset framework PEL
	public static final String repoAUID = "org.openmobilealliance.policy-commonpol";
	public static final String repoMIMEtype = "application/vnd.oma.policy-commonpol+xml";
	public static final String repoNamespace = "urn:oma:xml:xdm:policy-commonpol";

	private List<String> eventsList;

	public PoliciesDBManagerWithXcap(String host, int port, String root,
			List<String> eventsListArg) {
		xcapC = new PEM2XcapClient(host, port, root);
		xcapC.setMimeType(repoMIMEtype);
		if (eventsListArg != null)
			this.eventsList = eventsListArg;
		else
			this.eventsList = new ArrayList<String>();
	}

	// ------------ Policies Associated Methods -------------

	public List<String> getAvailablePolicyTypes() {
		StringBuilder sb = new StringBuilder();
		sb.append("policytypes");
		XCAPResponse resp = xcapC.doGetGlobalDocument(repoAUID, sb.toString());
		if (resp.getStatusCode() == 200) {
			String eventsXML = resp.getResponseBody();

			PolElements polEl = GenUtils.toPolElements(eventsXML);
			return polEl == null ? new ArrayList<String>() : polEl
					.getPolicyTypes().getPolicyType();
		}
		return new ArrayList<String>();
	}

	public OutputOverviews getOutputOverviews() {
		XCAPResponse resp = xcapC.doGetGlobalDocument(repoAUID,
				"outputOverview");
		if (resp.getStatusCode() == 200) {
			String eventsXML = resp.getResponseBody();
			PolElements pe = GenUtils.toPolElements(eventsXML);
			if (pe == null)
				return new OutputOverviews();
			return pe.getOutputOverviews();
		}
		return new OutputOverviews();

	}

	public List<PolicyIdentifier> getPoliciesIdentifiers(String identityType)
			throws RepositoryException {
		// get policyIdentifiers
		StringBuilder sb = new StringBuilder();
		sb.append("policyIdentifiers");
		sb.append("_");
		sb.append(identityType);
		XCAPResponse resp = xcapC.doGetGlobalDocument(repoAUID, sb.toString());
		if (resp.getStatusCode() == 200) {
			String str = resp.getResponseBody();
			log.debug(str);
			PoliciesIdentifiers piList = GenUtils.toPoliciesIdentifiers(str);
			List<PolicyIdentifier> resultList = new ArrayList<PolicyIdentifier>();
			for (PolicyIdentifier pi : piList.getPolicyIdentifier()) {
				if (this.eventsList.contains(pi.getEvent())) {
					resultList.add(pi);
				}
			}
			return resultList;
		} else {
			throw new RepositoryException(
					"Status error code from repository");
		}
	}

	public String getPolicyContent(PolicyIdentifier pi) {
		XCAPResponse xcapres = xcapC.doGetUserDocument(repoAUID,
				encode(pi.getIdentity()), this.generateXMLDocName(pi));
		if (xcapres.getStatusCode() == 200) {
			return xcapres.getResponseBody();
		} else
			return null;
	}

	public void updatePolicy(PolicyIdentifier pi, String policyContent)
			throws RepositoryException {
		XCAPResponse resp = xcapC.doPutUserDocument(repoAUID,
				encode(pi.getIdentity()), this.generateXMLDocName(pi),
				policyContent, true);
		if (resp.getStatusCode() == 201) {
			log.debug("Policy successfuly created/updated");
		} else if (resp.getStatusCode() == 409) {
			log.error("Error validating policy " + resp.getResponseBody());
			throw new RepositoryException("Error validating policy "
					+ resp.getResponseBody());
		} else {
			log.error("Policy unsuccessfuly created/updated");
			throw new RepositoryException("Error updating because of: "
					+ resp.getResponseBody());
		}
	}

	public void addPolicy(PolicyIdentifier pi, String policyContent)
			throws RepositoryException {
		XCAPResponse resp = xcapC.doPutUserDocument(repoAUID,
				encode(pi.getIdentity()), this.generateXMLDocName(pi),
				policyContent, false);
		if (resp.getStatusCode() == 200) {
			log.debug("Policy successfuly created/updated");
		} else {
			log.debug("Policy unsuccessfuly created/updated");
			throw new RepositoryException("Error creating because of: "
					+ resp.getResponseBody());
		}
	}

	public void deletePolicy(PolicyIdentifier pi)
			throws RepositoryException {
		XCAPResponse resp = xcapC.doDeleteUserDocument(repoAUID,
				encode(pi.getIdentity()), this.generateXMLDocName(pi));

		if (resp.getStatusCode() == 200) {
			log.debug("Policy successfuly deleted");
		} else {
			log.debug("Policy unsuccessfuly deleted");
			throw new RepositoryException("Error deleting because of: "
					+ resp.getResponseBody());
		}

	}

	public List<String> getEventsList() {
		return this.eventsList;
	}

	private String encode(String data) {
		try {
			return URLEncoder.encode(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Error decoding");
			return null;
		}
	}

	private String generateXMLDocName(PolicyIdentifier arg0) {
		StringBuilder sb = new StringBuilder();
		sb.append(arg0.getIdType());
		sb.append("_");
		if (arg0.getId() == null)
			sb.append("0");
		else
			sb.append(arg0.getId());
		sb.append("_");
		sb.append(arg0.getScope());
		sb.append("_");
		sb.append(arg0.getEvent());

		return sb.toString();
	}

	public PolicyObject getPolicyObject(String identityType, String id) throws RepositoryException {

		PolicyIdentifier pi = getPolicyIdentifier(identityType, id);
		if(pi!= null)
		{
			String policyContent = getPolicyContent(pi);
			PolicyObject po = new PolicyObject();
			po.setPolicyIdentifier(pi);
			po.setPolicyContent(policyContent);
			return po;
		}
		return null;
	}

	public PolicyIdentifier getPolicyIdentifier(String identityType, String id)
			throws RepositoryException {
		StringBuilder sb = new StringBuilder();
		sb.append("policyIdentifiers");
		sb.append("_");
		sb.append(identityType);
		sb.append("_");
		sb.append(id);
		XCAPResponse resp = xcapC.doGetGlobalDocument(repoAUID, sb.toString());
		if (resp.getStatusCode() == 200) {
			String str = resp.getResponseBody();
			log.debug(str);
			 List<PolicyIdentifier> piList = GenUtils.toPoliciesIdentifiers(str).getPolicyIdentifier();			
			return piList.size() == 0? null:piList.get(0);
		} else {
			throw new RepositoryException(
					"Status error code from repository");
		}
	}
}
