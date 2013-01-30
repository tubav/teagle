package de.fhg.fokus.ngni.openpe.action.drl.doprint;

import java.util.Map;

import de.tub.av.pe.rulecontext.ActionDescriptionModel;



public class DoPrintDescription implements ActionDescriptionModel{

	@Override
	public String getType() {
		return "doPrint";
	}

	@Override
	public Map<String, Frequency> getActionAttributes() {
		return null;
	}
}
