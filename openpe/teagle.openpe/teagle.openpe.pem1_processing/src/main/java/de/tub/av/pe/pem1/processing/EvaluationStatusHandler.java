package de.tub.av.pe.pem1.processing;

public class EvaluationStatusHandler {

	private String msg;

	public void setMessage(String msg)
	{
		this.msg = msg;
	}

	public void genMessage(String msg)
	{
		this.msg = msg;
	}
	
	public String getMessage()
	{
		return this.msg;
	}
}
