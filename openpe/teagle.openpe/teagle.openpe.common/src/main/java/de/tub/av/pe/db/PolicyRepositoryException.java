package de.tub.av.pe.db;

public class PolicyRepositoryException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PolicyRepositoryException(String msg, Throwable e)
	{
		super(msg, e);
	}
	
	public PolicyRepositoryException(Throwable e)
	{
		super(e);
	}

	public PolicyRepositoryException(String msg)
	{
		super(msg);
	}

}
