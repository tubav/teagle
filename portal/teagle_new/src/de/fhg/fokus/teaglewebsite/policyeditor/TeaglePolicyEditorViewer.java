package de.fhg.fokus.teaglewebsite.policyeditor;

import gen.openpe.elements.policy.EvaluationOutput;
import gen.openpe.elements.policy.OutputOverview;
import gen.openpe.elements.policy.OutputOverviews;
import gen.openpe.identifiers.policy.PolicyIdentifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu.av.openpe.xcapclient.PoliciesDBManager;
import de.tu.av.openpe.xcapclient.RepositoryException;
import de.tu.av.teagle.openpe.editor.PEEditorImpl;
import de.tu.av.teagle.openpe.editor.PoliciesEditor;
import edu.emory.mathcs.backport.java.util.Collections;

public class TeaglePolicyEditorViewer extends PEEditorImpl {

	private PoliciesDBManager policiesDBClient;
	private HashMap<Long, OutputOverview> hmoo;
	private IdentitiesDBManager usersDBClient;
	private ServletConfig servConfig = null;
	private static final String policyTemplate = "/PolicyTemplate.drl";
	private static final String policySchema = "/org.openmobilealliance.policy_commonpol.xsd";

	private Logger log = LoggerFactory
			.getLogger(TeaglePolicyEditorViewer.class);

	public TeaglePolicyEditorViewer() {
		super();
		this.policiesDBClient = ClientPoliciesEditors.getInstance()
				.getPoliciesDBClient();
		this.usersDBClient = ClientPoliciesEditors.getInstance()
				.getUsersDBClient();

		this.addEditor(new OrganisationPoliciesEditor(usersDBClient,
				policiesDBClient));
		this.addEditor(new ResourcePoliciesEditor(usersDBClient,
				policiesDBClient));
		this.addEditor(new UsersPoliciesEditor(usersDBClient, policiesDBClient));
	}

	public void setServletConfig(ServletConfig arg0) {
		if (this.servConfig == null) {
			this.servConfig = arg0;
			InputStream is = servConfig.getServletContext()
					.getResourceAsStream(policyTemplate);
			String exmplepolContent = readInputStream(is);
			for (PoliciesEditor pe : this.getEditors()) {
				pe.setPolicyExampleContent(exmplepolContent);
			}
		}
	}

	public void setUserFilter(String userName) {
		for (PoliciesEditor pe : this.getEditors()) {
			pe.setUserFilter(userName);
		}
	}

	public List<PolicyIdentifier> getUserFilteredPolicyObject(String editorScope) {
		List<PolicyIdentifier> res = new ArrayList<PolicyIdentifier>();

		try {

			PoliciesEditor editor = this.getPolicyEditor(editorScope);
			if (editor != null) {
				List<String> supportedIdentities = editor.getIdentitiesList();
				List<PolicyIdentifier> piL = this.getDataList(editorScope);
				for (PolicyIdentifier pi : piL) {
					if (supportedIdentities.contains(pi.getIdentity())) {
						res.add(pi);
					}
				}
			}
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		}
		return res;
	}

	public HashMap<Long, OutputOverview> loadNewOutputOverviews(
			String userFilter) {
		hmoo = new LinkedHashMap<Long, OutputOverview>();

		HashMap<Long, OutputOverview> hm = new HashMap<Long, OutputOverview>();
		OutputOverviews oos = policiesDBClient.getOutputOverviews();
		if (oos == null)
			return hm;
		for (OutputOverview oo : oos.getOutputOverview()) {
			if (userFilter == null)
				hm.put(oo.getId(), oo);
			else {
				try {

					String filterOrg = this.usersDBClient.getOrganizationList(
							userFilter).get(0);
					List<String> filteredResourcesList = this.usersDBClient
							.getResourcesList(userFilter);
					if (oo.getOriginatortype().equals("user")) {
						List<String> lst = this.usersDBClient
								.getOrganizationList(oo.getOriginator());
						if(lst.size()!=0)
					{
							String origOrg = lst.get(0);
						if (origOrg.equals(filterOrg))
							hm.put(oo.getId(), oo);
					}	
					} else if (oo.getTargettype().equals("user")) {
						List<String> lst = this.usersDBClient
								.getOrganizationList(oo.getTarget());
						if(lst.size()!=0)
						{
							String targetOrg = lst.get(0);
						if (targetOrg.equals(filterOrg))
							hm.put(oo.getId(), oo);
						}
					} else if (oo.getOriginatortype().equals("resource")) {
						if (filteredResourcesList.contains(oo.getOriginator()))
							hm.put(oo.getId(), oo);
					} else if (oo.getTargettype().equals("resource")) {
						if (filteredResourcesList.contains(oo.getTarget()))
							hm.put(oo.getId(), oo);
					}

				} catch (IdentitiesRepositoryException e) {
					hm.put(oo.getId(), oo);
					log.error(
							"Filtering has failed because information about the user {} is not available", userFilter,
							e);
				}

			}
		}
		List<Long> sortedKeys = new ArrayList<Long>(hm.keySet());
		Collections.sort(sortedKeys);
		Collections.reverse(sortedKeys);
		for (Long key : sortedKeys) {
			hmoo.put(key, hm.get(key));
		}
		return hmoo;
	}

	public HashMap<Long, OutputOverview> getCurrentOutputOverview() {
		return this.hmoo;
	}

	public static PolicyIdentifier toPolicyIdentifier(EvaluationOutput eo) {
		PolicyIdentifier pi = new PolicyIdentifier();
		String[] s = eo.getPolicyName().split("/");
		System.out.println(eo.getPolicyName());
		pi.setId(s[0]);
		pi.setIdType(s[1]);
		pi.setIdentity(s[2]);
		pi.setScope(s[3]);
		pi.setEvent(s[4]);
		return pi;
	}

	public PoliciesDBManager getPolicyDBClient() {
		return this.policiesDBClient;
	}

	public String getPolicySchemaContent() {
		return readInputStream(this.servConfig.getServletContext()
				.getResourceAsStream(policySchema));
	}

	public String getGeneralTipsMessage(String editorScope) {
		String message = "";
		if (editorScope.equals(OrganisationPoliciesEditor.EditorScope)) {
			message = "'An organization policy defines rules that apply for users or resources which are part of the specific organization. It provides a mechanism for addressing a collection of users/resource.'";
		} else if (editorScope.equals(ResourcePoliciesEditor.EditorScope)) {
			message = "'A resource policy defines rules that apply for a specific resource.'";
		} else if (editorScope.equals(UsersPoliciesEditor.EditorScope)) {
			message = "'A user policy defines rules that apply for a specific user.'";
		}
		return message;
	}

	public String getTipsMessage(String editorScope, String type) {
		if (type.equals("scope"))
			return getScopeTipsMessage(editorScope);
		else if (type.equals("operation"))
			return getOperationTipsMessage();
		else if (type.equals("policy"))
			return getPolicyContentTipsMessage();
		else
			return getIdentityTipsMessage();
	}

	public String getScopeTipsMessage(String editorScope) {
		if (editorScope.equals(OrganisationPoliciesEditor.EditorScope))
			return "'Scope of the identity regarding the operation.Possible values are <i>Originator</i>, <i>Target</i> or <i>All</i>.'";
		else
			return "'Scope of the identity regarding the operation.Possible values are <i>Originator</i> or <i>Target</i>.'";
	}

	public String getOperationTipsMessage() {
		return "'Operation for which the policy was defined. When an entity performs this operation, it triggers the policy evaluation before procedding with execution.'";
	}

	public String getIdentityTipsMessage() {
		return "'Type of the identity.'";
	}

	public String getPolicyContentTipsMessage() {
		return "'Content of the policy.'";
	}

	// public String getEDITipsMessage(String fieldName, String editorScope)
	// {
	// if(fieldName.equals("Scope"))
	// return this.getScopeTipsMessage(editorScope);
	// else if (fieldName.equals("Policy Content"))
	// {
	// return this.getPolicyContentTipsMessage();
	// }else if (fieldName.equals("Operation"))
	// {
	// return this.getOperationTipsMessage();
	// }
	// else{
	// return this.getIdentityTipsMessage();
	// }
	// }

	private String readInputStream(InputStream stream) {
		char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = null;
		try {
			try {
				in = new InputStreamReader(stream, "UTF-8");
				int read;
				do {
					read = in.read(buffer, 0, buffer.length);
					if (read > 0) {
						out.append(buffer, 0, read);
					}
				} while (read >= 0);
			} finally {
				if (in != null)
					in.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return out.toString();
	}

}
