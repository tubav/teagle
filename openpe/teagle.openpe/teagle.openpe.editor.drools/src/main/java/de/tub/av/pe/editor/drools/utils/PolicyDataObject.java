package de.tub.av.pe.editor.drools.utils;


import java.util.ArrayList;
import java.util.List;


public class PolicyDataObject {


	public PolicyDataObject(){}
	

	public List<String> getListEvents() {
		List<String> events = new ArrayList<String>();

			events.add("MultimediaConferenceService-createConference");
			events.add("MultimediaConferenceService-*");
			events.add("ThirdPartyCallService-makeCall");
			events.add("ThirdPartyCallService-*");
			events.add(0, "Global-*");
		return events;
	}
}
