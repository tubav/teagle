package de.tub.av.pe.editor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.editor.PEEditor;
import de.tub.av.pe.editor.PEEditorManager;

public class PEEditorManagerImpl implements PEEditorManager {
	private Map<String, PEEditor> editors = new HashMap<String, PEEditor>();
	private PEEditor latestinstance;

	@Override
	public synchronized void config(Properties props)
			throws ConfigurationException {
		String type = props.getProperty("openpe.editor.type");
		if (type == null)
			throw new ConfigurationException(
					"The property openpe.editor.type is missing");
		latestinstance = editors.get(type);
		if (latestinstance == null)
			throw new ConfigurationException("Instance of type " + type
					+ " was not registered");
		//in case the required property is missing, use the default value.
		if(props.getProperty("openpe.basedir") == null)
			props.put("openpe.basedir", "openpecfg");
		latestinstance.config(props);
	}

	@Override
	public synchronized PEEditor getInstance() {
		return latestinstance;
	}

	@Override
	public synchronized void register(PEEditor peeditor) {
		editors.put(peeditor.getEditorType(), peeditor);
	}

	@Override
	public synchronized void unregister(String type) {
		editors.remove(type);
	}

}
