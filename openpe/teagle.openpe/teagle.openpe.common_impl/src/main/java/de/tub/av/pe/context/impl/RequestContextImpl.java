
package de.tub.av.pe.context.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tub.av.pe.context.OpenPEContext;
import de.tub.av.pe.context.RequestContextInterface;
import de.tub.av.pe.rule.utils.ReasonOverview;
import de.tub.av.pe.rule.utils.LoggingBean.LogEntry;

public abstract class RequestContextImpl implements RequestContextInterface
{
		
	private String requestContent = null;

	private String originatorIdentity = null;
	
	private List<String> targetIdentity = null;
		
	private String event = null;
	
	private String originatorApplicationIP;
	
	private Map<String, List<Object>> parametersMap = new HashMap<String, List<Object>>();
	
	private OpenPEContext openPEContext;
		
	private String target; 
	
	private ReasonOverview reasonBean;

	private LogEntry logEntry;
		
	private String templateId;
	
	private String templateVersion;

	private	Boolean isreq;

	private String originatorIdentityType;

	private String targetIdentitiesType;
	
	/**
	 * 
	 * @param httpContent
	 */
	@Override
	public void setRequestContent(String httpContent)
	{
		this.requestContent = httpContent;
	}
	/**
	 * 
	 * @return
	 */
	@Override
	public String getRequestContent()
	{
		return requestContent;
	}
	/**
	 * 
	 * @param id
	 */
	@Override
	public void setOriginatorIdentity(String id)
	{
		this.originatorIdentity = id;
	}
	
	@Override
	public void setOriginatorIdentityType(String type)
	{
		this.originatorIdentityType = type;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getOriginatorIdentity()
	{
		return this.originatorIdentity;
	}
	
	@Override
	public String getOriginatorIdentityType()
	{
		return this.originatorIdentityType;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getTargetIdentities()
	{
		if(this.targetIdentity == null)
			this.targetIdentity = new ArrayList<String>();
		return this.targetIdentity;
	}
	
	@Override
	public void setTargetIdentities(List<String> list)
	{
		this.targetIdentity = list;
	}
	
	@Override
	public void setTargetIdentitiesType(String type)
	{
		this.targetIdentitiesType = type;
	}
	@Override
	public String getTargetIdentitiesType()
	{
		return this.targetIdentitiesType;
	}
	
	/**
	 * 
	 * @param originatorAppIp
	 */
	@Override
	public void setOriginator(String originator)
	{
		this.originatorApplicationIP = originator;
	}
	/**
	 * 
	 * @return
	 */
	@Override
	public String getOriginator()
	{
		return this.originatorApplicationIP;
	}

	/**
	 * Operation parameters Map
	 * @return
	 */
	@Override
	public Map<String, List<Object>> getParametersMap(){			
		return this.parametersMap;
	}

	/**
	 * Add a method parameter
	 * @param name
	 * @param value
	 */
	@Override
	public void addParameter(String name, Object value)
	{
		if (this.parametersMap.containsKey(name) == false)
		{
			List<Object> paramValueList = new ArrayList<Object>();
			paramValueList.add(value);
			this.parametersMap.put(name, paramValueList);
		}
		else
			this.parametersMap.get(name).add(value);
	}	
	/**
	 * 
	 */
	public void initializeEvalResultReasonGUI()
	{
		this.logEntry = this.openPEContext.getLoggingBeanObject().createNewEntry();		
    	this.reasonBean = new ReasonOverview();
    	logEntry.setReason(reasonBean);
	}
	
	/**
	 * Gets log entry GUI object.
	 * @return
	 */
	@Override
	public LogEntry getLogEntryGUI()
	{
		return this.logEntry;
	}
	
	/**
	 * Sets the template id of the input message.
	 * @param templateId
	 */
	@Override
	public void setTemplateId(String templateId)
	{
		this.templateId = templateId;
	}

	/**
	 * Gets the template id of the input message.
	 * @return
	 */
	@Override
	public String getTemplateId()
	{
		return this.templateId;
	}
	
	/**
	 * Sets the template version of the input message.
	 * @param templateVersion
	 */
	@Override
	public void setTemplateVersion(String templateVersion)
	{
		this.templateVersion = templateVersion;
	}
	
	/**
	 * Gets the template version of the input message.
	 * @return
	 */
	@Override
	public String getTemplateVersion()
	{
		return this.templateVersion;
	}
	
	public abstract void enforceActionExecutionResult(int actionId, Object executionResult);

	@Override
	public void setEvent(String eventName) {
		this.event = eventName;
	}

	@Override
	public String getEvent() {
		return this.event;
	}

	@Override
	public void setOpenPEContext(OpenPEContext openpeContext) {
		
		this.openPEContext = openpeContext;
	}

	@Override
	public OpenPEContext getOpenPEContext() {
		
		return this.openPEContext;
	}

	@Override
	public void setTarget(String target) {
		this.target = target;
	}
	@Override
	public String getTarget() {
		return target;
	}
	
	@Override
	public boolean isRequest()
	{
		return isreq;
	}
	@Override
	public void setisRequest(boolean isreq)
	{
		this.isreq = isreq;
	}

}



