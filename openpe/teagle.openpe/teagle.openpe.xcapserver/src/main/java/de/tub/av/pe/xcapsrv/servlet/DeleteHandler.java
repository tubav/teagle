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

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.db.PolicyIdentifier;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.PolicyRepositoryException;
import de.tub.av.pe.xcapsrv.DocumentSelector;
import de.tub.av.pe.xcapsrv.XCAPResult;
import de.tub.av.pe.xcapsrv.XCAPResultFactory;
import de.tub.av.pe.xcapsrv.etag.ETagValidator;




/**
 * The Class DeleteHandler.
 * XCAP Handlers verify whether the required action on the document is executable before being carried out at the Database Adapter.
 * For example, if a client attempts to put a new element into a document, it should be confirmed by the XCAP Handler that 
 * the document exists and the modified document is still well-formed and valid. 
 */
public class DeleteHandler implements Handler {

    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(DeleteHandler.class);
    
    private OpenPEContext pecontext;

    public DeleteHandler(OpenPEContext pecontext) {
		this.pecontext = pecontext;
	}

	
    /**
     * Deletes a document
     * @return the XCAP result
     */
    public XCAPResult process(DocumentSelector documentSelector, String mimeType, InputStream contentStream, ETagValidator etagValidator) {
    	
    	String auid = documentSelector.getAUID();
    	
    	PolicyRepository polRepo = this.pecontext.getPolicyRepositoryManager().getInstance();

    	if(!auid.equals(PEXcapSrvConstants.auid))
    	{
        	log.error("The AUID "+documentSelector.getAUID()+" was not found in Hashtable appUsages");
			return XCAPResultFactory.newResultForOtherError(HttpServletResponse.SC_NOT_FOUND, "AppUsage of " + auid + " not found. Please use "+ PEXcapSrvConstants.auid);    		
    	}
    	
    	if(mimeType!=null && !mimeType.equals(PEXcapSrvConstants.mime))
    	{
        	log.error("The MIME "+mimeType+" was not found.");
			return XCAPResultFactory.newResultForOtherError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE , "Mime" + mimeType+ " not found. Please use "+ PEXcapSrvConstants.mime);    		
    	}
    	
		//get based on the policy type, identity name, identity scope, 
		PolicyIdentifier pi = new PolicyIdentifier();
		pi.setIdType(documentSelector.getPolicyType());
		pi.setIdentity(documentSelector.getIdentity());
		pi.setEvent(documentSelector.getEvent());
		pi.setScope(documentSelector.getIdentityScope());
		if(!documentSelector.getPolicyId().equals("0"))
			pi.setId(documentSelector.getPolicyId());
		pi.setPriority(0);
    	try{
	        	polRepo.deletePolicy(pi); 
	        	return XCAPResultFactory.newResultForOK(200);	
    	}catch(PolicyRepositoryException e)
    	{
    		log.error("Cannot find document", e);
    		return XCAPResultFactory.newResultForOtherError(HttpServletResponse.SC_NOT_FOUND, "The path "+ documentSelector.getDocumentParent()+ " could not be found.");
    	}
    }

}