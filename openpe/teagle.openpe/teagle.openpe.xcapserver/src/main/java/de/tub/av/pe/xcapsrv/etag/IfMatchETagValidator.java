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

package de.tub.av.pe.xcapsrv.etag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IfMatchETagValidator implements ETagValidator {

    private String eTag;
    private final boolean noneMatch = false;
    private static final Log logger = LogFactory.getLog(IfMatchETagValidator.class);
    public IfMatchETagValidator(String eTag) {
        this.eTag = eTag;
    }

    public boolean isValid(String documentETag) {
        logger.debug("entering isValid of IfMatchETagValidator");
        boolean result = true;
        if (eTag != null) {
            if (eTag.compareTo("*") == 0) {
                // matches anything except null
                if (documentETag == null) {
                    result = false;
                }
            } else {
                // etags must match
                logger.debug("the current etag is :"+eTag);
                logger.debug("the document etag is:"+documentETag);
                if (documentETag == null || !eTag.equals(documentETag)) {
                    logger.debug("these two etag are not the same");
                   result = false;
                }
            }
        }
        return result;
    }

    public String getETag() {
        return eTag;
    }
    
    public boolean getNoneMatch() {
    	return noneMatch;
    }
}

