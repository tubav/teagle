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

import de.tub.av.pe.xcapsrv.DocumentSelector;
import de.tub.av.pe.xcapsrv.XCAPResult;
import de.tub.av.pe.xcapsrv.etag.ETagValidator;

/**
 * 
 * XCAP Handlers verify whether the required action on the document is executable before being carried out at the Database Adapter. 
 * For example, if a client attempts to put a new element into a document, it should be confirmed by the XCAP Handler that 
 * the document exists and the modified document is still well-formed and valid. 
 */

public interface Handler {
     	/**
      * Process.
      * 
      * @param resourceSelector the resource selector
      * @param mimeType the mime type
      * @param contentStream the content stream
      * @param eTagValidator the e tag validator
      * 
      * @return the xCAP result
      */
     public XCAPResult process(DocumentSelector resourceSelector, String mimeType, InputStream contentStream, ETagValidator etagValidator) ;
}
