package org.msh.tb.az.eidss.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.az.eidss.EIDSS;
import org.msh.tb.az.eidss.EidssIntConfig;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@Name("sysStartupAZ")
@Scope(ScopeType.APPLICATION)
public class SysStartupAZ{
	@In(create=true) SystemTimerAZ systemTimerAZ; 
	@In(create=true) FacesMessages facesMessages;
	
	private Map<String, String> messages;
	private QuartzTriggerHandle quartz;
	private EidssIntConfig config;
	private Date start;
	static Date now = new Date();

	static final long hour = 60*60*1000L;
	
	@Observer("org.jboss.seam.postInitialization")
	public void initTimerChecking() {
		EtbmanagerApp.instance().initializeInstance();

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2100);

		start = getStart();				
		if (getConfig().auto)
			start();
	}

	private Date getStart(){
		if (start == null){
			start = getConfig().dateStart;
			if (start == null)
				start = new Date();
		}
		return start;
	}
		
	private void changeStartDt(){
		config = EIDSS.loadConfig();
		Date db = getConfig().getDateStart();
		if (!getStart().equals(db))
			start = now.before(db) ? db : start;
	}

	public void start(){
		if (quartz==null){
			changeStartDt();	
			if (getConfig().getAuto())
				quartz = systemTimerAZ.trigger(start, getTimeInterval()*hour);
		}
		else
			try {
				if(quartz.getTrigger()==null){
					changeStartDt();
					quartz = systemTimerAZ.trigger(start, getTimeInterval()*hour);
				}
				else{
					quartz.cancel();
					start();
				}
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
	}

	private long getTimeInterval(){
		try{
			return getConfig().getInterval();
		}
		catch (Exception e){
			return 2;
		}
	}
	
	public void cancel(){
		if (quartz!=null)
			try {
				if (quartz.getTrigger()!=null){
					try {
						quartz.cancel();
					} catch (SchedulerException e) {
						e.printStackTrace();
					} 
				}
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
	}

	public void display(String mess, String onoff){
		String s = getMessages().get("admin.import.eidss.state")+": "
				+getMessages().get("admin.import.eidss.state."+onoff)+". "
				+getMessages().get("admin.import.eidss.nextFT")+": "
				+getNextFireTime();
		facesMessages.addFromResourceBundle(s);
		facesMessages.addFromResourceBundle(mess);
	}
	
	/**
	 * Return the current resource message file
	 * @return
	 */
	protected Map<String, String> getMessages() {
		if (messages == null)
			messages = Messages.instance();
		return messages;
	}
	
	public String getNextFireTime(){
		Trigger trig;
		String res = "";
		if (quartz!=null){
			try {
				trig = quartz.getTrigger();
				if (trig!=null)
					res = trig.getNextFireTime().toLocaleString();
				else
					res = getMessages().get("admin.import.eidss.nextFT.undef");
			} catch (SchedulerException e) {
				e.printStackTrace();
				res = e.getLocalizedMessage();
			}
		}
		else
			res = getMessages().get("admin.import.eidss.nextFT.undef");
		return res;
	}

	public EidssIntConfig getConfig() {
		if (config==null)
			config = EIDSS.loadConfig();
		return config;
	}

}
