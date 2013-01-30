package de.fhg.fokus.teaglewebsite.policyeditor;

import java.util.ArrayList;
import java.util.List;

import de.tu.av.openpe.xcapclient.PoliciesDBManager;
import de.tu.av.openpe.xcapclient.RepositoryException;
import de.tu.av.teagle.openpe.editor.PoliciesEditor;

public class UsersPoliciesEditor extends PoliciesEditor {
	private IdentitiesDBManager usersDBClient;
	public static String EditorScope = "User";
	private String userFilter;
	private String polTempl;

	public UsersPoliciesEditor(IdentitiesDBManager usersDBClient,
			PoliciesDBManager polDbManager) {
		this.usersDBClient = usersDBClient;
		this.configure(polDbManager);
	}

	@Override
	public List<String> getIdentitiesList() throws RepositoryException {
		try {
			if (userFilter == null)
				return usersDBClient.getPersonList();
			else
				return usersDBClient.getPersonList(userFilter);
		} catch (IdentitiesRepositoryException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public String getPolicyExampleContent() {
		return this.polTempl;
	}

	@Override
	public String getIdentityType() {
		return "user";
	}

	@Override
	public List<String> getIdentityScopeList() {
		List<String> list = new ArrayList<String>();
		list.add("Originator");
		list.add("Target");		
		return list;
	}

	@Override
	public String getEditorScope() {
		return EditorScope;
	}

	@Override
	public void setUserFilter(String user) {
		userFilter = user;
	}

	@Override
	public void setPolicyExampleContent(String arg0) {
		this.polTempl = arg0;
	}

}
