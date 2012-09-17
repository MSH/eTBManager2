package org.msh.tb.az.eidss.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.az.eidss.EidssIntHome;
import org.msh.tb.entities.SystemParam;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@Name("sysStartupAZ")
@Scope(ScopeType.APPLICATION)
public class SysStartupAZ{
	@In EntityManager entityManager;
	@In(create=true) SystemTimerAZ systemTimerAZ; 
	@In(create=true) EidssIntHome eidssIntHome;
	@In(create=true) FacesMessages facesMessages;
	
	private Map<String, String> messages;
	private QuartzTriggerHandle quartz;
	private Date start;

	static final long hour = 60*60*1000L;
	@Observer("org.jboss.seam.postInitialization")
	public void initTimerChecking() {
		EtbmanagerApp.instance().initializeInstance();

		//System.out.println("Initializing shedulled tasks...");

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2100);
				
		SystemParam p;
		SystemParam st;
		try {
			p = entityManager.find(SystemParam.class, "admin.eidss.auto");
			if ("true".equals(p.getValue())){
				st = entityManager.find(SystemParam.class, "admin.eidss.dateStart");
				if (st!=null)
					start = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(st.getValue());
				else
					start = new Date();					
				start();
			}
		} catch (Exception e) {
			p = null;
		}
	}

	public void start(){
		if (quartz==null){
			eidssIntHome.setDefaultWorkspace();
			eidssIntHome.loadConfig();
			if (eidssIntHome.getConfig().getAuto() == true)
				quartz = systemTimerAZ.trigger(start, getTimeInterval()*hour);
		}
		else
			try {
				if(quartz.getTrigger()==null)
					quartz = systemTimerAZ.trigger(start, getTimeInterval()*hour);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
	}

	private long getTimeInterval(){
		try{
			return eidssIntHome.getConfig().getInterval();
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
		facesMessages.add(getMessages().get("admin.import.eidss.state")+": "
				+getMessages().get("admin.import.eidss.state."+onoff)+". "
				+getMessages().get("admin.import.eidss.nextFT")+": "
				+getNextFireTime());
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
}
