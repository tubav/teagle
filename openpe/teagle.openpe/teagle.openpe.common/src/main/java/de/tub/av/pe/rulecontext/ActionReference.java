package de.tub.av.pe.rulecontext;


public abstract class ActionReference 
{
	protected ActionDescriptionModel description;
	
	protected ActionEnforcerModel enforcer;
	
		
	public ActionDescriptionModel getActionDescription()
	{
		return description;
	}
	
	public ActionEnforcerModel getActionEnforcer()
	{
		return enforcer;
	}
	
}
