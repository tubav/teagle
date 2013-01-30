package de.tub.av.pe.editor.drools.impl;

import javax.servlet.ServletContext;

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.editor.PEEditor;
import de.tub.av.pe.editor.PEEditorManager;

public class PEEditorBean {

	private ServletContext config = null;
	private PEEditorManager editormanager;
	private OpenPEContextManager manager;

	public void config(ServletContext init, String attributeName) {
		if (config == null) {
			this.config = init;
			manager = (OpenPEContextManager) config.getAttribute(attributeName);
			editormanager = manager.getInstance().getPEEditorManager();
		}
	}
	public PEEditor getEditor() {
		return editormanager.getInstance();
	}

	public OpenPEContext getOpenPEContext() {
		return manager.getInstance();
	}
}
