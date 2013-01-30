package de.fhg.fokus.ngni.openpe.action.drl.denyrequest;

import de.tub.av.pe.rulecontext.ActionReference;

public class DenyRequestReference extends ActionReference{

	public DenyRequestReference()
	{
		this.description = new DenyRequestDescription();
		this.enforcer = new DenyRequestEnforcer();
		this.enforcer.setActionDescription(description);
	}

}

