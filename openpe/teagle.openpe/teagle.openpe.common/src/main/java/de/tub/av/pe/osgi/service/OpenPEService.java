package de.tub.av.pe.osgi.service;

import java.util.List;
import java.util.Map;

import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.pe.PolicyEvaluation;
import de.tub.av.pe.rulecontext.RuleActionsRegistry;

public interface OpenPEService {
	PolicyEvaluation getPolicyEvaluation();

	RuleActionsRegistry getActionsRegistry();

	boolean eval(String originator, String originatorType,
			List<String> targets, String targetType, String eventname,
			Map<String, List<Object>> parameters);

	OpenPEContextManager getOpenPEContextManager();
}
