
package de.tub.av.pe.rule.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that provides methods for manipulation of each policy evaluation result and reason of the evaluation decision.
 * This class is used in building the Log Overview GUI Interface. 
 * @author Irina Boldea
 * @see RulesEvalOverview
 */
public class ReasonOverview {

    private List<ReasonEntry> entries = new ArrayList<ReasonEntry>();
    
    
    /**
     * 
     * @return
     */
    public ReasonEntry createNewEntry() {
    	ReasonEntry entry = new ReasonEntry();
    	entries.add(entry);
    	return entry;
    }
    /**
     * 
     * @return
     */
    public List<ReasonEntry> getReasonEntries() {
    	return entries;
    }
    
    
    public class ReasonEntry 
    {
    	
    	private String policyName = "";
    	private RulesEvalOverview policy;
    	private String reason = "";   
     	private boolean error = false;
     	
     	public ReasonEntry()
     	{
     		 policy = new RulesEvalOverview();
     	}
     	
     	/**
     	 * 
     	 * @return
     	 */
     	public synchronized boolean isError() {
    		return error;
    	}
    	
     	/**
     	 * 
     	 * @param error
     	 */
    	public synchronized void setError(boolean error) {
    		this.error = error;
    	}
    	
    	/**
    	 * 
    	 * @return
    	 */
    	public synchronized String getPolicyName() {
			return policyName;
		}

    	/**
    	 * 
    	 * @param policyName
    	 */
		public synchronized void setPolicyName(String policyName) {
			this.policyName = policyName;
		}


		/**
		 * 
		 * @return
		 */
		public synchronized String getReason() {
			return reason;
		}
		/**
		 * 
		 * @param reason
		 */
		public synchronized void setReason(String reason) 
		{
			if(this.reason.equals(""))
				this.reason = reason;
			else
				this.reason += "<br/>" + reason;	
		}
		/**
		 * 
		 * @return
		 */
		public synchronized RulesEvalOverview getRulesEvalOverview() {
			return this.policy;
		}
		/**
		 * 
		 * @param policy
		 */
		public synchronized void setRulesEvalOverview(RulesEvalOverview policy) {
			this.policy = policy;
		}		
    }
}
