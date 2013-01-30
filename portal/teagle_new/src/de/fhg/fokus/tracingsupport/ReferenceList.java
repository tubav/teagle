package de.fhg.fokus.tracingsupport;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class ReferenceList {
	
	@XStreamImplicit(itemFieldName="reference")
	List<String> references;
	
	public List<String> getReferences(){
		return references;
	}
}
