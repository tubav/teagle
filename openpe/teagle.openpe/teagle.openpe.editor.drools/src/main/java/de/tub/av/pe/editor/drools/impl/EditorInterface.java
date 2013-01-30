package de.tub.av.pe.editor.drools.impl;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.db.ValidationException;
import de.tub.av.pe.editor.EditorDataItem;
import de.tub.av.pe.editor.RepositoryException;

public interface EditorInterface {

	public void config(OpenPEContextManager factory);

	public void configProperties(Properties props)
			throws ConfigurationException;

	public void deleteData(String id) throws RepositoryException;

	public String getEditPageTitle(String id) throws RepositoryException;

	public List<EditorDataItem> getToEditObject(String id)
			throws RepositoryException;

	public void saveEditedDataObject(String id, Map<String, String[]> params)
			throws ValidationException, RepositoryException,
			DuplicateValueException;

	public List<EditorDataItem> getNewDataObject() throws RepositoryException;

	public void saveNewData(Map<String, String[]> params)
			throws RepositoryException, DuplicateValueException,
			ValidationException;

	public List<Map<String, String>> getDataList() throws RepositoryException;

	public List<Map<String, String>> getDataList(int start, int length) throws RepositoryException;

	public String[] getDataKeys();

	public List<EditorDataItem> getToSaveNewDataObject()
			throws RepositoryException;
	
	public int getDataListSize() throws RepositoryException;
}
