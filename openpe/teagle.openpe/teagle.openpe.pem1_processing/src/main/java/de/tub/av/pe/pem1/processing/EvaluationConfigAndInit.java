package de.tub.av.pe.pem1.processing;

import java.util.List;

import oma.xml.fokus.pem1_input_template.PolicyIdentifiers;
import oma.xml.fokus.pem1_output_template.EnforcementAction;
import oma.xml.fokus.pem1_output_template.EnforcementData;
import oma.xml.fokus.pem1_output_template.FOKUSOutputTemplateType;
import oma.xml.fokus.pem1_output_template.ObjectFactory;
import oma.xml.fokus.pem1_output_template.PolicyOutputData;
import oma.xml.fokus.soap_pem1_input_template.Event;
import oma.xml.fokus.soap_pem1_input_template.EventParameter;
import oma.xml.fokus.soap_pem1_input_template.FOKUSSOAPInputTemplateType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.pe.PolicyEvaluation;
import de.tub.av.pe.rule.utils.ReasonOverview.ReasonEntry;
import de.tub.av.pe.rule.utils.RulesEvalOverview.RuleEntry;

public class EvaluationConfigAndInit {

	private OpenPEContext openPEContext;
	private PolicyOutputData policyOutputData;

	private static final Logger log = LoggerFactory.getLogger(EvaluationConfigAndInit.class); 

	public EvaluationConfigAndInit(OpenPEContext openPEContext)
	{
		ObjectFactory objectFactory = new ObjectFactory();
		this.policyOutputData = objectFactory.createPolicyOutputData();
		this.openPEContext = openPEContext;
	}

	public boolean evaluateInputTemplate(FOKUSSOAPInputTemplateType inputTemplateData, int id)
	{
		long smallStartTime = System.currentTimeMillis();

		PolicyEvaluation policyEvaluationObj = openPEContext.getPolicyEvaluationManager().getInstance();

		RequestContextInCallableMode reqContext = this.getRequestContextData(inputTemplateData);

		boolean result = policyEvaluationObj.evaluateAndEnforcePolicies(reqContext);	    	 
		
		EnforcementData enfData = new EnforcementData();
		EnforcementAction action = new EnforcementAction();
		action.setEnforcementActionDescription(this.getEvaluationLog(reqContext));
		enfData.getEnforcementAction().add(action);
		
		if(result == true)
		{		
			log.debug("Evaluation time took >>> {} ms", System.currentTimeMillis()-smallStartTime);
			//log.debug("Evaluation Enforcement {}", reqContext.getEnforcemenData());
			//reqContext.getEvalOutputTemplate().setEnforcementData(reqContext.getEnforcemenData());
			reqContext.getEvalOutputTemplate().setId(new Integer(id).toString());
			reqContext.getEvalOutputTemplate().setStatusCode(2101);
			reqContext.getEvalOutputTemplate().setStatusText("Successfully evaluated");
			reqContext.getEvalOutputTemplate().setEnforcementData(enfData);
			
		}
		else
		{
			log.debug("Evaluation time took >>> {} ms", System.currentTimeMillis()-smallStartTime);
			log.debug("Evaluation Enforcement {}", reqContext.getEnforcemenData());
			reqContext.getEvalOutputTemplate().setId(new Integer(id).toString());
			reqContext.getEvalOutputTemplate().setStatusCode(2401);
			reqContext.getEvalOutputTemplate().setStatusText("Not allowed");
			reqContext.getEvalOutputTemplate().setEnforcementData(enfData);
		}
		return result;
	}
	
	public boolean evaluateInputTemplate(FOKUSSOAPInputTemplateType inputTemplateData, EvaluationStatusHandler handler)
	{
		long smallStartTime = System.currentTimeMillis();

		PolicyEvaluation policyEvaluationObj = openPEContext.getPolicyEvaluationManager().getInstance();

		RequestContextInCallableMode reqContext = this.getRequestContextData(inputTemplateData);

		boolean result = policyEvaluationObj.evaluateAndEnforcePolicies(reqContext);	    	 
				
		if(result == true)
		{		
			log.debug("Evaluation time took >>> {} ms", System.currentTimeMillis()-smallStartTime);
			//log.debug("Evaluation Enforcement {}", reqContext.getEnforcemenData());
			//reqContext.getEvalOutputTemplate().setEnforcementData(reqContext.getEnforcemenData());
			reqContext.getEvalOutputTemplate().setId("0");
			reqContext.getEvalOutputTemplate().setStatusCode(2101);
			reqContext.getEvalOutputTemplate().setStatusText("Successfully evaluated");
			handler.setMessage(this.getEvaluationLog(reqContext));		
		}
		else
		{
			log.debug("Evaluation time took >>> {} ms", System.currentTimeMillis()-smallStartTime);
			log.debug("Evaluation Enforcement {}", reqContext.getEnforcemenData());
			reqContext.getEvalOutputTemplate().setId("0");
			reqContext.getEvalOutputTemplate().setStatusCode(2401);
			reqContext.getEvalOutputTemplate().setStatusText("Not allowed");
			handler.setMessage(this.getEvaluationLog(reqContext));		
		}
		return result;
	}

	private String getEvaluationLog(RequestContextInCallableMode reqContext)
	{		
		if(reqContext == null)		
			return null;
		else
		{
			
			StringBuffer res = new StringBuffer();			
			res.append(reqContext.getLogEntryGUI().getFinalDecission());
			res.append(":\n");

			List<ReasonEntry> reasonEntries = reqContext.getLogEntryGUI().getReason().getReasonEntries();
			for (ReasonEntry rentry: reasonEntries)
			{				
				List<RuleEntry> ruleentries = rentry.getRulesEvalOverview().getRuleEntries();
				if(ruleentries.size() != 0)
				{	
					res.append(rentry.getPolicyName());
					res.append(":\n");
					for (RuleEntry ruleEntry:ruleentries)
					{		
						res.append(ruleEntry.getRuleId());
						res.append(":");
						res.append(ruleEntry.getActionsExecLog());
						if(ruleentries.size() > 1)
							res.append("\n");				
					}
					if(reasonEntries.size() > 1)
						res.append("\n");
				}
			}			
			return res.toString();
		}		
	}
	
	
	public PolicyOutputData getResultedPolicyData()
	{		
		return policyOutputData;
	}


	/**
	 *  get the request context data with the information contained in the received template
	 */
	private  RequestContextInCallableMode getRequestContextData(FOKUSSOAPInputTemplateType inputData)
	{    	
		PolicyIdentifiers  policyIdentifiers = inputData.getPolicyIdentifiers();

		RequestContextInCallableMode reqContext = new RequestContextInCallableMode();

		//set up the engine general context
		reqContext.setOpenPEContext(openPEContext);

		reqContext.initializeEvalResultReasonGUI();

		//originator application ID (IP address of the client)
		reqContext.setOriginator(policyIdentifiers.getOriginatorApplicationID());

		//target application ID (target endpoint)
		reqContext.setTarget(policyIdentifiers.getTargetApplicationID());

		//originator ID 
		reqContext.setOriginatorIdentity(policyIdentifiers.getOriginatorID());
		reqContext.setOriginatorIdentityType(policyIdentifiers.getOriginatorIDType());		
		reqContext.getLogEntryGUI().setOriginator(policyIdentifiers.getOriginatorID());
		reqContext.getLogEntryGUI().setOriginatorType(policyIdentifiers.getOriginatorIDType());

		//target ID
		List<String> targetsIdList = policyIdentifiers.getTargetID();
		reqContext.setTargetIdentities(targetsIdList);
		reqContext.setTargetIdentitiesType(policyIdentifiers.getTargetIDType());
		reqContext.getLogEntryGUI().setTargets(targetsIdList);
		reqContext.getLogEntryGUI().setTargetsType(policyIdentifiers.getTargetIDType());
		
		Event event = inputData.getEvent();
		if(event != null)
		{
			reqContext.getLogEntryGUI().setEvent(event.getName());
			reqContext.setEvent(event.getName());
			List<EventParameter> params = event.getEventParameter();
			for (EventParameter param:params)
			{
				reqContext.addParameter(param.getName(), param.getValue());				
			}
		}	    	

		//set up the output template
		FOKUSOutputTemplateType outputTemplate = new FOKUSOutputTemplateType();

		synchronized (this.policyOutputData) {
			this.policyOutputData.getPolicyOutputTemplate().add(outputTemplate);			
		}
		reqContext.setEvalOutputTemplate(outputTemplate);
		
		//set message type
		reqContext.setisRequest(true);

		//set template Id of the input message
		reqContext.setTemplateId(inputData.getTemplateID());

		//set template Version of the input message
		reqContext.setTemplateVersion(inputData.getTemplateVersion());

		outputTemplate.setTemplateID(reqContext.getTemplateId());

		outputTemplate.setTemplateVersion(reqContext.getTemplateVersion());
		
		return reqContext;
	}
	
}
