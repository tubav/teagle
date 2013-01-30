
package de.tub.av.pe.rule.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tub.av.pe.rulecontext.PERule;
import de.tub.av.pe.rulecontext.RuleContext;


/**
 * Class that provides methods for processing information about each policy rule evaluation result. This class is used
 * in building the graphical interface of OpenPE (Log Overview interface).
 * @author Irina Boldea
 *
 */
public class RulesEvalOverview 
{
	private List<RuleEntry> rules = Collections.synchronizedList(new ArrayList<RuleEntry>());
		
    public RuleEntry createNewEntry() {
    	RuleEntry rule = new RuleEntry();
    	rules.add(rule);
    	return rule;
    }

    public List<RuleEntry> getRuleEntries() {
    	return rules;
    }
    
	public class RuleEntry
	{
		private String ruleContent = null;
		private String reason = "";
    	private String actionsExecLog = "No Actions Enforced.";
    	private PERule rule = null; 
    	
    	private String loopActionsExecLog = null;
    	
    	private RuleContext ruleContext;
    	    	
    	public RuleContext getRuleContext()
    	{
    		return ruleContext;
    	}
    	
    	public void setRuleContext(RuleContext ruleContext)
    	{
    		this.ruleContext = ruleContext;
    	}
    	
    	public synchronized void setRule(PERule rule)
    	{
    		this.rule = rule;
    	} 
		
		public synchronized  String getRuleId()
		{
			return rule != null?rule.getId():"";
		}

		
		public synchronized  void setRuleContent(String content)
		{
			this.ruleContent = content;
		}
		
		
		public synchronized  String getRuleContent()
		{
			if(ruleContent == null)
				return "";
			return this.ruleContent;
		}
		
		public synchronized void setReason(String reason)
		{
			if(this.reason.equals(""))
				this.reason = reason;
			else
				this.reason += "<br/>" + reason;	
		}
		
		public synchronized void resetReason(String reason)
		{
			this.reason = reason;
		}
		
		public synchronized  String getReason()
		{
			return this.reason;
		}
		
		public synchronized  String getActionsExecLog()
		{
			return this.actionsExecLog;		
		}
		
		public synchronized void setActionsExecLog(String actionsExecLog)
		{
			if(this.actionsExecLog.equals("No Actions Enforced."))
				this.actionsExecLog = actionsExecLog;
			else
				this.actionsExecLog += "<br/>" + actionsExecLog;
		}
		
		public void setLoopActionsExecLog(String log)
		{
			if(this.loopActionsExecLog == null)
				this.loopActionsExecLog = log;
			else
				this.loopActionsExecLog += "<br/>" + log;			
		}
		
		public String getLoopActionsExecLog()
		{
			this.ruleContext.executeActionsToLoop();
			return this.loopActionsExecLog;
		}
		
		public void resetLoopActionsExecLog()
		{
			this.loopActionsExecLog = null;
		}		
	}
}