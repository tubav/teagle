package de.tub.av.pe.identities.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.IdentityRepositoryManager;

public class IdentityRepositoryManagerImpl implements IdentityRepositoryManager {

	private Map<String, IdentityRepository> repositories = new HashMap<String, IdentityRepository>();
	private IdentityRepository latestInstance;

	@Override
	public synchronized void config(Properties props)
			throws ConfigurationException {
		String type = props.getProperty("openpe.identities.type");
		if (type != null) {
			latestInstance = repositories.get(type);
			latestInstance.config(props);
		}
	}

	@Override
	public synchronized void config(Properties props, DataSource datasource)
			throws ConfigurationException {
		String type = props.getProperty("openpe.identities.type");
		if (type != null) {
			latestInstance = repositories.get(type);
			latestInstance.config(props, datasource);
		}
	}

	@Override
	public synchronized IdentityRepository getInstance() {
		return latestInstance;
	}

	@Override
	public synchronized void register(IdentityRepository ir) {
		repositories.put(ir.getRepositoryType(), ir);
	}

	@Override
	public synchronized void unregister(String type) {
		repositories.remove(type);
	}

}
