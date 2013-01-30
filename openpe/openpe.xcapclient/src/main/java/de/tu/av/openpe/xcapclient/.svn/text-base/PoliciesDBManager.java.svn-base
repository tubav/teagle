package de.tu.av.openpe.xcapclient;

import gen.openpe.elements.policy.OutputOverviews;
import gen.openpe.identifiers.policy.PolicyIdentifier;
import java.util.List;

public interface PoliciesDBManager {

	public List<String> getAvailablePolicyTypes();

	public OutputOverviews getOutputOverviews();

	public List<PolicyIdentifier> getPoliciesIdentifiers(String identityType)
			throws RepositoryException;
		
	public PolicyIdentifier getPolicyIdentifier(String identityType, String id) throws RepositoryException;
	
	public String getPolicyContent(PolicyIdentifier pi);
	
	public void updatePolicy(PolicyIdentifier pi, String policyContent)
			throws RepositoryException;

	public void addPolicy(PolicyIdentifier pi, String policyContent)
			throws RepositoryException;

	public void deletePolicy(PolicyIdentifier pi)
			throws RepositoryException;

	public List<String> getEventsList();

	PolicyObject getPolicyObject(String identityType, String id)
			throws RepositoryException;	
}
