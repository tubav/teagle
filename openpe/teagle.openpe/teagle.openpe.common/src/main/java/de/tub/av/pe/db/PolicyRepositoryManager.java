package de.tub.av.pe.db;

import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;

public interface PolicyRepositoryManager {

	public PolicyRepository getInstance();

	public void register(PolicyRepository pRepo);

	public void unregister(String type);

	void config(Properties props, DataSource datasource) throws ConfigurationException;

	void config(Properties props) throws ConfigurationException;
}
