package de.tub.av.pe.editor;

import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;

public interface PEEditorManager {
	
	public void config(Properties props) throws ConfigurationException;

	public PEEditor getInstance();

	public void register(PEEditor peeditor);

	public void unregister(String type);
}
