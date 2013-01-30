
package de.tub.av.pe.pem1.processing;

import java.util.List;

import de.tub.av.pe.context.impl.RequestContextImpl;
import oma.xml.fokus.pem1_output_template.EnforcementAction;
import oma.xml.fokus.pem1_output_template.EnforcementActionOperation;
import oma.xml.fokus.pem1_output_template.EnforcementData;
import oma.xml.fokus.pem1_output_template.FOKUSOutputTemplateType;

public class RequestContextInCallableMode extends RequestContextImpl {

	private FOKUSOutputTemplateType policyOutputTemplate;

	EnforcementData enforcementData = new EnforcementData();

	/**
	 * Set evaluation output template.
	 * 
	 * @param policyOutputTemplate
	 */
	public void setEvalOutputTemplate(
			FOKUSOutputTemplateType policyOutputTemplate) {
		this.policyOutputTemplate = policyOutputTemplate;
		this.policyOutputTemplate.setEnforcementData(this.enforcementData);
	}

	/**
	 * Gets evaluation output template.
	 */
	public FOKUSOutputTemplateType getEvalOutputTemplate() {
		return this.policyOutputTemplate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void enforceActionExecutionResult(int actionId,
			Object executionResult) {
		EnforcementAction enforcAction = new EnforcementAction();
		enforcAction.setId(actionId);

		if (executionResult instanceof String)
			enforcAction
					.setEnforcementActionDescription((String) executionResult);
		else if (executionResult instanceof List) {
			enforcAction.getEnforcementActionOperation().addAll(
					(List<EnforcementActionOperation>) executionResult);
		}
		enforcementData.getEnforcementAction().add(enforcAction);
	}

	/**
	 * 
	 * @return
	 */
	public EnforcementData getEnforcemenData() {
		return this.enforcementData;
	}

}
