package de.tub.av.pe.identities;


import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;


public interface IdentityRepository {

	public List<IdentityObject> getIdentities(String identityType) throws IdentityRepositoryException;
		
	public int addIdentity(String identityType, String identity) throws IdentityRepositoryException, DuplicateValueException;

	public void deleteIdentity(String identityType, String identity) throws IdentityRepositoryException;

	public void deleteIdentity(String identityType, int id) throws IdentityRepositoryException;

	public void config(Properties props) throws ConfigurationException;

	public void config(Properties props, DataSource datasource) throws ConfigurationException;
	
	public String getRepositoryType();

	String getIdentity(String identityType, int id) throws IdentityRepositoryException;

	int getIdentityId(String identityType, String name) throws IdentityRepositoryException;

	void updateIdentity(IdentityObject io) throws DuplicateValueException, IdentityRepositoryException;

	List<IdentityObject> getIdentities(String identityType, int start,
			int length) throws IdentityRepositoryException;

	int getIdentitiesCount(String identityType) throws IdentityRepositoryException;

	void updateSecondIdentities(IdentitiesRelation relation)
			throws IdentityRepositoryException;

	List<IdentityObject> getSecondIdentities(IdentitiesRelation relation)
			throws IdentityRepositoryException;

	List<String> getSecondIdentitiesTypeList(String identityType);

}

