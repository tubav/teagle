package de.fhg.fokus.tracingsupport;
import com.thoughtworks.xstream.annotations.XStreamAlias;

public class Config {
	
	String uuid;
	
	@XStreamAlias("sink_ip")
	String sinkIp;
	

	@XStreamAlias("capture_filter")
	String captureFilter;
	
	@XStreamAlias("display_filter")
	String displayFilter;
	
	@XStreamAlias("send_protocol")
	String sendProtocol;
	
	boolean started;
	
	@XStreamAlias("traces_type")
	String tracesType;
	
	@XStreamAlias("sink_port")
	String sinkPort;

	public String getUuid() {
		return uuid;
	}
	
	public String getSinkIp() {
		return sinkIp;
	}
	
	public String getCaptureFilter() {
		return captureFilter;
	}
	
	public String getDisplayFilter() {
		return displayFilter;
	}
	
	public String getSendProtocol() {
		return sendProtocol;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public String getTracesType() {
		return tracesType;
	}
	
	public String getSinkPort() {
		return sinkPort;
	}
}
