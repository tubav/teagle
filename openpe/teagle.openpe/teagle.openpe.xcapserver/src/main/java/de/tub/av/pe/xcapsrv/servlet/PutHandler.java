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

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.db.PolicyIdentifier;
import de.tub.av.pe.db.PolicyObject;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.PolicyRepositoryException;
import de.tub.av.pe.db.ValidationException;
import de.tub.av.pe.xcapsrv.DocumentSelector;
import de.tub.av.pe.xcapsrv.XCAPResult;
import de.tub.av.pe.xcapsrv.XCAPResultFactory;
import de.tub.av.pe.xcapsrv.XDMSConstants;
import de.tub.av.pe.xcapsrv.XMLValidator;
import de.tub.av.pe.xcapsrv.error.InternalServerErrorException;
import de.tub.av.pe.xcapsrv.error.NotUTF8ConflictException;
import de.tub.av.pe.xcapsrv.etag.ETagValidator;

/**
 * The Class PutHandler handles all incoming XCAP PUT queries.
 */
public class PutHandler implements Handler {

	private static final Logger log = LoggerFactory.getLogger(PutHandler.class);

	private OpenPEContext pecontext;

	PutHandler(OpenPEContext pecontext) {
		this.pecontext = pecontext;
	}

	/**
	 * Puts a document or an element of a document into the xml-database.
	 * 
	 * @return the XCAP result
	 */

	public XCAPResult process(DocumentSelector documentSelector,
			String mimeType, InputStream contentStream,
			ETagValidator etagValidator) {

		PolicyRepository polRepo = pecontext.getPolicyRepositoryManager()
				.getInstance();

		String auid = documentSelector.getAUID();

		if (!auid.equals(PEXcapSrvConstants.auid)) {
			log.error("The AUID " + documentSelector.getAUID()
					+ " was not found in Hashtable appUsages");
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
			String content = XMLValidator.getUTF8String(contentStream);
			// don't validate
			// put based on the policy type, identity name, identity scope,
			PolicyObject po = new PolicyObject();
			PolicyIdentifier pi = new PolicyIdentifier();
			po.setPolicyIdentifier(pi);
			pi.setIdType(documentSelector.getPolicyType());
			pi.setId(documentSelector.getPolicyId());
			pi.setEvent(documentSelector.getEvent());
			pi.setScope(documentSelector.getIdentityScope());
			pi.setIdentity(documentSelector.getIdentity());
			pi.setPriority(0);
			po.setPolicyContent(content);

			if (etagValidator != null && etagValidator.getNoneMatch()) {
				try {
					polRepo.addPolicy(po);
					return XCAPResultFactory.newResultForOK(200);
				} catch (DuplicateValueException e) {
					log.debug("Policy failed to upload because of duplication");
					return XCAPResultFactory.newResultForConflict(
							XDMSConstants.UNIQUENESS_FAILURE_INDEX, e.getMessage());
				}
			} else {
				polRepo.updatePolicy(po);
				return XCAPResultFactory.newResultForOK(201, po.getEtag());
			}

		} catch (ValidationException e) {
			log.error("Error validating the input stream " + e.getMessage());
			return XCAPResultFactory.newResultForConflict(
					XDMSConstants.SCHEMA_VALIDATION_ERROR_INDEX,
					e.getMessage());

		} catch (PolicyRepositoryException e) {
			log.error("Cannot find document", e);
			return XCAPResultFactory.newResultForOtherError(
					HttpServletResponse.SC_NOT_FOUND, "The path "
							+ documentSelector.getDocumentParent()
							+ " could not be found.");
		} catch (NotUTF8ConflictException e) {
			log.error("Error decoding the input stream", e);
			return XCAPResultFactory
					.newResultForOtherError(HttpServletResponse.SC_NOT_FOUND,
							"Error decoding the input stream.The supported format is UTF8.");
		} catch (InternalServerErrorException e) {
			log.error("Error decoding the input stream", e);
			return XCAPResultFactory
					.newResultForOtherError(HttpServletResponse.SC_NOT_FOUND,
							"Error decoding the input stream.The supported format is UTF8.");
		} catch (DuplicateValueException e) {
			log.debug("Policy failed to upload because of duplication");
			return XCAPResultFactory.newResultForConflict(
					XDMSConstants.UNIQUENESS_FAILURE_INDEX,
					XDMSConstants.UNIQUENESS_FAILURE);
		}
	}

}