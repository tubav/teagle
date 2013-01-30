package de.tu.av.teagle.openpe.editor;

import gen.openpe.identifiers.policy.PolicyIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.compiler.DroolsParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu.av.openpe.xcapclient.PoliciesDBManager;
import de.tu.av.openpe.xcapclient.PolicyObject;
import de.tu.av.openpe.xcapclient.RepositoryException;
import de.tu.av.teagle.openpe.editor.utils.EditorUtils;

public abstract class PoliciesEditor {

	private List<EditorDataItem> newDataObject = null;
	private PoliciesDBManager polrepo;

	private static final Logger log = LoggerFactory
			.getLogger(PoliciesEditor.class);

	public void configure(PoliciesDBManager polrepo)
	{
		this.polrepo = polrepo;
	}
	
	public abstract List<String> getIdentitiesList() throws RepositoryException;

	public abstract String getIdentityType();

	public abstract List<String> getIdentityScopeList();
	
	public abstract String getEditorScope();
	
	public abstract String getPolicyExampleContent();

	public abstract void setPolicyExampleContent(String arg0);
	
	public abstract void setUserFilter(String user);
	
		
	public void saveNewData(Map<String, String[]> params)
			throws DuplicateValueException, RepositoryException,
			ValidationException {
		String policy = params.get("policy") == null ? "" : params
				.get("policy")[0];
		String Identity = params.get("identity") == null ? "" : params
				.get("identity")[0];
		String Scope = params.get("scope") == null ? ""
				: params.get("scope")[0];
		String Operation = params.get("operation") == null ? ""
				: params.get("operation")[0];

		try {
			PolicyIdentifier pi = new PolicyIdentifier();
			pi.setIdentity(Identity);
			pi.setIdType(this.getIdentityType());
			pi.setEvent(Operation);
			pi.setScope(Scope);
			String policyContent = EditorUtils.toXML(policy);
			this.polrepo.addPolicy(pi, policyContent);
		} catch (RepositoryException e) {
			setNewDataObject(Identity, Scope, Operation, policy);
			log.error(e.getMessage(), e);
			throw e;
		} catch (DroolsParserException e) {
			setNewDataObject(Identity, Scope, Operation, policy);
			log.error(e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	public void deleteData(String id) throws RepositoryException {
		PolicyIdentifier pi = this.getPolicyIdentifier(id);
		polrepo.deletePolicy(pi);
	}

	public String getEditPageTitle(String id) {

		try {
			PolicyIdentifier pi = polrepo.getPolicyIdentifier(this.getIdentityType(), id);
			if (pi == null)
				return "no available edit page for id: [" + id + "]";
			return "policy of " + pi.getIdentity() + " with scope "
					+ pi.getScope() 
					+ " for " + pi.getEvent();
		} catch (RepositoryException e) {
			return "error: [" + e.getMessage() + "]";
		}
	}

	private void setNewDataObject(String Identity, String policyScope,
			String Operation, String policyContent) throws RepositoryException {
		if (newDataObject == null)
			newDataObject = new ArrayList<EditorDataItem>();
		else
			newDataObject.clear();
		List<String> identitiesLst = this.getIdentitiesList();
		EditorDataItem nameItem = new EditorDataItem("identity",
				identitiesLst, ItemHtmlElementEnum.SELECT, Identity);

		EditorDataItem eventItem = new EditorDataItem("operation", this.polrepo.getEventsList(),
				ItemHtmlElementEnum.SELECT, Operation);

		ArrayList<String> scopeList = EditorUtils
				.getAvailableScopeNames(Identity);

		EditorDataItem scopeItem = new EditorDataItem("scope", scopeList,
				ItemHtmlElementEnum.SELECT, policyScope);

		nameItem.setTrigeredEditorItem(scopeItem);

		newDataObject.add(nameItem);
		newDataObject.add(scopeItem);
		newDataObject.add(eventItem);

		newDataObject.add(new EditorDataItem("policy", policyContent,
				ItemHtmlElementEnum.TEXTAREA));
	}

	public List<EditorDataItem> getNewDataObject() throws RepositoryException {

		List<EditorDataItem> itemList = new ArrayList<EditorDataItem>();

		EditorDataItem nameItem = new EditorDataItem("identity",
				this.getIdentitiesList(), ItemHtmlElementEnum.SELECT);

		EditorDataItem eventItem = new EditorDataItem("operation",
				this.polrepo.getEventsList(), ItemHtmlElementEnum.SELECT);

		EditorDataItem scopeItem = new EditorDataItem("scope",
				this.getIdentityScopeList(), ItemHtmlElementEnum.SELECT);

		itemList.add(nameItem);
		itemList.add(scopeItem);
		itemList.add(eventItem);

		String content = "";
		try {
			content = EditorUtils.toDrl(this.getPolicyExampleContent());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		itemList.add(new EditorDataItem("policy", content, ItemHtmlElementEnum.TEXTAREA));
		return itemList;
	}

	public List<EditorDataItem> getToSaveNewDataObject()
			throws RepositoryException {
		if (newDataObject == null) {
			newDataObject = this.getNewDataObject();
		}
		return newDataObject;
	}

	public List<EditorDataItem> getToEditObject(String id)
			throws RepositoryException {

		List<EditorDataItem> itemList = new ArrayList<EditorDataItem>();

		try {
			PolicyObject po = polrepo.getPolicyObject(this.getIdentityType(), id);
			PolicyIdentifier pi = po.getPolicyIdentifier();
			
			EditorDataItem scopeItem = new EditorDataItem("scope",
					this.getIdentityScopeList(),
					ItemHtmlElementEnum.SELECT, pi.getScope());

			EditorDataItem nameItem = new EditorDataItem("identity",
					this.getIdentitiesList(), ItemHtmlElementEnum.SELECT,
					pi.getIdentity());
			nameItem.setTrigeredEditorItem(scopeItem);

			EditorDataItem eventItem = new EditorDataItem("operation", this.polrepo.getEventsList(),
					ItemHtmlElementEnum.SELECT, pi.getEvent());

			itemList.add(nameItem);
			itemList.add(scopeItem);
			itemList.add(eventItem);

			String policyContent = EditorUtils.toDrl(po.getPolicyContent());
			itemList.add(new EditorDataItem("policy", policyContent,
					ItemHtmlElementEnum.TEXTAREA));
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage(), e);
		}
		return itemList;
	}

	public void saveEditedDataObject(String id, Map<String, String[]> params)
			throws RepositoryException, ValidationException,
			DuplicateValueException {

		String policy = params.get("policy")[0];
		String Identity = params.get("identity")[0];
		String Scope = params.get("scope")[0];
		String Operation = params.get("operation")[0];

		try {
			PolicyIdentifier pi = new PolicyIdentifier();
			pi.setIdentity(Identity);
			pi.setIdType(this.getIdentityType());
			pi.setEvent(Operation);
			pi.setScope(Scope);
			pi.setId(id);
			String policyContent = EditorUtils.toXML(policy);
			this.polrepo.updatePolicy(pi, policyContent);
		} catch (Exception e) {
			setNewDataObject(Identity, Scope, Operation, policy);
			log.error(e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	public List<PolicyIdentifier> getDataList() throws RepositoryException {

		List<PolicyIdentifier> pil = this.polrepo.getPoliciesIdentifiers(this
				.getIdentityType());

		return pil;
	}
	public String[] getDataKeys() {
		return new String[] { "id", "identity", "scope", "operation"};
	}

	
	public PolicyIdentifier getPolicyIdentifier(String id) throws RepositoryException
	{
		return this.polrepo.getPolicyIdentifier(this.getIdentityType(), id);
	}

	public PolicyObject getPolicyObject(String id) throws RepositoryException {
		return this.polrepo.getPolicyObject(this.getIdentityType(), id);
	}
	
}
