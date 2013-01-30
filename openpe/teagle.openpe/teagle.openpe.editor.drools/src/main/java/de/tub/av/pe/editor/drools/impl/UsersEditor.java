package de.tub.av.pe.editor.drools.impl;


public class UsersEditor extends IdentitiesEditor
{

	@Override
	public String getIdentityType() {
		return "user";
	}

	@Override
	public String getAssociatedIdentityType() {
		return "profile";
	}
	
}
