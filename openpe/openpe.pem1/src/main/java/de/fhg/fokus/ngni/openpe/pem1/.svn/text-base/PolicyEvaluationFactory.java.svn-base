package de.fhg.fokus.ngni.openpe.pem1;

public class PolicyEvaluationFactory {

	private String endpoint;
	
	public PolicyEvaluationFactory(String endpoint) {
		this.endpoint = endpoint;
	}

	public PolicyEvaluationFactory() {
		this.endpoint = null;
	}
	public PolicyEvaluation createRequestPolicyEvaluation(String originator, String target) {
		PolicyEvaluation pe = new PolicyEvaluation(endpoint, true);
		pe.setOriginatorID(originator);
		pe.addTargetID(target);
		return pe;
	}

	public PolicyEvaluation createRequestPolicyEvaluation() {
		return new PolicyEvaluation(endpoint, true);
	}

	public PolicyEvaluation createResponsePolicyEvaluation() {
		return new PolicyEvaluation(endpoint, false);
	}
}
