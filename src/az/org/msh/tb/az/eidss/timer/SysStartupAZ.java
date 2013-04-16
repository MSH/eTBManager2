package org.msh.tb.az.eidss.timer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.az.eidss.EidssIntHome;
import org.msh.tb.entities.SystemParam;
import org.msh.tb.entities.Workspace;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@Name("sysStartupAZ")
@Scope(ScopeType.APPLICATION)
public class SysStartupAZ{
	@In(create=true) SystemTimerAZ systemTimerAZ; 
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) EidssIntHome eidssIntHome;
	
	private Map<String, String> messages;
	private QuartzTriggerHandle quartz;
	private Date start;
	private Workspace observWorkspace;
	static Date now = new Date();

	static final long hour = 60*60*1000L;
	
	@Observer("org.jboss.seam.postInitialization")
	public void initTimerChecking() {
		EtbmanagerApp.instance().initializeInstance();

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2100);
				
		SystemParam p;
		try {
			p = App.getEntityManager().find(SystemParam.class, "admin.eidss.auto");
			start = getStart();				
			if ("true".equals(p.getValue())){
				start();
			}
		} catch (Exception e) {
			p = null;
		}
	}

	private Date getStart(){
		if (start == null){
			SystemParam st;
			st = App.getEntityManager().find(SystemParam.class, "admin.eidss.dateStart");
			if (st!=null)
				try {
					start = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(st.getValue());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			else
				start = new Date();
		}
		return start;
	}
	
	private void changeStartDt(){
		Date db = eidssIntHome.getConfig().getDateStart();
		if (!getStart().equals(db))
			start = now.before(db) ? db : start;
	}
	
	public void start(){
		if (quartz==null){
			boolean auto = false;
			try{
				eidssIntHome.loadConfig();
				changeStartDt();
				auto = eidssIntHome.getConfig().getAuto();
			}
			catch (Exception e) {
				String str = App.getEntityManager().find(SystemParam.class, "admin.eidss.auto").getValue();
				if ("true".equals(str))
					auto = true;
			}
			if (auto)
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

	public Workspace getObservWorkspace() {
		if (observWorkspace==null)
			observWorkspace = App.getEntityManager().find(Workspace.class, 8);
		return observWorkspace;
	}
}
