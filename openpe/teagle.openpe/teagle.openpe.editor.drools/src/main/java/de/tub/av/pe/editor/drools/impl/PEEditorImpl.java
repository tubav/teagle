package de.tub.av.pe.editor.drools.impl;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metaparadigm.jsonrpc.JSONRPCBridge;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.db.ValidationException;
import de.tub.av.pe.editor.EditorDataItem;
import de.tub.av.pe.editor.PEEditor;
import de.tub.av.pe.editor.RepositoryException;
import de.tub.av.pe.editor.drools.utils.PolicyDataObject;

public class PEEditorImpl implements PEEditor {
	private Logger log = LoggerFactory.getLogger(PEEditorImpl.class);

	private PolicyDataObject policyDataObject = null;

	private Map<EditorScope, EditorInterface> editors = new HashMap<EditorScope, EditorInterface>();

	private Properties props;

	private enum EditorScope {
		User, Profile, UserPolicy, ProfilePolicy
	}

	public PEEditorImpl() {
		policyDataObject = new PolicyDataObject();

		JSONRPCBridge json_bridge = JSONRPCBridge.getGlobalBridge();
		JSONMethods obj = new JSONMethods(policyDataObject);
		json_bridge.registerObject("JSONMethods", obj);

		// register all the other editors
		editors.put(EditorScope.User, new UsersEditor());
		editors.put(EditorScope.Profile, new ProfilesEditor());
		editors.put(EditorScope.UserPolicy, new UserPoliciesEditor());
		editors.put(EditorScope.ProfilePolicy, new ProfilePoliciesEditor());

	}


	@Override
	public void config(OpenPEContextManager factory) {
		Iterator<EditorInterface> it = editors.values().iterator();

		while (it.hasNext()) {
			EditorInterface editor = it.next();
			editor.config(factory);
		}
	}
	
	@Override
	public List<Map<String, String>> getDataList(String editorScope)
			throws RepositoryException {
		log.debug("Reset Cache for {}!", editorScope);

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getDataList();
	}
	@Override
	public List<Map<String, String>> getDataList(String editorScope, int start, int length)
			throws RepositoryException {
		log.debug("Reset Cache for {}!", editorScope);

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getDataList(start, length);
	}

	@Override
	public List<Map<String, String>> getDataList(
			String editorScope, int start, int length, String filterName,
			String value) throws RepositoryException {

		List<Map<String, String>> dataList = this.getDataList(editorScope);

		if (dataList != null) {
			List<Map<String, String>> newFilteredList = new ArrayList<Map<String, String>>();

			if (value.equals("")) {
				newFilteredList = dataList;
			} else {
				for (Map<String, String> data:dataList) {
					if (data.get(filterName) != null
							&& data.get(filterName).toLowerCase()
									.contains(value.toLowerCase())) {
						newFilteredList.add(data);
					}
				}
			}
			int size = newFilteredList.size();
			if (start + length <= size)
				newFilteredList = newFilteredList.subList(start, start + length);
			else
				newFilteredList = newFilteredList.subList(start, size);

			return newFilteredList;
		} else
			return null;
	}

	@Override
	public int getDataListSize(String editorScope)
			throws RepositoryException {
		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return 0;
		}
		return editor.getDataListSize();
	}

	@Override
	public String[] getAvailableEditorsScopes() {

		EditorScope[] values = EditorScope.values();

		String[] scopes = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			scopes[i] = values[i].name();
		}

		return scopes;
	}

	@Override
	public void deleteData(String editorScope, String id)
			throws RepositoryException {
		if (id == null)
			throw new RepositoryException("Id cannot be null");

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return;
		}

		editor.deleteData(id);
	}

	@Override
	public String getEditPageTitle(String editorScope, String id)
			throws RepositoryException {
		String pageName = "";
		if (id == null)
			return pageName;

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return "Not supported scope name";
		}
		return editor.getEditPageTitle(id);
	}


	@Override
	public List<EditorDataItem> getToEditObject(String editorScope, String id)
			throws RepositoryException {
		if (id == null)
			return null;

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}

		return editor.getToEditObject(id);
	}

	@Override
	public void saveEditedDataObject(String editorScope, String id,
			Map<String, String[]> params) throws ValidationException,
			RepositoryException, DuplicateValueException {

		if (id == null)
			return;
		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return;
		}
		editor.saveEditedDataObject(id, params);
	}

	@Override
	public List<EditorDataItem> getNewDataObject(String editorScope)
			throws RepositoryException {

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getNewDataObject();
	}

	@Override
	public List<EditorDataItem> getToSaveNewDataObject(String editorScope)
			throws RepositoryException {

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getToSaveNewDataObject();
	}

	@Override
	public void saveNewData(String editorScope, Map<String, String[]> params)
			throws RepositoryException, DuplicateValueException,
			ValidationException {

		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return;
		}
		editor.saveNewData(params);
	}

	@Override
	public String getEditorType() {
		return "Local";
	}

	@Override
	public void config(Properties props) throws ConfigurationException {
		this.props = props;
		Iterator<EditorInterface> it = editors.values().iterator();
		while (it.hasNext()) {
			it.next().configProperties(props);
		}
	}

	@Override
	public String getProperty(String name) {
		return props.getProperty(name);
	}

	@Override
	public String[] getDataKeys(String editorScope) {
		
		EditorInterface editor = editors.get(EditorScope.valueOf(editorScope));
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getDataKeys();
	}
}
