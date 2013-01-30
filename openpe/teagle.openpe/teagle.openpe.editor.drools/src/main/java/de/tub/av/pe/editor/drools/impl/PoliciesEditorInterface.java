package de.tub.av.pe.editor.drools.impl;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import de.tub.av.pe.db.PolicyObject;
import de.tub.av.pe.editor.EditorDataItem;
import de.tub.av.pe.editor.RepositoryException;

public interface PoliciesEditorInterface {

	public boolean delete(ArrayList<PolicyObject> ctgData, String id);

	public String getEditPageName(ArrayList<PolicyObject> ctgData, String id);

	public Object getToEditObject(ArrayList<PolicyObject> ctgData, String id);

	public boolean updateObject(ArrayList<PolicyObject> ctgData, String id,
			HttpServletRequest request) throws JAXBException;

	public ArrayList<EditorDataItem> getNewDataObject();

	public boolean saveNewObject(ArrayList<PolicyObject> ctgData,
			HttpServletRequest request) throws RepositoryException;

	public ArrayList<PolicyObject> getCategoryData();
}
