package de.tub.av.pe.pe;

import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.rulecontext.RuleContext;

public interface PolicyEvaluation {
	
	public boolean evaluateAndEnforcePolicies(RequestContextInterface reqContext);
		
	public String getType();

	public void config(Properties props) throws ConfigurationException;

	Object evalValueOfParameter(RuleContext ruleContext,
			String parameter);

	public void init(PolicyRepository polrepo);
}

