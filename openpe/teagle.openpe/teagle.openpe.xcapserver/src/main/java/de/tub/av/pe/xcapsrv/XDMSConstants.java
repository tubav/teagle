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

/**
 * This class supplies the XDMS with static variables needed.
 * 
 */
public class XDMSConstants {

    public static final String HEADER_ALLOW = "Allow";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_IF_MATCH = "If-Match";
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_PUT = "PUT";
    public static final String REQUEST_METHOD_DELETE = "DELETE";

    //error elements for the conflict content
    public static final String NO_WELL_FORMED = "no-well-formed";
    public static final int NO_WELL_FORMED_INDEX = 1;
    public static final String NOT_XML_FRAG = "not-xml-frag";
    public static final int NOT_XML_FRAG_INDEX = 2;
    public static final String NO_PARENT = "no-parent";
    public static final int NO_PARENT_INDEX = 3;
    public static final String SCHEMA_VALIDATION_ERROR = "schema-validation-error";
    public static final int SCHEMA_VALIDATION_ERROR_INDEX = 4;
    public static final String NOT_XML_ATT_VALUE = "not-xml-att-value";
    public static final int NOT_XML_ATT_VALUE_INDEX = 5;
    public static final String CANNOT_INSERT = "cannot-insert";
    public static final int CANNOT_INSERT_INDEX = 6;
    public static final String CANNOT_DELETE = "cannot-delete";
    public static final int CANNOT_DELETE_INDEX = 7;
    public static final String UNIQUENESS_FAILURE = "uniqueness-failure";
    public static final int UNIQUENESS_FAILURE_INDEX = 8;
    public static final String CONSTRAINT_FAILURE = "constraint-failure";
    public static final int CONSTRAINT_FAILURE_INDEX = 9;
    public static final String EXTENSION = "extension";
    public static final int EXTENSION_INDEX = 10;
    public static final String NOT_UTF_8 = "not-utf-8";
    public static final int NOT_UTF_8_INDEX = 11;

    public static final String MIME_TYPE_CONFLICT = "application/xcap-error+xml";

    public static final String XCAP_DIFF_XMLNS = "urn:ietf:params:xml:ns:xcap-diff";
    
    //Resource Type for XCAPDiffResource - XMLParser determines the type of the changed Resource after a PUT/DELETE
    //so the XCAPDiffResource can be created properly
    public static final String RESOURCE_TYPE_ELEMENT = "Element";
    public static final String RESOURCE_TYPE_ATTRIBUTE = "Attribute";
    public static final String RESOURCE_TYPE_Namespace = "Namespace";
    
    //variables to determine the change in a document for SIP Notification
    public static final String RESOURCE_CHANGE_ADD = "add";
    public static final String RESOURCE_CHANGE_REPLACE = "replace";
    public static final String RESOURCE_CHANGE_REMOVE = "remove";
    
}
