package de.fhg.fokus.ngni.openpe.action.drl.doprint;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.model.drools.DrlPEActionImpl;
import de.tub.av.pe.rulecontext.ActionEnforcerException;
import de.tub.av.pe.rulecontext.ActionEnforcerModel;
import de.tub.av.pe.rulecontext.PEAction;
import de.tub.av.pe.rulecontext.RuleContext;

public class DoPrintEnforcer extends ActionEnforcerModel{

	private Logger log = LoggerFactory.getLogger(DoPrintEnforcer.class);
	
	@Override
	public void executeAction(RuleContext ruleContext,
			PEAction action) throws ActionEnforcerException {
		Map<String, List<Object>> attrs = ((DrlPEActionImpl)action).getAttributesMap();
		
		List<Object> values = attrs.get("message");
		if (values != null)
		{
			System.out.println(values.get(0));
			log.info((String)values.get(0));
			ruleContext.getRuleEntryGUI().setActionsExecLog("Action doPrint has been enforced: "+(String)values.get(0));			
		}		
	}
}
