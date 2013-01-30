package de.tub.av.pe.context;

public class DuplicateValueException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 921560967404696597L;

	
	public DuplicateValueException(String msg)
	{
		super(msg);
	}
	public DuplicateValueException(String msg, Throwable e)
	{
		super(msg, e);
	}
}
