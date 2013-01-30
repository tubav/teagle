package de.fhg.fokus.ngni.openpe.pem1;


import java.util.List;

import oma.xml.fokus.pem1_output_template.EnforcementAction;
import oma.xml.fokus.pem1_output_template.EnforcementData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyEnforcementActionsExecutor {
	private static Logger log = LoggerFactory.getLogger(PolicyEnforcementActionsExecutor.class);

	//how to example ..
	public static Object enforceActions(EnforcementData enforcementData)
	{
		List<EnforcementAction> enforcemntActionsList = enforcementData.getEnforcementAction();
		if(enforcemntActionsList.size() == 0)
    	{
    		log.error("No actions to execute");
    		return null;
    	}		
		
		for (EnforcementAction efnAction: enforcemntActionsList)
		{		
			int actionId = efnAction.getId();
//			switch(actionId)
//			{
////			case EnforcementConstant.ModifyMsgParamsActionId:
////				PolicyEvaluationTransformation policyEvaluationTransformation = new PolicyEvaluationTransformation(efnAction);
////				return policyEvaluationTransformation;
////			case EnforcementConstants.SelectCommunicationChannelActionId:
//				break;
//			}
		}
		return null;
	}

}
