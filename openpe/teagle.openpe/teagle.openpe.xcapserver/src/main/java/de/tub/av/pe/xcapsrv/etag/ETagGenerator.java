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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The ETag Management is utilized by XCAP Handlers to check the ETag of the document, 
 * as the conditional processing is required by the request. In addition, 
 * in case a document was modified, the ETag Management is capable of generating a new ETag for the document.
 */
public class ETagGenerator {

    /** The HAS h_ algorithm. */
    public static String HASH_ALGORITHM = "MD5";

    /** The Constant HEXCHARS. */
    private static final char[] HEXCHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * To hex string.
     * 
     * @param bytes the bytes
     * 
     * @return the string
     */
    private static String toHexString(byte[] bytes) {
        // convert each byte to hex chars
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(HEXCHARS[(bytes[i] >> 4) & 0x0f]).append(HEXCHARS[bytes[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * To generate a safe etag the document selector
     * and current time can be used as input for a digest.
     * This method creates such etag by converting each
     * digest byte to hex chars.
     * 
     * @param documentSelector the document selector part of a xcap uri
     * 
     * @return a String with the document etag.
     */
    public static String generate(String documentSelector) {
        // check argument
        if (documentSelector == null) {
            return null;
        }
        // get current time
        String date = Long.toString(System.currentTimeMillis());
        // build digest
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(documentSelector.getBytes());
            md.update(date.getBytes());
            byte[] digest = md.digest();
            // convert bytes to hex string
            return toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Generate number etag.
     * 
     * @param oldEtag the old etag
     * 
     * @return the string
     */
    public static String generateNumberETAG(String oldEtag) {

        int etag = Integer.parseInt(oldEtag);
        return Integer.toString(etag+1);
    }
}
