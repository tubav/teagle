package de.tub.av.pe.pem1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import oma.xml.fokus.pem1_input_template.InputTemplateType;
import oma.xml.fokus.pem1_input_template.PolicyInputData;
import oma.xml.fokus.pem1_output_template.PolicyOutputData;
import oma.xml.fokus.soap_pem1_input_template.FOKUSSOAPInputTemplateType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.OpenPEContextManager;

public class CallableInterfaceUtils {
	
	public static Logger log = LoggerFactory.getLogger(CallableInterfaceUtils.class);
	
	public static RequestContextInCallableMode genCallableRequestContext(OpenPEContextManager pemanager, String originator, String originatorType,
			List<String> targets, String targetType, String eventname,
			Map<String, List<Object>> parameters)
	{
		if (eventname == null)
			throw new IllegalArgumentException("eventname MUST NOT be null!");
		
		RequestContextInCallableMode reqContext = new RequestContextInCallableMode();
	
		reqContext.setOpenPEContext(pemanager.getInstance());

		reqContext.initializeEvalResultReasonGUI();

		reqContext.setOriginatorIdentity(originator);
		reqContext.setOriginatorIdentityType(originatorType);
		reqContext.getLogEntryGUI().setOriginator(originator);
		reqContext.getLogEntryGUI().setOriginatorType(originatorType);		

		if (targets != null) {
			reqContext.getTargetIdentities().addAll(targets);
			reqContext.setTargetIdentitiesType(targetType);
			reqContext.getLogEntryGUI().setTargets(targets);
			reqContext.getLogEntryGUI().setTargetsType(targetType);		
		}
		reqContext.setEvent(eventname);
		reqContext.getLogEntryGUI().setEvent(eventname);

		if (parameters != null) {
			for (Iterator<String> iterator = parameters.keySet().iterator(); iterator
					.hasNext();) {
				String paramName = iterator.next();
				List<Object> values = parameters.get(paramName);
				for (Object value : values)
					reqContext.addParameter(paramName, value);
			}
		}
		return reqContext;
	}
	
	public static PolicyOutputData doEvaluationOfInput(PolicyInputData policyInputData, OpenPEContextManager pefactory) throws DenyRequestException
	{		
		OpenPEContext pecontext = pefactory.getInstance();
		
		EvaluationConfigAndInit configAndInit = new EvaluationConfigAndInit(pecontext);

		boolean evaluationResult = false;

		List<InputTemplateType> inputTemplateList = policyInputData.getPolicyInputTemplate();

		if (inputTemplateList.size() == 1) {
			evaluationResult = configAndInit.evaluateInputTemplate((FOKUSSOAPInputTemplateType) inputTemplateList.get(0), 0);
		} else {
			ExecutorService threadsPool = Executors.newFixedThreadPool(20);

			ArrayList<FutureTask<Boolean>> futureTaskEvaluateRuleArray = new ArrayList<FutureTask<Boolean>>();
			for (int i = 0; i < inputTemplateList.size(); i++) {
				CallableInterfaceThread callIntfThread = new CallableInterfaceThread(configAndInit,
						(FOKUSSOAPInputTemplateType) inputTemplateList.get(i), i);
				FutureTask<Boolean> futureTask = new FutureTask<Boolean>(callIntfThread);
				futureTaskEvaluateRuleArray.add(futureTask);
				threadsPool.execute(futureTask);
			}

			// wait for all the threads to finish
			try{
				for (FutureTask<Boolean> futureTask : futureTaskEvaluateRuleArray) {
					evaluationResult |= futureTask.get().booleanValue();
				}
				log.info("All evaluation threads are finished {}", futureTaskEvaluateRuleArray.size());

				threadsPool.shutdown();
			}catch(Exception e)
			{	
				log.error("Exception when processing input", e);
				throw new DenyRequestException("Deny Exception", null);		
			}
		}

		if(!evaluationResult)
			throw new DenyRequestException("Deny Exception", null);

		return configAndInit.getResultedPolicyData();
	}
}
