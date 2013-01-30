package de.tub.av.pe.identities;

public class IdentityObject {

	private String type;
	private String name;
	private int dbId;
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setId(int dbId)
	{
		this.dbId = dbId;
	}
	
	public int getId()
	{
		return dbId;
	}
	
	
	public void setName(String name)
	{
		this.name= name;
	}
	public String getName()
	{
		return name;
	}

}
