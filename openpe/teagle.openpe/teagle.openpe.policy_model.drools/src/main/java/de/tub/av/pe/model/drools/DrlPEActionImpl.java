package de.tub.av.pe.model.drools;

import java.util.List;
import java.util.Map;

import de.tub.av.pe.rulecontext.PEAction;

public class DrlPEActionImpl implements PEAction {

	private String name;
	private Map<String, List<Object>> atts;
	private ActionImpact impact = ActionImpact.ALLOW;
	public enum ActionImpact
	{
		ALLOW, REJECT
	}
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAttributesMap(Map<String, List<Object>> atts) {
		this.atts = atts;
	}

	public Map<String, List<Object>> getAttributesMap() {
		return this.atts;
	}
	
	public void setImpact(ActionImpact impact)
	{
		this.impact = impact;
	}
	
	public ActionImpact getImpact()
	{
		return this.impact;
	}
}
