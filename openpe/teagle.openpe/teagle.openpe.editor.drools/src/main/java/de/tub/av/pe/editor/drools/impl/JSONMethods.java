package de.tub.av.pe.editor.drools.impl;

import java.util.ArrayList;
import java.util.List;

import de.tub.av.pe.editor.drools.utils.EditorUtils;
import de.tub.av.pe.editor.drools.utils.PolicyDataObject;

public class JSONMethods {
	
	private PolicyDataObject pdo;
	public JSONMethods(PolicyDataObject pdo)	
	{		
		this.pdo = pdo;
	}
	public List<String> getAssociatedData(String type, String data)
	{
		if (type.equals("scope"))
			return EditorUtils.getAvailableScopeNames(data);
		return null;
	}
	
	public List<String> getSuggestData(String type)
	{
		if(type != null && type.equals("event"))
		{			
			return pdo.getListEvents();
		}else
		{
			return new ArrayList<String>();
		}
	}
	
}
