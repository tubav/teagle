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

import java.util.HashMap;


public class XCAPResult {
    private int statusCode;
    private String body;
    private String mimeType;
    private HashMap<String,String> headers;

    public XCAPResult() {
        statusCode = 0;
        body = null;
        headers = new HashMap<String,String>();
    }

    public XCAPResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        headers = new HashMap<String,String>();
        mimeType = null;
    }

    public void addHeader(String headerName, String headerValue){
        headers.put(headerName,headerValue);
    }

    public boolean containedEtag(){
        boolean contained = false;
        if(headers.containsKey(XDMSConstants.HEADER_ETAG)){
            contained = true;
        }
        return contained;
    }

    public int getStatusCode() {
        return statusCode;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEtag(){
        String result = null;
        if(containedEtag())
              result = headers.get(XDMSConstants.HEADER_ETAG);
        return result;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
