package de.tub.av.pe.eval.drools;

import java.util.List;

import de.tub.av.pe.context.RequestContextInterface;

public class PEInputRequest {

	private String originator;
	private String originatorIdentity;
	private String originatorIdentityType;
	private String target;
	private String targetIdentityType;
	private String event;
	private List<String> targetIdentities;
	
	public PEInputRequest(RequestContextInterface reqContext) {
		this.event = reqContext.getEvent();

		this.originator = reqContext.getOriginator();
		this.originatorIdentity = reqContext.getOriginatorIdentity();
		this.originatorIdentityType = reqContext.getOriginatorIdentityType();

		this.target = reqContext.getTarget();
		this.targetIdentities = reqContext.getTargetIdentities();
		this.targetIdentityType = reqContext.getTargetIdentitiesType();
	}

	public String getOriginator() {
		return originator;
	}

	public String getOriginatorIdentity() {
		return originatorIdentity;
	}

	public String getOriginatorIdentityType() {
		return originatorIdentityType;
	}

	public String getTarget() {
		return target;
	}

	public List<String> getTargetIdentities() {
		return targetIdentities;
	}

	public String getTargetIdentityType() {
		return targetIdentityType;
	}

	public String getEvent() {
		return event;
	}
}

