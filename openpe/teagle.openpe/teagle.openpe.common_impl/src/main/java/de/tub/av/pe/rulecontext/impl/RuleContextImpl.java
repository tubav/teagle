package de.tub.av.pe.rulecontext.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.rule.utils.RulesEvalOverview;
import de.tub.av.pe.rulecontext.ActionToLoop;
import de.tub.av.pe.rulecontext.PERule;
import de.tub.av.pe.rulecontext.RuleContext;

public class RuleContextImpl implements RuleContext {

	private static final Logger log = LoggerFactory
			.getLogger(RuleContextImpl.class);

	private RequestContextInterface reqContext;

	private PERule rule;

	private RulesEvalOverview.RuleEntry ruleEntry;

	private HashMap<String, InvokeResourceExecutionData> invokeResourceActionsMap = new HashMap<String, InvokeResourceExecutionData>();

	private List<ActionToLoop> actionsToLoop = Collections
			.synchronizedList(new ArrayList<ActionToLoop>());

	private XPath xpathProcessor;

	public RuleContextImpl() {
		this.xpathProcessor = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Sets the request context object.
	 * 
	 * @param reqContext
	 */
	public void setRequestContext(RequestContextInterface reqContext) {
		this.reqContext = reqContext;
	}

	public void executeActionsToLoop() {
		this.ruleEntry.resetLoopActionsExecLog();
		for (ActionToLoop atl : actionsToLoop) {
			atl.reexecuteAction();
		}
	}

	public void addActionToLoop(ActionToLoop atl) {
		actionsToLoop.add(atl);
	}

	/**
	 * Gets the request context object.
	 * 
	 * @return
	 */
	public RequestContextInterface getRequestContext() {
		return this.reqContext;
	}

	/**
	 * Sets the policy rule object.
	 * 
	 * @param rule
	 */
	public void setRule(PERule rule) {
		this.rule = rule;
	}

	/**
	 * Gets the policy rule object.
	 * 
	 * @return
	 */
	public PERule getRule() {
		return this.rule;
	}

	/**
	 * Sets the rule entry GUI object.
	 * 
	 * @param ruleEntry
	 */
	public void setRuleEntryGUI(RulesEvalOverview.RuleEntry ruleEntry) {
		this.ruleEntry = ruleEntry;
	}

	/**
	 * Gets the rule entry GUI object.
	 */
	public RulesEvalOverview.RuleEntry getRuleEntryGUI() {
		return this.ruleEntry;
	}

	/**
	 * Memorizes the identifier and the resulted data(soap message) of an invoke
	 * service action. It is necessary to process the references from the rule's
	 * parameters to this data.
	 * 
	 * @param actionId
	 * @param invokeResourceResponseBody
	 */
	public void addInvokeResourceActionData(String actionId,
			Node invokeResourceResponseBody, String namespace) {
		this.invokeResourceActionsMap.put(actionId,
				new InvokeResourceExecutionData(namespace,
						invokeResourceResponseBody));
	}

	/**
	 * Gets the value of a response parameter from service invocation action.
	 * 
	 * @param actionsId
	 * @param parameterXPATH
	 */
	public List<Object> getInvokeResourceResponseParameterValue(
			String actionId, String parameterXPATH) {
		InvokeResourceExecutionData invExecData = this.invokeResourceActionsMap
				.get(actionId);

		if (invExecData == null) {
			log.error(
					"Invoke Action id {} does not exist in the current rule context",
					actionId);
			return null;
		}
		MyNamespaceContext context = new MyNamespaceContext(
				invExecData.getServiceNamespace());

		this.xpathProcessor.setNamespaceContext(context);

		try {

			NodeList nodel = (NodeList) xpathProcessor.evaluate(parameterXPATH,
					invExecData.getInvokeResourceResponseBody(),
					XPathConstants.NODESET);

			if (nodel == null) {
				log.info("XPATH expression {} could not be found",
						parameterXPATH);
				return null;
			}
			List<Object> valuesList = new ArrayList<Object>();

			for (int i = 0; i < nodel.getLength(); i++) {
				Node item = nodel.item(i);
				valuesList.add(item.getFirstChild().getNodeValue());
			}
			return valuesList;

		} catch (XPathExpressionException e) {
			log.error("XPATH Expression {} cannot be evaluated", parameterXPATH);
			e.printStackTrace();
			return null;
		}
	}
	
	private class MyNamespaceContext implements NamespaceContext {
		String[] namespaces;

		public MyNamespaceContext(String namespace) {
			this.namespaces = namespace.split(" ");
		}

		public String getNamespaceURI(String prefix) {
			if (prefix.equals("ns") || prefix.equals(""))
				return namespaces[0];
			else {
				for (int i = 0; i < this.namespaces.length; i++) {
					if (prefix.equals("ns" + (new Integer(i + 1)).toString()))
						return namespaces[i];
				}
			}

			return javax.xml.XMLConstants.NULL_NS_URI;
		}

		public String getPrefix(String namespace) {
			return null;
		}

		public Iterator<?> getPrefixes(String namespace) {
			return null;
		}
	}

	private class InvokeResourceExecutionData {
		Node invokeResourceResponseBody;
		String serviceNamespace;

		public InvokeResourceExecutionData(String serviceNamespace,
				Node invokeResourceResponseBody) {
			this.serviceNamespace = serviceNamespace;
			this.invokeResourceResponseBody = invokeResourceResponseBody;
		}

		/**
		 * Gets the service namespace.
		 * 
		 * @return
		 */
		public String getServiceNamespace() {
			return this.serviceNamespace;
		}

		/**
		 * Gets the invoke service response body as a DOM Node.
		 * 
		 * @return
		 */
		public Node getInvokeResourceResponseBody() {
			return this.invokeResourceResponseBody;
		}
	}

}
