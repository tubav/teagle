package de.tub.av.pe.configuration;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.configuration.ServiceConfiguration.Parameter;
import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.OpenPEContextManager;

public class OpenPEConfigurationListener implements
		ServiceConfigurationListener {
	private OpenPEContextManager factory;
	private Logger log = LoggerFactory.getLogger(OpenPEConfiguration.class);

	public void init(OpenPEContextManager factory) {
		this.factory = factory;
	}

	@Override
	public void onParameterChanged(ServiceConfiguration config,
			Parameter parameter) {
	}

	@Override
	public void onSaved(ServiceConfiguration config) {
		Properties props = new Properties();
		List<Parameter> parameterList = config.getParameters();
		for (Parameter parameter : parameterList) {
			props.put(parameter.getName(), parameter.getValue());
		}
		config(props);
	}

	@Override
	public void onInit(ServiceConfiguration config) {
		Properties props = new Properties();
		List<Parameter> parameterList = config.getParameters();
		for (Parameter parameter : parameterList) {
			props.put(parameter.getName(), parameter.getValue());
		}
		config(props);
	}

	private void config(Properties props) {
		try {
			OpenPEContext openpeContext = factory.getInstance();
			openpeContext.getActionsRegistry().config(props);
			openpeContext.configRepos(props);
			openpeContext.getPolicyEvaluationManager().getInstance()
					.config(props);
			openpeContext.getPEEditorManager().getInstance().config(props);
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
		}
	}
}
