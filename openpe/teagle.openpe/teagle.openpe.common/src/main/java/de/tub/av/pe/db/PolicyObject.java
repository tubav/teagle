package de.tub.av.pe.db;


import de.tub.av.pe.rulecontext.PERuleset;

/**
 * Class that throughly describes the policy object as it is stored in the db 
 * @author ibo
 *
 */
public class PolicyObject{

	private String policyContent;	
	private PolicyIdentifier pi;
	
	public void setPolicyIdentifier(PolicyIdentifier pi)
	{
		this.pi = pi;
	}
	
	public PolicyIdentifier getPolicyIdentifier()
	{	
		return pi;
	}
	 
	public void setPolicyContent(String policyContent)
	{		
		this.policyContent = policyContent;
	}
	
	public String getPolicyContent()
	{
		return this.policyContent;
	}
		
	public String getEtag()
	{
		StringBuilder strb = new StringBuilder();		
		strb.append(pi.toString());
		strb.append(policyContent);
		return new Integer(strb.hashCode()).toString();		
	}
		
	public PERuleset getPERuleset()
	{
		return null;
	}	
}
