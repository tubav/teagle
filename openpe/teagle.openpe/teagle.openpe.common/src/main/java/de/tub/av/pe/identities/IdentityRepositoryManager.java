package de.tub.av.pe.identities;

import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;

public interface IdentityRepositoryManager {

	public void config(Properties props)throws ConfigurationException;

	public void config(Properties props, DataSource datasource)throws ConfigurationException;

	public IdentityRepository getInstance();

	public void register(IdentityRepository ir);

	public void unregister(String type);
}
