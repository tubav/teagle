package de.tub.av.pe.context;

import java.util.List;
import java.util.Map;

import de.tub.av.pe.rule.utils.LoggingBean;

public interface RequestContextInterface {

	public void setRequestContent(String httpContent);

	public String getRequestContent();

	public void setOriginatorIdentity(String id);

	public String getOriginatorIdentity();

	public void setEvent(String eventName);

	public String getEvent();

	public void setOriginator(String originator);

	public String getOriginator();

	public void setTarget(String target);

	public String getTarget();

	public Map<String, List<Object>> getParametersMap();

	public void setOpenPEContext(OpenPEContext openPEContext);

	public OpenPEContext getOpenPEContext();

	public LoggingBean.LogEntry getLogEntryGUI();

	public void setTemplateId(String templateId);

	public String getTemplateId();

	public void setTemplateVersion(String templateVersion);

	public String getTemplateVersion();

	public void enforceActionExecutionResult(int actionId,
			Object executionResult);

	boolean isRequest();

	void setisRequest(boolean isreq);

	void setOriginatorIdentityType(String type);

	String getOriginatorIdentityType();

	void setTargetIdentities(List<String> list);

	List<String> getTargetIdentities();

	void setTargetIdentitiesType(String type);

	String getTargetIdentitiesType();

	void addParameter(String name, Object value);
}
