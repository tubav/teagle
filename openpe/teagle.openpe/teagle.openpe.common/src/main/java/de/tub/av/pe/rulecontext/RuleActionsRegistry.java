package de.tub.av.pe.rulecontext;


import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;


public interface RuleActionsRegistry {

	public void config(Properties props) throws ConfigurationException;
		
	public String getImplementationClass(String actionname);
	
	public ActionReference getActionReference(String actionname);

	void addActionReference(String actionname, Class<?> clzz);
	
	void removeActionReference(String actionname);

	void addActionReference(String actionname, ActionReference actionReference);
}
