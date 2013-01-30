package de.tub.av.pe.model.drools;

import org.drools.definition.rule.Rule;
import org.drools.runtime.rule.Agenda;

import de.tub.av.pe.rulecontext.PERule;

public class DrlPERuleImpl implements PERule{

	private Rule rule;
	private Agenda agenda;

	@Override
	public String getId() {
		return this.rule.getName();
	}

	@Override
	public String getContent() {
		return null;
	}

	public void setRule(Rule rule)
	{
		this.rule = rule;
	}
	public Rule getRule()
	{
		return this.rule;
	}

	public void setAgenda(Agenda agenda) {
		this.agenda = agenda;		
	}
	public Agenda getAgenda()
	{
		return this.agenda;
	}
}
