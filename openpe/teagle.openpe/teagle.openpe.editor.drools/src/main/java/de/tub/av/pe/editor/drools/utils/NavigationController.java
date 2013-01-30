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

public class NavigationController {

	String SEE = "SEE";
	String PE = "PE";
	String UM = "UM";
	String Monitoring = "Monitoring";
	String Setup = "Setup";
	
	
	public NavigationController(){
		
	}
	
	public String getSelected(String navi, String subnavi){
		
		String returnString = "";
		if(navi!=null){
			if(navi.equals("main") || navi.equals("ServiceManagement") ||  navi.equals("ComposedServices")|| navi.equals("ServiceValidation") || navi.equals("FAQ") || navi.equals("ValidationWorkflow") || navi.equals("BundleDescription") || navi.equals("SCXMLDescription")){
			
			
				returnString = SEE;
			}
			else if(navi.equals("Setup") || navi.equals("SetupSEE") || navi.equals("SetupPE")|| navi.equals("SetupMonitoring") || navi.equals("SetupUM")) {
				returnString =Setup;
			}
			else if(navi.equals("PE") || navi.equals("PolicyEditor")|| navi.equals("newCategoryData") || navi.equals("categoryEdit")){
				returnString = PE;
			}
			else if(navi.equals("Monitoring")){
				returnString = Monitoring;
			}
			else if(navi.equals("UM")){
				returnString = UM;
			}
		}
		return returnString;
	}
}
