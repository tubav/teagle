package de.fhg.fokus.teaglewebsite.policyeditor;

import java.util.ArrayList;
import java.util.List;

import de.tu.av.openpe.xcapclient.PoliciesDBManager;
import de.tu.av.openpe.xcapclient.RepositoryException;
import de.tu.av.teagle.openpe.editor.PoliciesEditor;


public class ResourcePoliciesEditor extends PoliciesEditor
{	
	private IdentitiesDBManager identitiesDBClient;
	public static String EditorScope = "Resource";
	private String userFilter;
	private String polTempl = "";
		
	public ResourcePoliciesEditor(IdentitiesDBManager identitiesDBClient, PoliciesDBManager polDbManger)
	{
		this.identitiesDBClient = identitiesDBClient;		
		this.configure(polDbManger);
	}
	
	@Override	
	public String getPolicyExampleContent()
	{
		return polTempl;
	}


	@Override
	public List<String> getIdentitiesList()throws RepositoryException
	{	
		try{
		if(userFilter == null)
			return identitiesDBClient.getResourcesList();
		else
			return identitiesDBClient.getResourcesList(userFilter);			
		}catch(IdentitiesRepositoryException e)
		{
			throw new RepositoryException(e.getMessage(), e);
		}
		}
	
	@Override
	public String getIdentityType()
	{
		return "resource";
	}

	@Override
	public List<String> getIdentityScopeList()
	{		
		List<String> list = new ArrayList<String>();
		list.add("Originator");
		list.add("Target");
		return list;	
	}
	
	
	@Override
	public String getEditorScope()
	{
		return EditorScope;
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


