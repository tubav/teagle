package de.tub.av.pe.db;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.rulecontext.PERuleset;

public interface PolicyRepository {
	
	public String getRepositoryType();
	
	public List<PolicyObject> getPolicies(String identityType) throws PolicyRepositoryException;
	
	public String getPolicyContent(PolicyIdentifier pi) throws PolicyRepositoryException;
	
	public void updatePolicy(PolicyObject policyObj) throws PolicyRepositoryException, DuplicateValueException, ValidationException ;	

	public String addPolicy(PolicyObject policyObj) throws PolicyRepositoryException, DuplicateValueException, ValidationException;
	
	public void deletePolicy(PolicyIdentifier pi) throws PolicyRepositoryException ;
	
	public List<PolicyObject> getPolicies(PolicyIdentifier pi) throws PolicyRepositoryException;
	
	public List<PolicyIdentifier> getPoliciesIdentifiers(String identityType) throws PolicyRepositoryException;

	public List<PolicyIdentifier> getPoliciesIdentifiers(String identityType, int start, int length) throws PolicyRepositoryException;
	
	void config(Properties props) throws ConfigurationException;

	void config(Properties props, DataSource datasource) throws ConfigurationException;

	String getPolicyId(PolicyIdentifier pi) throws PolicyRepositoryException;

	PolicyObject getPolicyObject(String id) throws PolicyRepositoryException;

	PolicyIdentifier getPolicyIdentifier(String id)
			throws PolicyRepositoryException;

	int getPoliciesCount(String identityType) throws PolicyRepositoryException;;

	List<PERuleset> getPERulesets(PolicyIdentifier pi)
			throws PolicyRepositoryException;

	List<PERuleset> getPERulesets(int priority, RequestContextInterface reqContext)
			throws PolicyRepositoryException;

	PolicyObject getPolicy(PolicyIdentifier pi)
			throws PolicyRepositoryException;	
	
}

