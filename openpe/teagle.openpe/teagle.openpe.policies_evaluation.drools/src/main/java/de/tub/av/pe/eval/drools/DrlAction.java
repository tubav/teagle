package de.tub.av.pe.eval.drools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrlAction {
	private String name;
	

	
	Map<String, List<Object>> atts = new HashMap<String, List<Object>>();
	
	
	public DrlAction(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void addAttribute(String name, Object value)
	{
		List<Object> values = atts.get(name);
		if (values == null) {
			values = new ArrayList<Object>();
			atts.put(name, values);
		}
		values.add(value);
	}
	
	public Map<String, List<Object>> getAttributesMap()
	{
		return atts;
	}
	
}
