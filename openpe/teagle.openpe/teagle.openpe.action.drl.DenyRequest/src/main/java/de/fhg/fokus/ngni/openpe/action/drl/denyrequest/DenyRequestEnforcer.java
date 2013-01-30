package de.fhg.fokus.ngni.openpe.action.drl.denyrequest;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.model.drools.DrlPEActionImpl;
import de.tub.av.pe.model.drools.DrlPERuleImpl;
import de.tub.av.pe.model.drools.DrlPEActionImpl.ActionImpact;
import de.tub.av.pe.rulecontext.ActionEnforcerException;
import de.tub.av.pe.rulecontext.ActionEnforcerModel;
import de.tub.av.pe.rulecontext.PEAction;
import de.tub.av.pe.rulecontext.RuleContext;

public class DenyRequestEnforcer extends ActionEnforcerModel{

	private Logger log = LoggerFactory.getLogger(DenyRequestEnforcer.class);
	
	@Override
	public void executeAction(RuleContext ruleContext,
			PEAction peaction) throws ActionEnforcerException {
		
		DrlPEActionImpl action = (DrlPEActionImpl)peaction;
		
		Map<String, List<Object>> attrs = action.getAttributesMap();
		
		List<Object> values = attrs.get("message");
		if (values != null)
		{
			log.debug("Denied because {}", values.get(0));
			ruleContext.getRuleEntryGUI().setActionsExecLog("Action denyRequest has been enforced: "+values.get(0));				
		}
		//empty the agenda of the evaluation process
		((DrlPERuleImpl)ruleContext.getRule()).getAgenda().clear();
		action.setImpact(ActionImpact.REJECT);
	}
}
