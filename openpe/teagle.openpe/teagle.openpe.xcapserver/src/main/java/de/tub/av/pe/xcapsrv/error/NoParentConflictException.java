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

package de.tub.av.pe.xcapsrv.error;

public class NoParentConflictException extends ConflictException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String conflictError = null;
    private String existingAncestor = null;
    private String queryComponent = null;
    private String schemeAndAuthorityURI = null;

    public NoParentConflictException(String existingAncestor) {
        if (existingAncestor == null) {
            throw new IllegalArgumentException("existing ancestor must not be null");
        }
        this.existingAncestor = existingAncestor;
    }

    public void setQueryComponent(String queryComponent) {
        this.queryComponent = queryComponent;
    }

    public void setSchemeAndAuthorityURI(String schemeAndAuthorityURI) {
        this.schemeAndAuthorityURI = schemeAndAuthorityURI;
    }

    protected String getConflictError() {
        if (conflictError == null) {
            if (schemeAndAuthorityURI != null) {
                StringBuilder sb = new StringBuilder("<no-parent><ancestor>").append(schemeAndAuthorityURI);
                if (!existingAncestor.equals("")) {
                    sb.append('/').append(existingAncestor);
                }
                if (queryComponent != null) {
                    sb.append('?').append(queryComponent);
                }
                sb.append("</ancestor></no-parent>");
                conflictError = sb.toString();
            } else {
                return "<parent />";
            }
        }
        return conflictError;
    }

}
