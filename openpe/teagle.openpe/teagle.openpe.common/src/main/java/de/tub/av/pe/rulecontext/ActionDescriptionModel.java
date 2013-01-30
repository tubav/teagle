package de.tub.av.pe.rulecontext;

import java.util.Map;

public interface ActionDescriptionModel {
	public enum Frequency {
		One, ZeroOrOne, ZeroOrMore, OneOrMore;
	}
	String getType();
	Map<String, Frequency> getActionAttributes();
}
