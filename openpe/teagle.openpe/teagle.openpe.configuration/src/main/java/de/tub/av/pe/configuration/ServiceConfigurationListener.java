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

import de.tub.av.pe.configuration.ServiceConfiguration.Parameter;

/**
 * @author sim
 * 
 */
public interface ServiceConfigurationListener {

	/**
	 * @param config
	 * @param parameter
	 */
	public void onParameterChanged(ServiceConfiguration config,
			Parameter parameter);

	/**
	 * @param config
	 */
	public void onSaved(ServiceConfiguration config);

	/**
	 * @param config
	 */
	public void onInit(ServiceConfiguration config);
}
