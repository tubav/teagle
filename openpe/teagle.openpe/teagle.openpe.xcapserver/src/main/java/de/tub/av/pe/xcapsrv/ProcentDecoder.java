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

import java.util.Hashtable;

public class ProcentDecoder {
	
    public static final String[] ESCAPE_CODE = {"%20", "%3C", "%3E", "%23", "%25", "%7B", "%7D", "%7C",
            "%5C", "%5E", "%7E", "%5B", "%5D", "%60", "%3B", "%2F", "%3F",
            "%3A", "%40", "%3D", "%26", "%24", "%22"};
    public static final String[] CHARACTERS = {" ", "<", ">", "#", "%", "{", "}", "|",
    		"\\", "^", "~", "[", "]", "'", ";", "/", "?",
            ":", "@", "=", "&", "$", "\""};
    public static ProcentDecoder me;
    public static Hashtable<String, String> demappings;
    public static Hashtable<String, String> enmappings;

    public synchronized static ProcentDecoder getInstance() {
        if (me == null)
            me = new ProcentDecoder();
        return me;
    }

    public ProcentDecoder() {
        demappings = new Hashtable<String, String>();
        enmappings = new Hashtable<String, String>();
        for (int i = 0; i < ESCAPE_CODE.length; i++) {
            demappings.put(ESCAPE_CODE[i], CHARACTERS[i]);
            enmappings.put(CHARACTERS[i],ESCAPE_CODE[i]);
        }
    }

    /*
    *substitue all the escape codes contained in the specific string with the corresponding characters.
    * And return the decoded string.
    */
    
    public String decode(String str) {
        StringBuffer result = new StringBuffer();
        int i = 0;
        int fragEnd;
        String strFragment;
        for (int j = str.indexOf("%", i); j != -1 && i < str.length(); j = str.indexOf("%", ++i)) {
            result.append(str.substring(i, j));
            fragEnd = j + 2;
            if (fragEnd >= str.length()) {
                result.append(str.substring(j, str.length()));
                i = str.length();
                break;
            } else {
                strFragment = str.substring(j, j + 3);
                if (demappings.containsKey(strFragment.toUpperCase())) {
                    strFragment = demappings.get(strFragment);
                    result.append(strFragment);
                    i = j + 2;
                } else {
                    result.append("%");
                    i = j;
                }
            }
        }
        if (i < str.length()) {
            result.append(str.substring(i));
        }
        return result.toString();
    }
    
    /* substitue all special characters with escape codes*/
    public String encode(String str){
       StringBuffer result = new StringBuffer();
       for(int i=0;i<str.length();i++){
           String current = str.substring(i,i+1);
           if(enmappings.containsKey(current)){
               result.append(enmappings.get(current));
           }else{
               result.append(current);
           }
       }
       return result.toString();
    }
    
    /**
     * encodes an URI with Procent-encoding but leaves Special Chars like ? in the URI
     * 
     * @param str
     * @return the encoded String
     */
    
    public String encodeURI(String str){
    	String no_encoding = "@?=~"; //characters that should be left in the URI
    	
        StringBuffer result = new StringBuffer();
        for(int i=0;i<str.length();i++){
            String current = str.substring(i,i+1);
            if(enmappings.containsKey(current) && !no_encoding.contains(current)){
                result.append(enmappings.get(current));
            }else{
                result.append(current);
            }
        }
        return result.toString();
     }

}
