package de.fhg.fokus.teaglewebsite.policyeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import teagle.vct.model.ModelManager;
import teagle.vct.model.Organisation;
import teagle.vct.model.Person;
import teagle.vct.model.ResourceSpec;

public class IdentitiesDBManager 
{
	public List<String> getPersonList() throws IdentitiesRepositoryException
	{
		List<String> lst = new ArrayList<String>();
		try {
			List<? extends Person> personList = ModelManager.getInstance().listPersons();
			for (Person prs: personList)
			{
				lst.add(prs.getUserName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lst;
	}

	public List<String> getPersonList(String user) throws IdentitiesRepositoryException
	{
		List<String> lst = new ArrayList<String>();
		try {
			List<String> olst = this.getOrganizationList(user);
			for (String org:olst)
			{
				Organisation o = ModelManager.getInstance().getOrganisation(org);
				List<? extends Person> personList = o.getPersons();
				for (Person prs:personList)
				{
					lst.add(prs.getUserName());
				}
			}
					
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return lst;
	}

	
	
	public List<String> getOrganizationList() throws IdentitiesRepositoryException
	{
		List<String> list = new ArrayList<String>();
		list.add("All");
		
		try
		{
			List<? extends Organisation> organisations = ModelManager.getInstance().listOrganisations();
			for (Organisation org : organisations)
			{
				list.add(org.getName());
			}
			Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		}catch(Exception e)
		{
			throw new IdentitiesRepositoryException(e.getMessage(), e);
		}

		return list;		
	}

	public List<String> getOrganizationList(String user) throws IdentitiesRepositoryException
	{
		List<String> list = new ArrayList<String>();		
		try
		{
			List<? extends Organisation> organisations = ModelManager.getInstance().findOrganisationsByUserName(user);
			for (Organisation org : organisations)
			{
				list.add(org.getName());
			}
			Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		}catch(Exception e)
		{
			throw new IdentitiesRepositoryException(e.getMessage(), e);
		}

		return list;		
	}

	
	public List<String> getResourcesList() throws IdentitiesRepositoryException
	{
		List<String> list = new ArrayList<String>();
		try
		{
			List<? extends ResourceSpec> rL = ModelManager.getInstance().listResourceSpecs();
			for (ResourceSpec res: rL)
			{
				list.add(res.getCommonName());
			}
			Collections.sort(list, String.CASE_INSENSITIVE_ORDER);

		}catch(Exception e)
		{
			throw new IdentitiesRepositoryException(e.getMessage(), e);
		}		
		return list;		
	}

	public List<String> getResourcesList(String user) throws IdentitiesRepositoryException
	{
		List<String> list = new ArrayList<String>();
		try
		{

//			Organization[] organisations  = OM.getOrganizations(user);
			List<? extends Organisation> organisations = ModelManager.getInstance().findOrganisationsByUserName(user);
			for (Organisation org : organisations)
			{				
//				Resource[] resourceList = RM.getResourcesByOrganization(org.name);
				List<? extends ResourceSpec> resourceList = org.getResourceSpecs();
				for (ResourceSpec res: resourceList)
				{
					list.add(res.getCommonName());					
				}
			}
			Collections.sort(list, String.CASE_INSENSITIVE_ORDER);

		}catch(Exception e)
		{
			throw new IdentitiesRepositoryException(e.getMessage(), e);
		}		
		return list;		
	}

	
}
