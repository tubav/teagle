package de.fhg.fokus.ngni.openpe.action.drl.doprint;

import de.tub.av.pe.rulecontext.ActionReference;

public class DoPrintReference extends ActionReference{

	public DoPrintReference()
	{
		this.description = new DoPrintDescription();
		this.enforcer = new DoPrintEnforcer();
		this.enforcer.setActionDescription(description);
	}

}

