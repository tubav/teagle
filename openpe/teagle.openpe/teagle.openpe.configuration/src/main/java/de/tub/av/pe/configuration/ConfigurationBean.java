/*
 * Created by FhG FOKUS, Institute for Open Communication Systems
 * 2010
 *
 * For further information please contact Fraunhofer FOKUS 
 * via e-mail at the following address:
 *     info@fokus.fraunhofer.de
 *
 */
package de.tub.av.pe.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for java server pages (jsp)
 * 
 * @author sim
 *
 */
public class ConfigurationBean {
	
	/**
	 * 
	 */
	public static List<ServiceConfiguration> configs = Collections.synchronizedList(new ArrayList<ServiceConfiguration>());

	/**
	 * @param config
	 */
	public static void addServiceConfig(ServiceConfiguration config) {
		synchronized (configs) {
			configs.add(config);
		}
	}
	
	/**
	 * @param serviceName
	 * @return
	 */
	public static ServiceConfiguration getServiceConfiguration(String serviceName) {
		for (ServiceConfiguration conf : configs) {
			if (serviceName.equals(conf.getServiceName())) {
				return conf;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public static List<ServiceConfiguration> getConfigs() {
		return configs;
	}
		
}
