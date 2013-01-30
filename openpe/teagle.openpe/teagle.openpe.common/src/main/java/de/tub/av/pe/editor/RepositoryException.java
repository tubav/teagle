package de.tub.av.pe.editor;

public class RepositoryException extends Exception {

	/**
	 * automatically generated serial UID 
	 */
	private static final long serialVersionUID = -7724926711238634031L;

	public RepositoryException(String message)
	{
		super(message);
	}
	
	public RepositoryException(String message, Throwable e)
	{
		super(message, e);		
	}

	public RepositoryException(Throwable e)
	{
		super(e);		
	}

}
