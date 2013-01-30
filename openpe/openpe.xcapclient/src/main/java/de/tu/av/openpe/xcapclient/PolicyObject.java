package de.tu.av.openpe.xcapclient;

import gen.openpe.identifiers.policy.PolicyIdentifier;

public class PolicyObject {

	private String policyContent;
	private PolicyIdentifier pi;

	public PolicyObject()
	{
	}

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
}
