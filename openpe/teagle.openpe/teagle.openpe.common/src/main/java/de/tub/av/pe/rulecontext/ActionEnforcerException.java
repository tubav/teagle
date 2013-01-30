package de.tub.av.pe.rulecontext;

public class ActionEnforcerException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -653062216717622274L;

	public ActionEnforcerException(String msg)
	{
		super(msg);	
	}
	
	public ActionEnforcerException(String msg, Throwable e)
	{
		super(msg, e);	
	}

}
