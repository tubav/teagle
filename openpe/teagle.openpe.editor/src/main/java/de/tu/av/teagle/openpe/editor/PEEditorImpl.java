package de.tu.av.teagle.openpe.editor;

import gen.openpe.identifiers.policy.PolicyIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu.av.openpe.xcapclient.PolicyObject;
import de.tu.av.openpe.xcapclient.RepositoryException;

public class PEEditorImpl implements PEEditor {
	private Logger log = LoggerFactory.getLogger(PEEditorImpl.class);

	private Map<String, PoliciesEditor> editors = new HashMap<String, PoliciesEditor>();

	private Properties props;

	public void addEditor(PoliciesEditor editor)
	{
		editors.put(editor.getEditorScope(), editor);		
	}
	
	
	public PoliciesEditor getPolicyEditor(String editorScope)
	{
		return editors.get(editorScope);
	}

	@Override
	public List<PolicyIdentifier> getDataList(String editorScope)
			throws RepositoryException {
		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getDataList();
	}
	
	@Override
	public String getIdentityType(String editorScope)
	{
		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getIdentityType();		
	}
	
	
	public PolicyIdentifier getPolicyIdentifier(String editorScope, String id) throws RepositoryException
	{
		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getPolicyIdentifier(id);
	}
	
	public PolicyObject getPolicyObject(String editorScope, String id) throws RepositoryException
	{
		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getPolicyObject(id);
	}
	
//	@Override
//	public List<Map<String, String>> getDataList(String editorScope, int start, int length)
//			throws RepositoryException {
//		log.debug("Reset Cache for {}!", editorScope);
//
//		PoliciesEditor editor = editors.get(editorScope);
//		if (editor == null) {
//			log.error("Not supported scope name {}", editorScope);
//			return null;
//		}
//		return editor.getDataList(start, length);
//	}

//	@Override
//	public List<Map<String, String>> getDataList(
//			String editorScope, int start, int length, String filterName,
//			String value) throws RepositoryException {
//
//		List<Map<String, String>> dataList = this.getDataList(editorScope);
//
//		if (dataList != null) {
//			List<Map<String, String>> newFilteredList = new ArrayList<Map<String, String>>();
//
//			if (value.equals("")) {
//				newFilteredList = dataList;
//			} else {
//				for (Map<String, String> data:dataList) {
//					if (data.get(filterName) != null
//							&& data.get(filterName).toLowerCase()
//									.contains(value.toLowerCase())) {
//						newFilteredList.add(data);
//					}
//				}
//			}
//			int size = newFilteredList.size();
//			if (start + length <= size)
//				newFilteredList = newFilteredList.subList(start, start + length);
//			else
//				newFilteredList = newFilteredList.subList(start, size);
//
//			return newFilteredList;
//		} else
//			return null;
//	}

//	@Override
//	public int getDataListSize(String editorScope)
//			throws RepositoryException {
//		PoliciesEditor editor = editors.get(editorScope);
//		if (editor == null) {
//			log.error("Not supported scope name {}", editorScope);
//			return 0;
//		}
//		return editor.getDataListSize();
//	}

	@Override
	public String[] getAvailableEditorsScopes() {
		return editors.keySet().toArray(new String[0]);
	}

	@Override
	public void deleteData(String editorScope, String id)
			throws RepositoryException {
		if (id == null)
			throw new RepositoryException("Id cannot be null");

		PoliciesEditor editor = editors.get(editorScope);
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

		PoliciesEditor editor = editors.get(editorScope);
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

		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return new ArrayList<EditorDataItem>();
		}

		return editor.getToEditObject(id);
	}

	@Override
	public void saveEditedDataObject(String editorScope, String id,
			Map<String, String[]> params) throws ValidationException,
			RepositoryException, DuplicateValueException {

		if (id == null)
			return;
		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return;
		}
		editor.saveEditedDataObject(id, params);
	}

	@Override
	public List<EditorDataItem> getNewDataObject(String editorScope)
			throws RepositoryException {

		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getNewDataObject();
	}

	@Override
	public List<EditorDataItem> getToSaveNewDataObject(String editorScope)
			throws RepositoryException {

		PoliciesEditor editor = editors.get(editorScope);
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

		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return;
		}
		editor.saveNewData(params);
	}



	@Override
	public String getProperty(String name) {
		return props.getProperty(name);
	}

	@Override
	public String[] getDataKeys(String editorScope) {
		
		PoliciesEditor editor = editors.get(editorScope);
		if (editor == null) {
			log.error("Not supported scope name {}", editorScope);
			return null;
		}
		return editor.getDataKeys();
	}


	@Override
	public PoliciesEditor[] getEditors() {		
		return editors.values().toArray(new PoliciesEditor[0]);		
	}
}
