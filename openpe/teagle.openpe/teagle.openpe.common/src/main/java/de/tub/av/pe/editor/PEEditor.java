package de.tub.av.pe.editor;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.db.ValidationException;

public interface PEEditor {
	public String getEditorType();

	public void config(Properties props) throws ConfigurationException;

	void config(OpenPEContextManager factory);

	String[] getAvailableEditorsScopes();

	List<EditorDataItem> getToEditObject(String editorScope, String id)
			throws RepositoryException;

	void saveEditedDataObject(String editorScope, String id,
			Map<String, String[]> params) throws ValidationException,
			RepositoryException, DuplicateValueException;

	List<EditorDataItem> getNewDataObject(String editorScope)
			throws RepositoryException;

	List<EditorDataItem> getToSaveNewDataObject(String editorScope)
			throws RepositoryException;

	void saveNewData(String editorScope, Map<String, String[]> params)
			throws RepositoryException, DuplicateValueException,
			ValidationException;

	String getProperty(String name);

	String getEditPageTitle(String editorScope, String id)
			throws RepositoryException;

	List<Map<String, String>> getDataList(String editorScope)
			throws RepositoryException;

	List<Map<String, String>> getDataList(String editorScope, int start,
			int length) throws RepositoryException;

	List<Map<String, String>> getDataList(String editorScope, int start,
			int length, String filterName, String value)
			throws RepositoryException;

	int getDataListSize(String editorScope) throws RepositoryException;

	void deleteData(String editorScope, String id) throws RepositoryException;

	String[] getDataKeys(String editorScope);

}
