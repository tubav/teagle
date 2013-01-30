package de.tub.av.pe.context;

public class ConfigurationException extends Exception{

	private static final long serialVersionUID = 2825567812814856988L;

	public ConfigurationException(String msg)
	{
		super(msg);
	}
	
	public ConfigurationException(String msg, Throwable e)
	{
		super(msg, e);
	}
	
}
