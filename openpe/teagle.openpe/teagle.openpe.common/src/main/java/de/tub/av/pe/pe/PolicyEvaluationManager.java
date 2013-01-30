package de.tub.av.pe.pe;

import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;

public interface PolicyEvaluationManager {

	public void config(Properties props) throws ConfigurationException;

	public PolicyEvaluation getInstance();

	public void register(PolicyEvaluation poleval);

	public void unregister(String type);
}
