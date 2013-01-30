package de.fhg.fokus.teaglewebsite.policyeditor;

import java.util.ArrayList;
import java.util.List;

import de.tu.av.openpe.xcapclient.PoliciesDBManager;
import de.tu.av.openpe.xcapclient.PoliciesDBManagerWithXcap;

public class ClientPoliciesEditors {

	private static ClientPoliciesEditors instance;
	
	private PoliciesDBManager policiesDBClient;

	private IdentitiesDBManager usersDBClient;
	
	private ClientPoliciesEditors()
	{
		List<String> proposedEventsList = new ArrayList<String>();
		proposedEventsList.add("bookResource");
		proposedEventsList.add("bookVct");
		proposedEventsList.add("connectResources");
		usersDBClient = new IdentitiesDBManager();

		policiesDBClient = new PoliciesDBManagerWithXcap("127.0.0.1", 8080,
				"openpe/xcap", proposedEventsList);
	}
		
	public PoliciesDBManager getPoliciesDBClient()
	{
		return this.policiesDBClient;
	}
	
	public IdentitiesDBManager getUsersDBClient()
	{
		return this.usersDBClient;
	}
	
	public static ClientPoliciesEditors getInstance()
	{
		if(instance == null)
			instance = new ClientPoliciesEditors();
		return instance;
	}	
}
