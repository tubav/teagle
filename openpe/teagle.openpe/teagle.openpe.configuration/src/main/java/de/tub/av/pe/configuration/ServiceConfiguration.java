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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author sim
 * 
 */
public class ServiceConfiguration {

	private String serviceName;
	/**
	 * 
	 */
	private List<Parameter> parameters = new ArrayList<Parameter>();

	private String configDir;

	private List<ServiceConfiguration> subconfigs = new ArrayList<ServiceConfiguration>();

	private List<ServiceConfigurationListener> listeners = Collections.synchronizedList(new ArrayList<ServiceConfigurationListener>());
	
	private Logger log = LoggerFactory.getLogger(ServiceConfiguration.class);
	
	public ServiceConfiguration() {
	}

	/**
	 * @param serviceName
	 */
	protected ServiceConfiguration(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @param configDir
	 */
	public void init(String configDir) {
		
		if(configDir == null || configDir.equals("")){
			this.configDir = "config";
		}
		else{	
			this.configDir = configDir;
		}
		load();
		fireOnInit();
	}

	public void init(Properties properties) {
		transferProperties(properties);
		onInit();
		fireOnInit();
	}

	/**
	 * @return
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param listener
	 */
	public void addListener(ServiceConfigurationListener listener) {
		synchronized (listener) {
			listeners.add(listener);
		}
	}

	/**
	 * @param listener
	 */
	public void removeListener(ServiceConfigurationListener listener) {
		synchronized (listener) {
			listeners.remove(listener);
		}
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	/**
	 * @param name
	 * @param value
	 * @param label
	 * @param description
	 */
	protected void addStringParameter(String name, String value, String label, String description) {
		synchronized (parameters) {
			parameters.add(new Parameter(name, value, label, description, "String", this));
		}
	}

	/**
	 * @param name
	 * @param value
	 * @param label
	 * @param description
	 */
	protected void addIntParameter(String name, Integer value, String label, String description) {
		synchronized (parameters) {
			parameters.add(new Parameter(name, value, label, description, "Integer", this));
		}
	}

	/**
	 * @param name
	 * @param value
	 * @param label
	 * @param description
	 */
	protected void addBooleanParameter(String name, boolean value, String label, String description) {
		synchronized (parameters) {
			parameters.add(new Parameter(name, Boolean.valueOf(value), label, description, "Boolean", this));
		}
	}

	/**
	 * @param config
	 */
	public void addServiceConfig(ServiceConfiguration config) {
		subconfigs.add(config);
	}

	/**
	 * 
	 */
	public void clearServiceConfigs() {
		subconfigs.clear();
	}

	/**
	 * @return
	 */
	public List<ServiceConfiguration> getServiceConfigs() {
		return subconfigs;
	}

	/**
	 * 
	 */
	public void load() {
		File file = new File(this.getLocation());

		if (!file.exists()) {
			try {
				// transfer parameters -> properties
				Properties props = new Properties();
				synchronized (parameters) {
					for (Parameter parameter : parameters) {
						props.setProperty(parameter.getName(), parameter.toString());
					}
				}
				props.store(new FileOutputStream(this.getLocation()), serviceName + " configuration properties");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				log.error("Exception saving properties", e);
			} catch (IOException e) {
				e.printStackTrace();
				log.error("Exception saving properties", e);
			}
		} else {
			try {
				Properties props = new Properties();
				props.load(new FileInputStream(file));
				transferProperties(props);
			} catch (IOException e) {
				log.error("Exception loading properties", e);
			}
			// transfer properties -> parameters
		}
	}

	private String getLocation() {
		try{
		String location = serviceName.toLowerCase() + ".properties";
		location = location.replace(' ', '_');
		
		File brokerDir = new File(configDir);
		if (!brokerDir.exists()) {
			boolean res = brokerDir.mkdirs();
			System.out.println("Create config dir: " +configDir +" "+ res);
		}
		if (brokerDir.isDirectory()) {
			location = configDir + "/" + location;
		}
		System.out.println("It writes at "+ location);
		return location;
		}catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void transferProperties(Properties props) {
		// transfer properties -> parameters
		synchronized (parameters) {
			for (Parameter parameter : parameters) {
				String value = props.getProperty(parameter.getName(), parameter.toString());
				parameter.setValue(value);
			}
		}
	}

	/**
	 * 
	 */
	public void postSave() {
		synchronized (parameters) {
			for (Parameter parameter : parameters) {
				if (parameter.getType().equals("Boolean")) {
					parameter.setValue(Boolean.FALSE);
				}
			}
		}
	}

	public final void save() {
		doSave();
		fireConfigurationSaved();
	}

	protected void onInit() {

	}

	protected void doSave() {
		Properties props = new Properties();

		// transfer parameters -> properties
		synchronized (parameters) {
			for (Parameter parameter : parameters) {
				props.setProperty(parameter.getName(), parameter.toString());
			}
		}
		try {
			props.store(new FileOutputStream(this.getLocation()), serviceName + " configuration properties");
		} catch (IOException e) {
			log.debug("Exception saving configuration data", e);
		}	
	}

	protected void onLoad() {

	}

	/**
	 * Save the new props to the current parameters.
	 * 
	 * @param props
	 */
	public void save(Properties props) {
		this.transferProperties(props);
		fireConfigurationSaved();
	}

	/**
	 * @param name
	 * @return
	 */
	public Parameter getParameter(String name) {
		synchronized (parameters) {
			for (Parameter parameter : parameters) {
				if (parameter.getName().equals(name)) {
					return parameter;
				}
			}
		}
		return null;
	}

	private void fireConfigurationSaved() {
		ServiceConfigurationListener[] listeners = getListenerArray();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onSaved(this);
		}
	}

	private void fireConfigurationParameterChanged(Parameter parameter) {
		ServiceConfigurationListener[] listeners = getListenerArray();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onParameterChanged(this, parameter);
		}
	}

	protected void fireOnInit() {
		ServiceConfigurationListener[] listeners = getListenerArray();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].onInit(this);
		}
	}

	private ServiceConfigurationListener[] getListenerArray() {
		synchronized (listeners) {
			return (ServiceConfigurationListener[]) listeners.toArray(new ServiceConfigurationListener[listeners.size()]);
		}
	}
	
	public String getBaseConfigFolder(){
		return "config";
	}

	/**
	 * @author sim
	 * 
	 */
	public class Parameter {
		private String name;
		private String label;
		private String type;
		private Object value;
		private String description;

		private ServiceConfiguration config;

		private Parameter(String name, Object value, String label, String description, String type, ServiceConfiguration config) {
			this.name = name;
			this.value = value;
			this.label = label;
			this.description = description;
			this.type = type;

			this.config = config;
		}

		/**
		 * @return
		 */
		public Object getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return value == null ? "" : value.toString();
		}

		/**
		 * @param value
		 */
		public void setValue(Object value) {
			this.value = value;
			config.fireConfigurationParameterChanged(this);
		}

		/**
		 * @param value
		 */
		public void setValue(String value) {
			if (type.equals("Integer")) {
				this.value = value.equals("") ? null : Integer.valueOf(value);
			} else if (type.equals("Boolean")) {
				this.value = Boolean.valueOf(value);
			} else if (type.equals("String")) {
				this.value = value;
			}
			config.fireConfigurationParameterChanged(this);
		}

		/**
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @return
		 */
		public String getType() {
			return type;
		}

		/**
		 * @return
		 */
		public String getDescription() {
			return description;
		}

	}

}
