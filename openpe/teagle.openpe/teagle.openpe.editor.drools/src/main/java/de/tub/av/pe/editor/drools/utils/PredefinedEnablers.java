package de.tub.av.pe.editor.drools.utils;

import java.util.ArrayList;

public class PredefinedEnablers {
	private  String[] enablersList = {"Global",
			"ApplicationRouter",
			"MultimediaConferenceService", 
			"ThirdPartyCallService", 
			"SendMessageService", 
			"PresenceConsumerService",
			"PresenceNotification",
			"TerminalLocationNotification",
			"TeagleResourceService"
			};

private String[] multimediaConferenceOp = {"createConference", "inviteParticipant"};
private String[] thirdPartyCallOp = {"makeCall", "endCall", "getCallInformation"};
private String[] sendMessageOp = {"sendMessage"};
private String[] presenceConsumerOp = {"getUserPresence", "subscribePresence", "getUserPresenceResponse"};
private String[] presenceNotifOp = {"statusChanged"};
private String[] appRouterOp = {"INVITE", "MESSAGE"};
private String[] terminalNotifOp = {"locationNotification"};
private String[] teagleResourceOp = {"useResource" , "connectResources", "provisionResource"};

public  ArrayList<String> getEnablersName()
{		
	ArrayList<String> list = new ArrayList<String>();

	for (int i = 0; i < enablersList.length; i++)
		list.add(enablersList[i]);
	
	return list;		
}


public  ArrayList<String> getEnablersOperationsName(String enablerName)
{		
	ArrayList<String> list = new ArrayList<String>();

	if(enablerName.equals("Global"))
	{
		list.add("*");
	}else if (enablerName.equals("MultimediaConferenceService"))
	{
		for (int i = 0; i < multimediaConferenceOp.length; i++)
			list.add(multimediaConferenceOp[i]);
	}else if (enablerName.equals("ThirdPartyCallService"))
	{
		for (int i = 0; i < thirdPartyCallOp.length; i++)
			list.add(thirdPartyCallOp[i]);			
	}else if (enablerName.equals("SendMessageService"))
	{
		for (int i = 0; i <  sendMessageOp.length; i++)
			list.add(sendMessageOp[i]);						
	}else if(enablerName.equals("PresenceConsumerService"))
	{
		for (int i = 0; i <  presenceConsumerOp.length; i++)
			list.add(presenceConsumerOp[i]);									
	}else if (enablerName.equals("PresenceNotification"))
	{
		for (int i = 0; i <  presenceNotifOp.length; i++)
			list.add(presenceNotifOp[i]);												
	}
	else if (enablerName.equals("ApplicationRouter"))
	{
		for (int i = 0; i <  appRouterOp.length; i++)
			list.add(appRouterOp[i]);												
	}
	else if (enablerName.equals("TerminalLocationNotification"))
	{
		for (int i = 0; i <  terminalNotifOp.length; i++)
			list.add(terminalNotifOp[i]);
	}
	else if (enablerName.equals("TeagleResourceService"))
	{
		for (int i = 0; i <  teagleResourceOp.length; i++)
			list.add(teagleResourceOp[i]);															
	}
	return list;		
}
}
