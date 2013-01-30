package de.tub.av.pe.identities;

import java.util.ArrayList;
import java.util.List;

public class IdentitiesRelation {
	
	private String firstIdentity;
	private String firstIdentityType;
	private List<String> secondIdentities = new ArrayList<String>();
	private String secondIdentitiesType;
	
	public String getFirstIdentity() {
		return firstIdentity;
	}
	public void setFirstIdentity(String firstIdentity) {
		this.firstIdentity = firstIdentity;
	}
	public String getFirstIdentityType() {
		return firstIdentityType;
	}
	public void setFirstIdentityType(String firstIdentityType) {
		this.firstIdentityType = firstIdentityType;
	}
	public List<String> getSecondIdentities() {
		return secondIdentities;
	}
	public void setSecondIdentities(List<String> secondIdentity) {
		this.secondIdentities = secondIdentity;
	}
	public String getSecondIdentitiesType() {
		return secondIdentitiesType;
	}
	public void setSecondIdentitiesType(String secondIdentityType) {
		this.secondIdentitiesType = secondIdentityType;
	}
	
}
