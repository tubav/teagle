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

package de.tub.av.pe.xcapsrv;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;

public class XCAPResultFactory {
    private static final String ERROR_DOCUMENT_PREFIX = "<?xml version='1.0' encoding='UTF-8'?><xcap-error xmlns='urn:ietf:params:xml:ns:xcap-error'>";
    private static final String ERROR_DOCUMENT_SUFFIX = "</xcap-error>";
    private static Log log = LogFactory.getLog(XCAPResult.class);
    
    public static XCAPResult newResultForConflict(int index, String reason){
        XCAPResult result;
        switch(index){
             case XDMSConstants.CANNOT_DELETE_INDEX:           result = conflictDelete();            break;
             case XDMSConstants.CANNOT_INSERT_INDEX:           result = conflictInsert();            break;
             case XDMSConstants.CONSTRAINT_FAILURE_INDEX:      result = conflictConstraintFailure(); break;
             case XDMSConstants.EXTENSION_INDEX:               result = conflictExtension();         break;
             case XDMSConstants.NO_PARENT_INDEX:               result = conflictNoParent();          break;
             case XDMSConstants.NO_WELL_FORMED_INDEX:          result = conflictNoWellFormed();      break;
             case XDMSConstants.NOT_XML_ATT_VALUE_INDEX:       result = conflictNOTXmlAttValue();    break;
             case XDMSConstants.NOT_XML_FRAG_INDEX:            result = conflictNOTXmlFrag();        break;
             case XDMSConstants.SCHEMA_VALIDATION_ERROR_INDEX: result = conflictSchemaValidation(reason);  break;
             case XDMSConstants.UNIQUENESS_FAILURE_INDEX:      result = conflictUniquenessFailure(); break;
             case XDMSConstants.NOT_UTF_8_INDEX:               result = conflictNOTUTF8(); break;
             default: log.debug("because of a not correct index, reply with internal server error");
                      result = new XCAPResult();
                      result.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     break;
        }
        return result;
    }

    public static XCAPResult newResultForOtherError(int statusCode){
        XCAPResult result;
        result = new XCAPResult();
        result.setStatusCode(statusCode);
        return result;
    }
    public static XCAPResult newResultForMethodNotAllowed(){
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        result.addHeader(XDMSConstants.HEADER_ALLOW,XDMSConstants.REQUEST_METHOD_GET);
        return result;
    }
    private static XCAPResult conflictUniquenessFailure() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.UNIQUENESS_FAILURE+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictSchemaValidation(String reason) {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.SCHEMA_VALIDATION_ERROR+">");
        content.append(reason);
        content.append("</"+XDMSConstants.SCHEMA_VALIDATION_ERROR+">");        
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictNOTXmlFrag() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.NOT_XML_FRAG+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictNOTXmlAttValue() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.NOT_XML_ATT_VALUE+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictNoWellFormed() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.NO_WELL_FORMED+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictNoParent() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.NO_PARENT+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;  //To change body of created methods use File | Settings | File Templates.
    }

    private static XCAPResult conflictExtension() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private static XCAPResult conflictConstraintFailure() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.CONSTRAINT_FAILURE+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictInsert() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.CANNOT_INSERT+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }

    private static XCAPResult conflictDelete() {
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.CANNOT_DELETE+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }
    private static XCAPResult conflictNOTUTF8(){
        XCAPResult result = new XCAPResult();
        result.setStatusCode(HttpServletResponse.SC_CONFLICT);
        StringBuilder content = new StringBuilder(ERROR_DOCUMENT_PREFIX);
        content.append("<"+XDMSConstants.NOT_UTF_8+"/>");
        content.append(ERROR_DOCUMENT_SUFFIX);
        result.setBody(content.toString());
        result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
        return result;
    }
    /*
    * Create Response for successful request processing.
    * The statuscode should be 2XX. E.g. 200, 201
    */
    public static XCAPResult newResultForOK(int statuscode){
        XCAPResult result = new XCAPResult();
        result.setStatusCode(statuscode);
        return result;
    }
    
    public static XCAPResult newResultForOK(int statuscode, String eTag){
        XCAPResult result = new XCAPResult();
        result.setStatusCode(statuscode);
        result.addHeader(XDMSConstants.HEADER_ETAG,eTag);
        return result;
    }
    public static XCAPResult newResultForGetOK(Resource resource,String eTag){
        XCAPResult result = new XCAPResult();
    	try{
	        result.setStatusCode(HttpServletResponse.SC_OK);
	        result.setMimeType(resource.getMimetype());
	        result.setBody(resource.toXML());
	        result.addHeader(XDMSConstants.HEADER_ETAG,eTag);
    	}catch(Throwable t){
    		log.error("newResultForGetOK ERROR: " + t);
    		return result;
    	}
        return result;
    }
    
    public static XCAPResult newResultForGetOK(Resource resource){
        XCAPResult result = new XCAPResult();
    	try{
	        result.setStatusCode(HttpServletResponse.SC_OK);
	        result.setMimeType(resource.getMimetype());
	        result.setBody(resource.toXML());
    	}catch(Throwable t){
    		log.error("newResultForGetOK ERROR: " + t);
    		return result;
    	}
        return result;
    }
    
    // creates an XCAPResult with a statuscode and a error message
    // these are no standard xcap-errors!
    public static XCAPResult newResultForOtherError(int statuscode , String error ){
    	 XCAPResult result = new XCAPResult();
         result.setStatusCode(statuscode);
         StringBuilder content = new StringBuilder("<error>" +"\n");
         content.append("< " + error + " />" + "\n");
         content.append("</error>");
         result.setBody(content.toString());
         result.setMimeType(XDMSConstants.MIME_TYPE_CONFLICT);
         return result;
    }
}
