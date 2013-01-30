package de.tu.av.openpe.xcapclient;

public class RepositoryException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RepositoryException(String msg, Throwable e)
	{
		super(msg, e);
	}
	
	public RepositoryException(Throwable e)
	{
		super(e);
	}

	public RepositoryException(String msg)
	{
		super(msg);
	}

}
