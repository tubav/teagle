package de.tub.av.pe.eval.drools;

import java.util.Map;

import org.drools.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.eval.drools.DrlPolicyEvaluationImpl.EnforcementResult;
import de.tub.av.pe.model.drools.DrlPEActionImpl;
import de.tub.av.pe.model.drools.DrlPEActionImpl.ActionImpact;
import de.tub.av.pe.rulecontext.ActionEnforcerException;
import de.tub.av.pe.rulecontext.ActionReference;
import de.tub.av.pe.rulecontext.RuleActionsRegistry;
import de.tub.av.pe.rulecontext.RuleContext;


public class DrlActionsManager {

	private RuleActionsRegistry actionsRegistry;

	private Logger log = LoggerFactory.getLogger(DrlActionsManager.class);

	private Map<String, RuleContext> ruleContextMap;

	private EnforcementResult enforcementResult = EnforcementResult.ALLOW;
	
	public DrlActionsManager(RuleActionsRegistry actionsRegistry, Map<String, RuleContext> ruleContextMap) {
		this.actionsRegistry = actionsRegistry;
		this.ruleContextMap = ruleContextMap;
	}
	

	public void execute(Rule rule, DrlAction action) {
		ActionReference actRef = this.actionsRegistry.getActionReference(action.getName());

		if(actRef == null)
		{
			log.error ("Action Reference of type {} could not be found", action.getName());
			return;
		}

		DrlPEActionImpl drlaction = new DrlPEActionImpl();
		drlaction.setName(action.getName());
		drlaction.setAttributesMap(action.getAttributesMap());
		try {
			String key = rule.getPackageName()+"/"+rule.getName();
			actRef.getActionEnforcer().executeAction(this.ruleContextMap.get(key), drlaction);				
			ActionImpact impact = drlaction.getImpact();
			if(impact == ActionImpact.REJECT)
			{
				synchronized (this) {
					enforcementResult = EnforcementResult.DENY;
				}
			}
		} catch (ActionEnforcerException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public EnforcementResult getEnforcementResult()
	{
		synchronized (this) {
			return this.enforcementResult;
		}
	}
}
