package de.tub.av.pe.pem1.processing;

public class DenyRequestException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2662489370117830253L;

	public DenyRequestException(String msg)
	{
		super(msg);
	}

	public DenyRequestException(String msg, Throwable e)
	{
		super(msg, e);
	}

}
