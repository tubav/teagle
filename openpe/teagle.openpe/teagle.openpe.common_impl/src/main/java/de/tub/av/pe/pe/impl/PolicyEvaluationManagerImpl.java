package de.tub.av.pe.pe.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.pe.PolicyEvaluation;
import de.tub.av.pe.pe.PolicyEvaluationManager;

public class PolicyEvaluationManagerImpl implements PolicyEvaluationManager {

	private Map<String, PolicyEvaluation> evaluations = new HashMap<String, PolicyEvaluation>();
	private PolicyEvaluation latestinstance;

	@Override
	public synchronized void config(Properties props) throws ConfigurationException {
		String type = props.getProperty("openpe.policyevaluation.type");
		if (type == null)
			throw new ConfigurationException("Property openpe.policyevaluation.type is missing");
		latestinstance = evaluations.get(type);
		if (latestinstance == null)
			throw new ConfigurationException("There is no PolicyEvaluation of type "+ type);
		latestinstance.config(props);
	}

	
	@Override
	public synchronized PolicyEvaluation getInstance() {
		return latestinstance;
	}

	@Override
	public synchronized void register(PolicyEvaluation poleval) {
		evaluations.put(poleval.getType(), poleval);

	}

	@Override
	public synchronized void unregister(String type) {
		evaluations.remove(type);
	}
}
