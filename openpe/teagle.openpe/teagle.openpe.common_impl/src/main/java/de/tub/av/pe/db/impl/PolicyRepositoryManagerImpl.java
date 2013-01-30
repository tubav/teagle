package de.tub.av.pe.db.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.PolicyRepositoryManager;

public class PolicyRepositoryManagerImpl implements PolicyRepositoryManager {

	Map<String, PolicyRepository> repositories = new HashMap<String, PolicyRepository>();
	PolicyRepository latestInstance = null;

	@Override
	public synchronized void config(Properties props)
			throws ConfigurationException {
		String type = props.getProperty("openpe.db.type");
		if (type != null) {
			latestInstance = repositories.get(type);
			if(latestInstance != null)
				latestInstance.config(props);
		}
	}

	@Override
	public synchronized void config(Properties props, DataSource datasource)
			throws ConfigurationException {
		String type = props.getProperty("openpe.db.type");
		if (type == null) 
			throw new ConfigurationException("Property openpe.db.type is missing");
		latestInstance = repositories.get(type);
		if(latestInstance == null)	
			throw new ConfigurationException("Repository of type "+type+" was not registered");
		latestInstance.config(props, datasource);
	}

	@Override
	public synchronized PolicyRepository getInstance() {
		return latestInstance;
	}

	@Override
	public synchronized void register(PolicyRepository pRepo) {
		repositories.put(pRepo.getRepositoryType(), pRepo);
	}

	@Override
	public synchronized void unregister(String type) {
		repositories.remove(type);
	}

}
