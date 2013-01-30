package de.tub.av.pe.editor.drools.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.drools.compiler.DroolsParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.context.DuplicateValueException;
import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.db.PolicyIdentifier;
import de.tub.av.pe.db.PolicyObject;
import de.tub.av.pe.db.PolicyRepository;
import de.tub.av.pe.db.PolicyRepositoryException;
import de.tub.av.pe.db.ValidationException;
import de.tub.av.pe.editor.EditorDataItem;
import de.tub.av.pe.editor.ItemHtmlElementEnum;
import de.tub.av.pe.editor.RepositoryException;
import de.tub.av.pe.editor.drools.utils.EditorUtils;
import de.tub.av.pe.identities.IdentityObject;
import de.tub.av.pe.identities.IdentityRepository;
import de.tub.av.pe.identities.IdentityRepositoryException;

public abstract class PoliciesEditor implements EditorInterface {

	private PolicyRepository polrepo;
	private IdentityRepository idrepo;
	private List<EditorDataItem> newDataObject = null;
	private String basedir = "openpecfg";
	private int minpriority = 0;

	private static final Logger log = LoggerFactory
			.getLogger(PoliciesEditor.class);

	public void config(OpenPEContextManager manager) {
		polrepo = manager.getInstance().getPolicyRepositoryManager()
				.getInstance();
		idrepo = manager.getInstance().getIdentityRepositoryManager()
				.getInstance();
	}

	public void configProperties(Properties props)
			throws ConfigurationException {
		String bdir = props.getProperty("openpe.basedir");
		if (bdir != null)
			this.basedir = bdir;
		String priority = props.getProperty("openpe.priorities");
		if (priority != null)
			minpriority = Integer.parseInt(priority);
	}

	public void saveNewData(Map<String, String[]> params)
			throws DuplicateValueException, RepositoryException,
			ValidationException {
		String policy = params.get("policy") == null ? "" : params
				.get("policy")[0];
		String identity = params.get("identity") == null ? "" : params
				.get("identity")[0];
		String scope = params.get("scope") == null ? ""
				: params.get("scope")[0];
		String event = params.get("event") == null ? ""
				: params.get("event")[0];
		int priority = Integer.parseInt(params.get("priority") == null ? ""
				: params.get("priority")[0]);

		try {
			PolicyObject po = new PolicyObject();
			po.setPolicyContent(EditorUtils.toXML(policy));
			PolicyIdentifier pi = new PolicyIdentifier();
			po.setPolicyIdentifier(pi);
			pi.setIdentity(identity);
			pi.setIdType(this.getIdentityType());
			pi.setEvent(event);
			pi.setScope(scope);
			pi.setPriority(priority);
			String id = this.polrepo.addPolicy(po);
			if (id == null) {
				setNewDataObject(identity, scope, event, policy, priority);
			}
		} catch (PolicyRepositoryException e) {
			setNewDataObject(identity, scope, event, policy, priority);
			log.error(e.getMessage(), e);
			throw new RepositoryException(e);
		}
		catch (ValidationException e) {
			setNewDataObject(identity, scope, event, policy, priority);
			log.error(e.getMessage(), e);
			throw e;
		}catch(DroolsParserException e)
		{
			setNewDataObject(identity, scope, event, policy, priority);
			log.error(e.getMessage(), e);
			throw new RepositoryException(e);			
		}
	}

	public void deleteData(String id) throws RepositoryException {

		try {
			PolicyIdentifier pi = new PolicyIdentifier();
			pi.setId(id);
			polrepo.deletePolicy(pi);
		} catch (PolicyRepositoryException e) {
			throw new RepositoryException(e);
		}
	}

	public String getEditPageTitle(String id) {

		try {
			PolicyIdentifier pi = polrepo.getPolicyIdentifier(id);
			if (pi == null)
				return "no available edit page for id: [" + id + "]";
			return "policy of " + pi.getIdentity() + " with scope "
					+ pi.getScope() + "and priority " + pi.getPriority()
					+ " for " + pi.getEvent();
		} catch (PolicyRepositoryException e) {
			return "error: [" + e.getMessage() + "]";
		}
	}

	private void setNewDataObject(String identity, String policyScope,
			String event, String policyContent, int priority)
			throws RepositoryException {
		if (newDataObject == null)
			newDataObject = new ArrayList<EditorDataItem>();
		else
			newDataObject.clear();
		try {
			List<IdentityObject> iol = idrepo.getIdentities(this
					.getIdentityType());
			List<String> identitiesLst = new ArrayList<String>(iol.size());
			for (IdentityObject io : iol) {
				identitiesLst.add(io.getName());
			}
			EditorDataItem nameItem = new EditorDataItem("identity",
					identitiesLst, ItemHtmlElementEnum.SELECTWITHEVENT,
					identity);

			EditorDataItem eventItem = new EditorDataItem("event", null,
					ItemHtmlElementEnum.INPUTWITHSUGGESTIONS, event);

			ArrayList<String> scopeList = EditorUtils
					.getAvailableScopeNames(identity);

			EditorDataItem scopeItem = new EditorDataItem("scope", scopeList,
					ItemHtmlElementEnum.SELECT, policyScope);

			nameItem.setTrigeredEditorItem(scopeItem);

			List<String> priorities = new ArrayList<String>();
			for (int i = 0; i <= minpriority; i++)
				priorities.add((new Integer(i)).toString());
			EditorDataItem priorityItem = new EditorDataItem("priority",
					priorities, ItemHtmlElementEnum.SELECT);

			newDataObject.add(nameItem);
			newDataObject.add(scopeItem);
			newDataObject.add(eventItem);
			newDataObject.add(priorityItem);

			newDataObject.add(new EditorDataItem("policy", policyContent,
					ItemHtmlElementEnum.TEXTAREA));
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e);
		}
	}

	public List<EditorDataItem> getNewDataObject() throws RepositoryException {

		ArrayList<EditorDataItem> itemList = new ArrayList<EditorDataItem>();

		try {

			EditorDataItem scopeItem = new EditorDataItem("scope",
					EditorUtils.getAvailableScopeNames("Generic"),
					ItemHtmlElementEnum.SELECT);

			List<IdentityObject> iol = idrepo.getIdentities(this
					.getIdentityType());
			List<String> identitiesLst = new ArrayList<String>(iol.size());
			for (IdentityObject io : iol) {
				identitiesLst.add(io.getName());
			}

			EditorDataItem nameItem = new EditorDataItem("identity",
					identitiesLst, ItemHtmlElementEnum.SELECTWITHEVENT);
			nameItem.setTrigeredEditorItem(scopeItem);

			EditorDataItem eventItem = new EditorDataItem("event", null,
					ItemHtmlElementEnum.INPUTWITHSUGGESTIONS);

			List<String> priorities = new ArrayList<String>();
			for (int i = 0; i <= minpriority; i++)
				priorities.add((new Integer(i)).toString());
			EditorDataItem priorityItem = new EditorDataItem("priority",
					priorities, ItemHtmlElementEnum.SELECT);

			itemList.add(nameItem);
			itemList.add(scopeItem);
			itemList.add(eventItem);
			itemList.add(priorityItem);

			itemList.add(new EditorDataItem("policy", EditorUtils
					.generatePolicyTemplate(basedir),
					ItemHtmlElementEnum.TEXTAREA));
		} catch (IdentityRepositoryException e) {
			throw new RepositoryException(e);
		}
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

			PolicyObject po = polrepo.getPolicyObject(id);
			PolicyIdentifier pi = po.getPolicyIdentifier();

			EditorDataItem scopeItem = new EditorDataItem("scope",
					EditorUtils.getAvailableScopeNames("Generic"),
					ItemHtmlElementEnum.SELECT, pi.getScope());

			List<IdentityObject> iol = idrepo.getIdentities(this
					.getIdentityType());
			List<String> identitiesLst = new ArrayList<String>(iol.size());
			for (IdentityObject io : iol) {
				identitiesLst.add(io.getName());
			}

			EditorDataItem nameItem = new EditorDataItem("identity",
					identitiesLst, ItemHtmlElementEnum.SELECTWITHEVENT,
					pi.getIdentity());
			nameItem.setTrigeredEditorItem(scopeItem);

			EditorDataItem eventItem = new EditorDataItem("event", null,
					ItemHtmlElementEnum.INPUTWITHSUGGESTIONS, pi.getEvent());

			List<String> priorities = new ArrayList<String>();
			for (int i = 0; i <= minpriority; i++)
				priorities.add((new Integer(i)).toString());
			EditorDataItem priorityItem = new EditorDataItem("priority",
					priorities, ItemHtmlElementEnum.SELECT, new Integer(
							pi.getPriority()).toString());

			itemList.add(nameItem);
			itemList.add(scopeItem);
			itemList.add(eventItem);
			itemList.add(priorityItem);

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
		String identity = params.get("identity")[0];
		String scope = params.get("scope")[0];
		String event = "testEvent";
		if(params.get("event") != null)
			event = params.get("event")[0];
		int priority = Integer.parseInt(params.get("priority")[0]);

		try {
			PolicyObject po = new PolicyObject();
			po.setPolicyContent(EditorUtils.toXML(policy));
			PolicyIdentifier pi = new PolicyIdentifier();
			po.setPolicyIdentifier(pi);
			pi.setIdentity(identity);
			pi.setIdType(this.getIdentityType());
			pi.setEvent(event);
			pi.setScope(scope);
			pi.setPriority(priority);
			pi.setId(id);

			this.polrepo.updatePolicy(po);
		} catch (Exception e) {
			setNewDataObject(identity, scope, event, policy, priority);
			log.error(e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	public abstract String getIdentityType();

	public List<Map<String, String>> getDataList() throws RepositoryException {

		List<Map<String, String>> lst = new ArrayList<Map<String, String>>();

		try {
			List<PolicyIdentifier> pil = this.polrepo
					.getPoliciesIdentifiers(this.getIdentityType());
			for (PolicyIdentifier pi : pil) {

				Map<String, String> map = new HashMap<String, String>();
				map.put("id", pi.getId());
				map.put("identity", pi.getIdentity());
				map.put("scope", pi.getScope());
				map.put("event", pi.getEvent());
				map.put("priority", new Integer(pi.getPriority()).toString());
				lst.add(map);
			}
		} catch (PolicyRepositoryException e) {
			throw new RepositoryException(e);
		}
		return lst;
	}

	public List<Map<String, String>> getDataList(int start, int length)
			throws RepositoryException {

		List<Map<String, String>> lst = new ArrayList<Map<String, String>>();

		try {
			List<PolicyIdentifier> pil = this.polrepo.getPoliciesIdentifiers(
					this.getIdentityType(), start, length);
			for (PolicyIdentifier pi : pil) {

				Map<String, String> map = new HashMap<String, String>();
				map.put("id", pi.getId());
				map.put("identity", pi.getIdentity());
				map.put("scope", pi.getScope());
				map.put("event", pi.getEvent());
				map.put("priority", new Integer(pi.getPriority()).toString());
				lst.add(map);
			}
		} catch (PolicyRepositoryException e) {
			throw new RepositoryException(e);
		}
		return lst;
	}

	public String[] getDataKeys() {
		return new String[] { "id", "identity", "scope", "event", "priority" };
	}

	public int getDataListSize() throws RepositoryException {
		try {
			return this.polrepo.getPoliciesCount(this.getIdentityType());
		} catch (PolicyRepositoryException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}

}
