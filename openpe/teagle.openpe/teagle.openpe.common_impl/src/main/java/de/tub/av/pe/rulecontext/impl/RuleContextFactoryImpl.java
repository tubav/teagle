package de.tub.av.pe.rulecontext.impl;

import de.tub.av.pe.rulecontext.RuleContext;
import de.tub.av.pe.rulecontext.RuleContextFactory;

public class RuleContextFactoryImpl implements RuleContextFactory{

	@Override
	public RuleContext newRuleContext() {
		return new RuleContextImpl();
	}

}
