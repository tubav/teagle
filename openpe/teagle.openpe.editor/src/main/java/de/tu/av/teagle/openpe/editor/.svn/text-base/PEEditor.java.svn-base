package de.tu.av.teagle.openpe.editor;

import gen.openpe.identifiers.policy.PolicyIdentifier;

import java.util.List;
import java.util.Map;

import de.tu.av.openpe.xcapclient.RepositoryException;

public interface PEEditor {

	String[] getAvailableEditorsScopes();

	PoliciesEditor[] getEditors();
	
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


//	List<Map<String, String>> getDataList(String editorScope, int start,
//			int length) throws RepositoryException;
//
//	List<Map<String, String>> getDataList(String editorScope, int start,
//			int length, String filterName, String value)
//			throws RepositoryException;
//
//	int getDataListSize(String editorScope) throws RepositoryException;

	void deleteData(String editorScope, String id) throws RepositoryException;

	String[] getDataKeys(String editorScope);

	List<PolicyIdentifier> getDataList(String editorScope)
			throws RepositoryException;

	String getIdentityType(String editorScope);

}
