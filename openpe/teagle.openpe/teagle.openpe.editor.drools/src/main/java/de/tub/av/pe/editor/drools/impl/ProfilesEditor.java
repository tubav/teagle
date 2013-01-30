package de.tub.av.pe.editor.drools.impl;

public class ProfilesEditor extends IdentitiesEditor {

	public String getIdentityType() {
		return "profile";
	}

	public String getAssociatedIdentityType() {
		return "user";
	}

}
