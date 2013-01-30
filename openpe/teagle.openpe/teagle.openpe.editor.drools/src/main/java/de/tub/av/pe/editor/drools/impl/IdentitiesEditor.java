package de.tub.av.pe.editor.drools.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.db.ValidationException;
import de.tub.av.pe.editor.EditorDataItem;
import de.tub.av.pe.editor.ItemHtmlElementEnum;
import de.tub.av.pe.editor.RepositoryException;
import de.tub.av.pe.identities.IdentitiesRelation;
import de.tub.av.pe.identities.IdentityObject;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.IdentityRepositoryException;

public abstract class IdentitiesEditor implements EditorInterface {

	private static final Logger log = LoggerFactory
			.getLogger(IdentitiesEditor.class);

	private List<EditorDataItem> newDataObject = null;
	private IdentityRepository idrepo;

	public void config(OpenPEContextManager Manager) {
		idrepo = Manager.getInstance().getIdentityRepositoryManager()
				.getInstance();
	}

	public void configProperties(Properties props) {
	}

	public void saveNewData(Map<String, String[]> params)
			throws RepositoryException, DuplicateValueException {

		String identity = params.get("name")[0];

		if (identity != null) {
			int id = -1;
			try {
				id = this.idrepo.addIdentity(this.getIdentityType(), identity);

				if (id == -1) {
					setNewDataObject(identity);
					throw new RepositoryException(
							"Failed when adding the identity [" + identity
									+ "]");
				}
			} catch (IdentityRepositoryException e) {
				setNewDataObject(identity);
				throw new RepositoryException(e.getMessage(), e);
			} catch (DuplicateValueException e) {
				setNewDataObject(identity);
				throw e;
			}
		} else
			throw new RepositoryException("Identity value is null!");
	}

	public void deleteData(String id) throws RepositoryException {
		try {
			this.idrepo.deleteIdentity(getIdentityType(), Integer.parseInt(id));
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException("failed to delete user with id ["
					+ id + "]", e);
		}
	}

	public String getEditPageTitle(String id) throws RepositoryException {
		try {
			String identity = this.idrepo.getIdentity(getIdentityType(),
					Integer.parseInt(id));
			return "Profile" + identity;
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(
					"error when retriving page title because ["
							+ e.getMessage() + "]", e);
		}
	}

	private void setNewDataObject(String value) {
		if (newDataObject == null)
			newDataObject = new ArrayList<EditorDataItem>();
		else
			newDataObject.clear();

		newDataObject.add(new EditorDataItem("name", value,
				ItemHtmlElementEnum.TEXT));
	}

	@Override
	public List<EditorDataItem> getNewDataObject() {

		List<EditorDataItem> list = new ArrayList<EditorDataItem>();
		list.add(new EditorDataItem("name", null, ItemHtmlElementEnum.TEXT));
		return list;
	}

	@Override
	public List<EditorDataItem> getToSaveNewDataObject() {
		if (newDataObject == null) {
			newDataObject = this.getNewDataObject();
		}
		return newDataObject;
	}

	public abstract String getIdentityType();

	public abstract String getAssociatedIdentityType();

	@Override
	public List<EditorDataItem> getToEditObject(String id)
			throws RepositoryException {

		HashMap<String, String> idsNameAndStatusMap = new HashMap<String, String>();

		try {
			String identity = idrepo.getIdentity(getIdentityType(),
					Integer.parseInt(id));

			List<IdentityObject> availableRelatedIdentities = this.idrepo
					.getIdentities(this.getAssociatedIdentityType());

			IdentitiesRelation relation = new IdentitiesRelation();
			relation.setFirstIdentityType(this.getIdentityType());
			relation.setFirstIdentity(identity);
			relation.setSecondIdentitiesType(this.getAssociatedIdentityType());
			List<IdentityObject> identityRelatedIdentities = idrepo.getSecondIdentities(relation);
			
			for (IdentityObject availableio : availableRelatedIdentities) {
				boolean found = false;
				for (IdentityObject io : identityRelatedIdentities) {
					if (availableio.getName().equals(io.getName())) {
						idsNameAndStatusMap.put(availableio.getName(),
								"checked");
						found = true;
					}
				}
				if (!found) {
					idsNameAndStatusMap.put(availableio.getName(), "unchecked");
				}
			}
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e);
		}

		List<EditorDataItem> list = new ArrayList<EditorDataItem>();
		list.add(new EditorDataItem("name", idsNameAndStatusMap,
				ItemHtmlElementEnum.CHECKBOX));
		return list;
	}

	public void saveEditedDataObject(String id, Map<String, String[]> params)
			throws ValidationException, RepositoryException {
		log.debug("Update identities");

		try {
			String identityName = idrepo.getIdentity(getIdentityType(),
					Integer.parseInt(id));

			ArrayList<String> identitiesList = new ArrayList<String>();
			Iterator<Entry<String, String[]>> it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String[]> entry = it.next();
				String name = entry.getKey();
				if (name.contains("option")) {
					identitiesList.add(entry.getValue()[0]);
				}
			}
			
			IdentitiesRelation relation = new IdentitiesRelation();
			relation.setFirstIdentity(identityName);
			relation.setFirstIdentityType(getIdentityType());
			relation.setSecondIdentitiesType(getAssociatedIdentityType());
			relation.setSecondIdentities(identitiesList);
			idrepo.updateSecondIdentities(relation);
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e);
		}
	}

	public List<Map<String, String>> getDataList() throws RepositoryException {

		List<Map<String, String>> lst = new ArrayList<Map<String, String>>();

		try {
			List<IdentityObject> iol = this.idrepo.getIdentities(this.getIdentityType());
			for (IdentityObject io : iol) {

				if (!io.getName().equals("Global")) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("id", new Integer(io.getId()).toString());
					map.put("name", io.getName());
					lst.add(map);
				}
			}
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e);
		}
		return lst;
	}
	
	public List<Map<String, String>> getDataList(int start, int length) throws RepositoryException {

		List<Map<String, String>> lst = new ArrayList<Map<String, String>>();

		try {
			List<IdentityObject> iol = this.idrepo.getIdentities(this.getIdentityType(), start, length);
			for (IdentityObject io : iol) {

				if (!io.getName().equals("Global")) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("id", new Integer(io.getId()).toString());
					map.put("name", io.getName());
					lst.add(map);
				}
			}
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e);
		}
		return lst;
	}
	
	public String[] getDataKeys() {
		return new String[] {"id", "name" };
	}
	
	public int getDataListSize() throws RepositoryException {
		try {
			return this.idrepo.getIdentitiesCount(getIdentityType());
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}

}
