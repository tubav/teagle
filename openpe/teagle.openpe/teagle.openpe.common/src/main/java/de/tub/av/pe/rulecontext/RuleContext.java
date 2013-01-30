package de.tub.av.pe.rulecontext;

import java.util.List;

import org.w3c.dom.Node;

import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.rule.utils.RulesEvalOverview;

public interface RuleContext {

	/**
	 * Sets the request context object.
	 * @param reqContext
	 */
	public void setRequestContext(RequestContextInterface reqContext);
	
	public void executeActionsToLoop();
	
	public void addActionToLoop(ActionToLoop atl);	
	
	public RequestContextInterface getRequestContext();

	public void setRule(PERule rule);	

	public PERule getRule();	
	/**
	 * Sets the rule entry GUI object.
	 * @param ruleEntry
	 */
	public void setRuleEntryGUI(RulesEvalOverview.RuleEntry ruleEntry);	
	/**
	 * Gets the rule entry GUI object.
	 */
	public RulesEvalOverview.RuleEntry getRuleEntryGUI();	
	/**
	 * Memorizes the identifier and the resulted data(soap message) of an invoke service action. It is necessary
	 * to process the references from the rule's parameters to this data.
	 * @param actionId
	 * @param invokeResourceResponseBody
	 */
	public void addInvokeResourceActionData(String actionId, Node invokeResourceResponseBody, String namespace);	
	/**
	 * Gets the value of a response parameter from service invocation action.
	 * @param actionsId
	 * @param parameterXPATH
	 */
	public List<Object> getInvokeResourceResponseParameterValue(String actionId, String parameterXPATH);	
	
}
