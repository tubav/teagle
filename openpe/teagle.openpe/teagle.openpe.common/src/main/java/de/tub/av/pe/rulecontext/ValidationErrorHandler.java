package de.tub.av.pe.rulecontext;

public class ValidationErrorHandler {
	private String errorMessage;
	
	public void error(String message)
	{
		errorMessage = message;
	}
	
	public String getError()
	{
		return this.errorMessage;
	}
	
}
