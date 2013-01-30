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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DocumentSelector {
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DocumentSelector.class);

    private String auid = null;
    /* the structure of documentParent is: global or users/xui/treepath.
       E.g. users/sip:bob@open-ims.test/buddylist */
    private String documentParent = null;
    private String documentName = null;
    private String documentSelector = null;
    private String resourceSelector = null;
    private boolean polIdentifiersReq = false;
    private boolean eventsListReq = false;
    private boolean policyTypesReq = false;
    private boolean outputOverview = false;
    private boolean global = false;
    private String xui = null;
    
    private String nodeSelector = null;
    private boolean boolNodeSelector = false;
        
    private String policyType = null;
    private String identityScope = null;
    private String event = null;
    private String policyId = null;
    
    /**
     * Constructor of the DocumentSelector.
     * The DocumentSelector contains all parts of the selection.
     * Using the getter classes you can get auid, xui, documentname...
     * 
     * @param resourceSelector
     * @param queryComponent
     * @throws ParseException
     */
    // EXAMPLE:
    // /org.oma.groups/users/sip:john@ims.org/index.xml/~~/a:test/b:doe as resourceSelector
    // and xmlns(a=urn:test:ns1)xmlns(b=urn:test:ns2) as queryComponent (in the request divided with a ? from the resourceSelector)
    // would result in the following:
    
    // resourceSelector = /org.oma.groups/users/sip:john@ims.org/index.xml/~~/a:test/b:doe
    // documentParent = /org.oma.groups/users/sip:john@ims.org
    // documentName = index.xml
    // boolNodeSelector = true
    // nodeSelector = /a:test/b:doe
    // auid = org.oma.groups
    // documentSelector = /org.oma.groups/users/sip:john@ims.org/index.xml
    // global = false
    // xui = sip:john@ims.org
    // queryComponent = xmlns(a=urn:test:ns1)xmlns(b=urn:test:ns2)
    // the namespace map would contain (a, urn:test:ns1) and (b, urn:test:ns2) where a and b are the prefixes
    
    public DocumentSelector(String resourceSelector, String queryComponent)  throws ParseException {
    		//resourceSelector = ProcentDecoder.getInstance().decode(resourceSelector); //the resourceSelector is URL encoded - therefore there has to be this conversion
    		try {
				resourceSelector = URLDecoder.decode(resourceSelector, "UTF-8");
	    		this.resourceSelector = resourceSelector;
				this.parseResourceSelector(resourceSelector);
				if(queryComponent != null && !queryComponent.equals("")){
					queryComponent = URLDecoder.decode(queryComponent, "UTF-8");
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not decode some part of the URL: " + e.getMessage());
				throw new ParseException(e.getMessage());
			}
    }

    public DocumentSelector(String resourceSelector)throws ParseException 
    { 	
    	try {
			resourceSelector = URLDecoder.decode(resourceSelector, "UTF-8");
    		this.resourceSelector = resourceSelector;
			this.parseResourceSelector(resourceSelector);
		} catch (UnsupportedEncodingException e) {
			logger.error("Could not decode some part of the URL: " + e.getMessage());
			throw new ParseException(e.getMessage());
		}
    	
    }
    
    
    /**
     * This method parses the resourceSelector.
     * 
     * @param resourceSelector
     * 
     */
    
    private void parseResourceSelector(String resourceSelector) throws ParseException{
    	logger.debug("Resource Selector is: " + resourceSelector);
       	String temp_ds = resourceSelector;
    	
       	try {
	       	//is there a nodeSelector
	        if(temp_ds.contains("~")){
	        	boolNodeSelector = true;
	        	String[] temp = temp_ds.split("/~~");
	        	temp_ds = temp[0];
	        	this.nodeSelector = temp[1];
	        }
	        
	        if(temp_ds.endsWith("/"))
	        	temp_ds = temp_ds.substring(0,temp_ds.length()-1);
	        // check there is a leading '/'
	        if(!temp_ds.startsWith("/"))
	        	temp_ds = "/" + temp_ds;
        
            // get documentName & documentParent
            int documentNameSeparator = temp_ds.lastIndexOf("/");
            if (documentNameSeparator != -1) {
                String documentParent = temp_ds.substring(0, documentNameSeparator);
                documentName = temp_ds.substring(documentNameSeparator + 1);

                if (documentParent.charAt(0) != '/') {
                    throw new ParseException("invalid documentParent expected character: /");
                } else {
                	//get auid
                    int auidSeparator = documentParent.indexOf('/', 1);
                    if (auidSeparator > 1) {
                        String auid = documentParent.substring(1, auidSeparator);
                       
                        logger.debug("Parsed document selector includes: \n\t" +
                        		"AUID: " + auid + "\n\t" +
                        		"Document Parent Selector: " +documentParent+ "\n\t" +
                        		"Document Name: " + documentName + "\n\t");
                        
                        this.documentSelector = getDocSelector(auid, documentParent);
                    } else {
                        throw new ParseException("could not find the auid parsing the resource selector");
                    }
                }                
                parseDocumentName();
            } else {
                throw new ParseException("could not find the document name parsing the resource selector");
            }
        }
        catch (IndexOutOfBoundsException e) {
            throw new ParseException("Index out of bounds. Unexpected structure of the resource selector");
        }
    }
    
    
    private void parseDocumentName()
    {
    	String[] s = documentName.split("_");
        if(s[0].equals("policyIdentifiers"))
        {
        	this.polIdentifiersReq = true;
        	this.policyType = s[1];
        	if(s.length == 3)
        		this.policyId = s[2];
        }
        else if (s[0].equals("events"))
        {
        	this.eventsListReq = true;
        }
        else if (s[0].equals("policytypes"))
        {
        	this.policyTypesReq = true;
        }
        else if (s[0].equals("outputOverview"))
        {
        	this.outputOverview = true;
        }
        else
        {
            this.policyType = s[0];
        	this.policyId = s[1];
            this.identityScope = s[2];
            this.event = s[3];
        }
    	
    }
    
    /**
     * Auxiliary method for parseDocumentSelector.
     * 
     * @param auid
     * @param documentParent
     * @param documentName
     * @return returns the document selector
     */

    private String getDocSelector(String auid, String documentParent){
    	this.auid = auid;
    	this.documentParent = documentParent;
    	
        // kill first and last "/"
        if(auid.contains("/"))
        	this.auid = auid.replace("/", "");  
        
        if(!this.documentParent.startsWith("/"))
        	this.documentParent = "/" + this.documentParent;
        if(this.documentParent.endsWith("/"))
        	this.documentParent = this.documentParent.substring(0, this.documentParent.length()-1);
        
        if(this.documentName.startsWith("/"))
        	this.documentName = this.documentName.replaceFirst("/", "");
        if(this.documentName.endsWith("/"))
        	this.documentName = this.documentName.substring(0, documentName.length()-1);
        
        //docselector "/" + this.auid
        String dummy = this.documentParent + "/" + this.documentName;
	        
        if(this.documentParent.contains("global"))
        	this.global = true;
        else
        {
        	this.xui = parseXUI(this.documentParent);
        }
        return dummy;
    }
    
    private String parseXUI(String documentParent){
        String result = null;
        int index = documentParent.indexOf("users/");
        int endIndex = index + 6;
        if(index!=-1){
            index = documentParent.indexOf("/",endIndex);
            if(index!=-1){
            result = documentParent.substring(endIndex,index);
            }else{
                result = documentParent.substring(endIndex);
            }
        }

        return result;
    }
    
   
    
    /**
     * Escape encoding.
     * 
     * @param parent the parent
     * 
     * @return the string
     */
    private String escapeEncoding(String parent) {
        String result = null;
        int index = parent.indexOf("sip:");
        if(index!=-1){
            int slashIndex = parent.indexOf("/",index);
            if(slashIndex!=-1){
            	String encodedSIPURI = ProcentDecoder.getInstance().encode(parent.substring(index,slashIndex));
            	result = parent.substring(0,index)+encodedSIPURI+parent.substring(slashIndex);
            }else{
                String encodedSIPURI = ProcentDecoder.getInstance().encode(parent.substring(index));
                result = parent.substring(0,index)+encodedSIPURI;
            }
        }else{
            result = parent;
        }
        return result;  //To change body of created methods use File | Settings | File Templates.
    }
    
    public String getAUID() {
        return auid;
    }
    public String getDocumentName() {
        return documentName;
    }
    public String getDocumentParent() {
        return documentParent;
    }
    public String getDocumentParentESCEncoded() {
        return escapeEncoding(documentParent);
    }

    public String getIdentity(){
    	return xui;
    }
    
    public String getPolicyType()
    {
    	logger.debug("The policy type {}", this.policyType);
    	return this.policyType;
    }
    
    public String getIdentityScope()
    {
    	return this.identityScope;
    }
    
    public String getEvent()
    {
    	return this.event;
    }    
    
    public String getPolicyId()
    {
    	return this.policyId;
    }    
 
    
	public String getDocumentSelector() {
		return documentSelector;
	}
	
	public String getResourceSelector() {
		return resourceSelector;
	}

	public boolean isGlobal() {
		return global;
	}

	public String getNodeSelector() {
		return nodeSelector;
	}

	public boolean isBoolNodeSelector() {
		return boolNodeSelector;
	}
    
	public boolean isPolicyIdentifiersReq()
	{
		return this.polIdentifiersReq;
	}
	
	public boolean isEventsListReq()
	{
		return this.eventsListReq;
	}
	
	
	public boolean isPolicyTypesReq()
	{
		return this.policyTypesReq;
	}
	public boolean isOutputOverviewReq()
	{
		return this.outputOverview;
	}
	
}
