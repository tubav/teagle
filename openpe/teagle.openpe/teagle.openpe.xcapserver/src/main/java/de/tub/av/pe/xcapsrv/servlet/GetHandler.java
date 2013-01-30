/*
 * Copyright (C) 2009 FhG FOKUS, Institute for Open Communication Systems
 *
 * This file is part of the FOKUS XDMS - an XML Document Management Server
 * 
 * The FOKUS XDMS is proprietary software that is licensed
 * under the FhG FOKUS "SOURCE CODE LICENSE for FOKUS Open IMS COMPONENTS".
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

package de.tub.av.pe.xcapsrv.servlet;

import gen.openpe.elements.policy.EvaluationOutput;
import gen.openpe.elements.policy.OutputOverview;
import gen.openpe.elements.policy.OutputOverviews;
import gen.openpe.elements.policy.PolElements;
import gen.openpe.elements.policy.RuleEvaluationOutput;
import gen.openpe.identifiers.policy.ObjectFactory;
import gen.openpe.identifiers.policy.PoliciesIdentifiers;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.db.PolicyIdentifier;
import de.tub.av.pe.db.PolicyObject;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.PolicyRepositoryException;
import de.tub.av.pe.generated.utils.GenUtils;
import de.tub.av.pe.rule.utils.LoggingBean;
import de.tub.av.pe.rule.utils.LoggingBean.LogEntry;
import de.tub.av.pe.rule.utils.ReasonOverview.ReasonEntry;
import de.tub.av.pe.rule.utils.RulesEvalOverview.RuleEntry;
import de.tub.av.pe.xcapsrv.DocumentSelector;
import de.tub.av.pe.xcapsrv.Resource;
import de.tub.av.pe.xcapsrv.XCAPResult;
import de.tub.av.pe.xcapsrv.XCAPResultFactory;
import de.tub.av.pe.xcapsrv.etag.ETagValidator;

/**
 * The Class GetHandler handles HTTP XCAP GET requests. All incoming GET
 * requests can be used
 */
public class GetHandler implements Handler {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(GetHandler.class);
	private OpenPEContext pecontext;

	public GetHandler(OpenPEContext pecontext) {
		this.pecontext = pecontext;
	}

	public XCAPResult process(DocumentSelector documentSelector,
			String mimeType, InputStream contentStream,
			ETagValidator etagValidator) {
		PolicyRepository polRepo = pecontext.getPolicyRepositoryManager()
				.getInstance();

		String auid = documentSelector.getAUID();

		if (!auid.equals(PEXcapSrvConstants.auid)) {
			log.error("The AUID " + documentSelector.getAUID()
					+ " is not supported");
			return XCAPResultFactory.newResultForOtherError(
					HttpServletResponse.SC_NOT_FOUND, "AppUsage of " + auid
							+ " not found. Please use "
							+ PEXcapSrvConstants.auid);
		}

		if (mimeType != null && !mimeType.equals(PEXcapSrvConstants.mime)) {
			log.error("The MIME " + mimeType + " was not found.");
			return XCAPResultFactory.newResultForOtherError(
					HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Mime"
							+ mimeType + " not found. Please use "
							+ PEXcapSrvConstants.mime);
		}

		try {
			if (documentSelector.isPolicyIdentifiersReq()) {			
				PoliciesIdentifiers pis = new ObjectFactory()
						.createPoliciesIdentifiers();
				
				if(documentSelector.getPolicyId() == null)
				{				
					List<PolicyIdentifier> piList = polRepo
							.getPoliciesIdentifiers(documentSelector
									.getPolicyType());
					for (PolicyIdentifier pi : piList) {
						pis.getPolicyIdentifier().add(toGenPolicyIdentifier(pi));
					}

				}else
				{
					PolicyIdentifier pi = polRepo.getPolicyIdentifier(documentSelector.getPolicyId());
					pis.getPolicyIdentifier().add(toGenPolicyIdentifier(pi));
				}

				String result = GenUtils.toString(pis);
				return XCAPResultFactory.newResultForGetOK(new Resource(
						PEXcapSrvConstants.mime, result));
			} else if (documentSelector.isOutputOverviewReq()) {
				LoggingBean logging = pecontext.getLoggingBeanObject();
				PolElements polEl = new gen.openpe.elements.policy.ObjectFactory()
						.createPolElements();
				polEl.setOutputOverviews(toOutputOverviews(logging));
				String result = GenUtils.toString(polEl);
				return XCAPResultFactory.newResultForGetOK(new Resource(
						PEXcapSrvConstants.mime, result));
			} else {
				// get based on the policy type, identity name, identity scope,
				// policy ID
				PolicyIdentifier pi = new PolicyIdentifier();
				pi.setIdType(documentSelector.getPolicyType());
				pi.setIdentity(documentSelector.getIdentity());
				pi.setEvent(documentSelector.getEvent());
				pi.setScope(documentSelector.getIdentityScope());
				pi.setId(documentSelector.getPolicyId());
				PolicyObject po = new PolicyObject();
				po.setPolicyContent(polRepo.getPolicyContent(pi));
				po.setPolicyIdentifier(pi);
				if (etagValidator == null
						|| etagValidator.isValid(po.getEtag()))
					return XCAPResultFactory.newResultForGetOK(new Resource(
							PEXcapSrvConstants.mime, po.getPolicyContent()));
				else
					return XCAPResultFactory
							.newResultForOtherError(HttpServletResponse.SC_NOT_MODIFIED);
			}

		} catch (PolicyRepositoryException e) {
			log.error("Cannot find document", e);
			return XCAPResultFactory.newResultForOtherError(
					HttpServletResponse.SC_NOT_FOUND, "The path "
							+ documentSelector.getDocumentParent()
							+ " could not be found.");
		}
	}

	private OutputOverviews toOutputOverviews(LoggingBean logbean) {
		OutputOverviews oos = new OutputOverviews();

		for (LogEntry logEntry : logbean.getLogEntries()) {
			oos.getOutputOverview().add(toOutputOverview(logEntry));
		}
		return oos;
	}

	private OutputOverview toOutputOverview(LogEntry logEntry) {
		OutputOverview oo = new OutputOverview();
		oo.setDecision(logEntry.getFinalDecission());
		oo.setEvent(logEntry.getEvent());
		oo.setOriginator(logEntry.getOriginator());
		oo.setOriginatortype(logEntry.getOriginatorType());
		oo.setTarget(logEntry.getTarget());
		oo.setTargettype(logEntry.getTargetType());
		oo.setTime(logEntry.getTime());
		oo.setId(logEntry.getId());
		
		List<ReasonEntry> reasonEntries = logEntry.getReason()
				.getReasonEntries();

		for (ReasonEntry reasonEnt : reasonEntries) {
			oo.getEvaluationOutput().add(toEvaluationOutput(reasonEnt));

		}
		return oo;
	}

	private EvaluationOutput toEvaluationOutput(ReasonEntry reasonEntry) {
		EvaluationOutput eo = new EvaluationOutput();
		eo.setPolicyName(reasonEntry.getPolicyName());
		eo.setReason(reasonEntry.getReason());		
		List<RuleEntry> ruleEntries = reasonEntry.getRulesEvalOverview().getRuleEntries();
		for (RuleEntry ruleE : ruleEntries) {
			eo.getRuleEvaluationOutput().add(toRuleEvaluationOutput(ruleE));
		}

		return eo;
	}

	private RuleEvaluationOutput toRuleEvaluationOutput(RuleEntry ruleEntry) {
		RuleEvaluationOutput reo = new RuleEvaluationOutput();
		reo.setRuleId(ruleEntry.getRuleId());
		reo.setReason(ruleEntry.getActionsExecLog());
		return reo;
	}
	
	private gen.openpe.identifiers.policy.PolicyIdentifier toGenPolicyIdentifier(PolicyIdentifier pi)
	{
		gen.openpe.identifiers.policy.PolicyIdentifier gpi = new gen.openpe.identifiers.policy.PolicyIdentifier();
		gpi.setId(pi.getId());
		gpi.setIdentity(pi.getIdentity());
		gpi.setIdType(pi.getIdType());
		gpi.setPriority(pi.getPriority());
		gpi.setScope(pi.getScope());
		gpi.setPriority(pi.getPriority());
		gpi.setEvent(pi.getEvent());

		return gpi;
	}

}