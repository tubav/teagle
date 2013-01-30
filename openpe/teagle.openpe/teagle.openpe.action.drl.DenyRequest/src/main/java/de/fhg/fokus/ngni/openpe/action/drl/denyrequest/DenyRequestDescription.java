package de.fhg.fokus.ngni.openpe.action.drl.denyrequest;

import java.util.Map;

import de.tub.av.pe.rulecontext.ActionDescriptionModel;



public class DenyRequestDescription implements ActionDescriptionModel{

	@Override
	public String getType() {
		return "denyRequest";
	}

	@Override
	public Map<String, Frequency> getActionAttributes() {
		return null;
	}
}
