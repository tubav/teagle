/*
 * Created by FhG FOKUS, Institute for Open Communication Systems
 * 2010
 *
 * For further information please contact Fraunhofer FOKUS 
 * via e-mail at the following address:
 *     info@fokus.fraunhofer.de
 *
 */
package de.tub.av.pe.editor.drools.utils;

import java.io.Serializable;

public class NavigationConstants implements Serializable{



	public String getPath() {
		return path;
	}

	public String getSee() {
		return see;
	}

	public String getPe() {
		return pe;
	}
	public String getPeeditor(){
		return peeditor;
	}

	public String getMonitoring() {
		return monitoring;
	}

	public String getSetup() {
		return setup;
	}

	public String getUm() {
		return um;
	}

	public String getSeefaq() {
		return seefaq;
	}

	public String getSeenewincoming() {
		return seenewincoming;
	}

	public String getSeeservicemanagement() {
		return seeservicemanagement;
	}

	public String getSeeworkflowservices() {
		return seeworkflowservices;
	}

	public String getSeenewinservicevalid() {
		return seenewinservicevalid;
	}

	public String getSeenewintest() {
		return seenewintest;
	}

	public String getSeeservicemanagementregistry() {
		return seeservicemanagementregistry;
	}

	public String getSeeservicemanagementrepository() {
		return seeservicemanagementrepository;
	}

	public String getSeeservicemanagementdiscovery() {
		return seeservicemanagementdiscovery;
	}

	public String getSetupsee() {
		return setupsee;
	}
	public String getSetuppe(){
		return setuppe;
	}
	public String getSetupum(){
		return setupum;
	}
	public String getSetupmon(){
		return setupmon;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NavigationConstants(){};
	

	public final String path = "content/";
	public final String see = path+ "SEE/";
	public final String pe = path+ "PE/";
	public final String peeditor = pe + "editor/";
	public final String monitoring =path+  "Monitoring/";
	public final String setup = path+ "Setup/";
	public final String um = path+ "UM/";
	
	public final String seefaq = see + "faq/";
	public final String seenewincoming = see + "NewIncomingServices/";
	public final String seeservicemanagement = see + "ServiceManagement/";
	public final String seeworkflowservices = see + "WorkflowServices/";
	public final String seenewinservicevalid = seenewincoming
			+ "ServiceValidation/";
	public final String seenewintest = seenewincoming + "Test/";
	public final String seeservicemanagementregistry = seeservicemanagement
			+ "ServiceRegistry/";
	public final String seeservicemanagementrepository = seeservicemanagement
			+ "ServiceRepository/";
	public final String seeservicemanagementdiscovery = seeservicemanagement
			+ "ServiceDiscovery/";
	
	public final String setupsee = setup + "SetupSEE/";
	public final String setuppe = setup + "SetupPE/";
	public final String setupum = setup + "SetupUM/";
	public final String setupmon = setup +"SetupMonitoring/";
}
