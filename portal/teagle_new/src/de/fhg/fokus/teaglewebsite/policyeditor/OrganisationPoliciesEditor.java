package de.fhg.fokus.teaglewebsite.policyeditor;

import java.util.ArrayList;
import java.util.List;

import de.tu.av.openpe.xcapclient.PoliciesDBManager;
import de.tu.av.openpe.xcapclient.RepositoryException;
import de.tu.av.teagle.openpe.editor.PoliciesEditor;

public class OrganisationPoliciesEditor extends PoliciesEditor {
	private IdentitiesDBManager usersDBClient;

	private String polTempl = "";

	public static String EditorScope = "Organisation";

	public String userFilter = null;

	public OrganisationPoliciesEditor(IdentitiesDBManager usersDBClient,
			PoliciesDBManager polDbManager) {
		this.usersDBClient = usersDBClient;
		this.configure(polDbManager);
	}

	@Override
	public List<String> getIdentitiesList() throws RepositoryException {
		try {
			if (userFilter != null)
				return usersDBClient.getOrganizationList(userFilter);
			else
				return usersDBClient.getOrganizationList();
		} catch (IdentitiesRepositoryException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public String getIdentityType() {
		return "organisation";
	}

	@Override
	public List<String> getIdentityScopeList() {
		List<String> idscopes = new ArrayList<String>();
		idscopes.add("Originator");
		idscopes.add("Target");
		if (userFilter != null) {
			return idscopes;
		} else {
			idscopes.add("All");
			return idscopes;
		}
	}

	@Override
	public String getEditorScope() {
		return EditorScope;
	}

	@Override
	public String getPolicyExampleContent() {
		return this.polTempl;
	}

	@Override
	public void setUserFilter(String user) {
		this.userFilter = user;
	}

	@Override
	public void setPolicyExampleContent(String arg0) {
		this.polTempl = arg0;
	}

}
