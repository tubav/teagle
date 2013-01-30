package de.tub.av.pe.context.impl;

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.OpenPEContextManager;

public class OpenPEContextManagerImpl implements OpenPEContextManager{
	private OpenPEContext context;
	public OpenPEContextManagerImpl()
	{
		this.context = new OpenPEContextImpl();
	}
	
	@Override
	public OpenPEContext getInstance() {
		return context;
	}

}
