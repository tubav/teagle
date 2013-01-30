
package de.tub.av.pe.rule.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


/**
 * Helper class that provides methods for populating the log overview page elements.
 * @author Simon Dutkowski, Irina Boldea
 */
public class LoggingBean {

	private List<LogEntry> entries = Collections.synchronizedList(new ArrayList<LogEntry>());
	private long entryid = 0;
    
    public LogEntry createNewEntry() 
    {
    	LogEntry entry;
    	synchronized (entries) {
        	if(entries.size() == 100)
        	{
        		entries.remove(entries.size()-1);
        	}
        	entry = new LogEntry();
        	entry.setTime(now("hh:mm:ss-dd:MM:yy"));        	
        	entry.setId(entryid++);
        	entries.add(0, entry);			
		}
    	return entry;
    }

    private static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
      }

    
    public List<LogEntry> getLogEntries() {
    	return entries;
    }

    public static class LogEntry {
    	
    	private String finalDecission = "UNKNOWN";
    	
    	private String originator = "*";

    	private String target = "*";
    	private String event = "";
    	private String time = "";
    	
    	private boolean error = false;

    	private ReasonOverview reason ;

		private String originatorType = "*";

		private String targetType = "*";
		private long id = 0;

    	/**
    	 * 
    	 * @retur
    	 */
    	public synchronized boolean isError() {
    		return error;
    	}
    	/**
    	 * 
    	 * @param error
    	 */
    	public synchronized void setError(boolean error) {
    		this.error = error;
    	}
    	/**
    	 * 
    	 * @return
    	 */
    	public  synchronized String getFinalDecission() {
			return finalDecission;
		}
    	/**
    	 * 
    	 * @param finalDecission
    	 */
		public synchronized  void setFinalDecission(String finalDecission) {
			this.finalDecission = finalDecission;
		}
		public String getTime()
		{
			return time;			
		}
		
		public void setTime(String execTime)
		{
			time+=execTime;
		}
		
		/**
		 * 
		 * @return
		 */
		public synchronized  ReasonOverview getReason() {
			return reason;
		}
		/**
		 * 
		 * @param reason
		 */
		public synchronized  void setReason(ReasonOverview reason) {
			this.reason = reason;
		}
		/**
		 * 
		 * @return
		 */
		public synchronized String getOriginator() {
			return originator;
		}
		/**
		 * 
		 * @param originator
		 */
		public synchronized  void setOriginator(String originator) {
			if(originator != null && !originator.equals(""))
				this.originator = originator;
		}
		
		public synchronized  void setOriginatorType(String originatorType) {
			if(originatorType != null && !originatorType.equals(""))
				this.originatorType = originatorType;
		}

		public synchronized String getOriginatorType() {
				return this.originatorType;
		}

		public synchronized  void setTargetsType(String targetType) {
			if(targetType != null && !targetType.equals(""))
				this.targetType = targetType;
		}

		public synchronized  String getTargetType() {
			return this.targetType;
		}

    	/**
    	 * 
    	 * @return
    	 */
		public synchronized  String getTarget() {
			return target;
		}
		/**
		 * 
		 * @param target
		 */
		public synchronized  void setTargets(List<String> targetList) 
		{
			if(targetList.size() != 0)
			{
				this.target = targetList.get(0);
			}
			for (int i = 1; i < targetList.size(); i++)
			{
				this.target +=",<br/>"+targetList.get(i);
			}
		}
		/**
		 * 
		 * @return
		 */
		public synchronized String getEvent() {
			return event;
		}
		/**
		 * 
		 * @param operation
		 */
		public synchronized void setEvent(String event) {
			this.event = event;
		}
		
		public void setId(long id)
		{
			this.id = id;
		}
		
		public long getId()
		{
			return this.id;
		}
		
    }
}
