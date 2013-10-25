package org.msh.tb.az.eidss;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.az.eidss.timer.SysStartupAZ;
import org.msh.tb.entities.SystemParam;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;

/**
 * This class intended to configure and run export data from the EIDSS.
 * Manually by sysadmin or by timer event.
 * @author alexey
 *
 */
@Name("eidssIntHome")
@Scope(ScopeType.APPLICATION)
public class EidssIntHome {
	@In(required=false) Workspace defaultWorkspace;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) SysStartupAZ sysStartupAZ;
	/**
	 * import launch configuration
	 */
	private EidssIntConfig config=null;
	/**
	 * Prefix for parameters keys
	 */
	private final String prefix = "admin.eidss";
	private UserLogin userLogin;
	private Workspace workspace;
	private List<SelectItem> intervals;

	/**
	 * Show any message to UI
	 * @param message Any message
	 * @return 
	 */
	public void showMessage(String message) {
		//TODO show messages for background execution
		facesMessages.addFromResourceBundle(message);
	}


	/**
	 * Initialize GUI to make changes in the configuration
	 */
	@Begin(join=true)
	public void initConfigEditing() {
		getConfig();
	}

	/**
	 * Execute import asynchronously by user
	 * @return Success if import ran, or Error otherwise
	 */
	public String execute1(){
		saveConfig();
		return execute();
	}

	/**
	 * Execute import asynchronously
	 * @return Success if import ran, or Error otherwise
	 */
	public String execute(){
		//setDefaultWorkspace();
		config = EIDSS.loadConfig();
		setCurrentUser();
		if (userLogin!=null){
			TaskManager manager = (TaskManager)Component.getInstance(TaskManager.class);
			if (manager != null){
				Map<String,Object> params = new HashMap<String, Object>();
				params.put("config", getConfig());
				params.put("userId", userLogin.getId());
				if (getTask() == null){
					manager.runTask(EidssTaskImport.class, params);
					sysStartupAZ.display("eidss.import.started","on");
					//showMessage("eidss.import.started");
				}else{
					sysStartupAZ.display("eidss.import.starting","on");
					//showMessage("eidss.import.starting");
				}
			}
		}
		return "Success";
	}

	public EidssTaskImport getTask(){
		TaskManager taskManager = (TaskManager)Component.getInstance(TaskManager.class);
		EidssTaskImport task = (EidssTaskImport) taskManager.findTaskByClass(EidssTaskImport.class);
		return task;
	}

	/**
	 * Stop current async task
	 */
	public void stop(){
		EidssTaskImport task = getTask();
		if (task != null){
			sysStartupAZ.cancel();
			task.cancel();
			sysStartupAZ.display("eidss.import.canceled","off");
			//showMessage("eidss.import.canceled");
		}else{
			sysStartupAZ.cancel();
			sysStartupAZ.display("eidss.import.idle","off");
			//showMessage("eidss.import.idle");
		}
	}

	/**
	 * check state of the current async task
	 */
	public void check(){
		if (getTask() != null){
			sysStartupAZ.display(EidssTaskImport.getStateMessage(),"on");
			//showMessage(EidssTaskImport.getStateMessage());
		}else{
			sysStartupAZ.display("eidss.import.idle","off");
			//showMessage("eidss.import.idle");
		}
	}

	/**
	 * Check if integration is running
	 * @return true, if export task running
	 */
	public boolean isTaskRunning() {
		//TODO body
		return true;
	}

	/**
	 * Automatically execute by timer event
	 * @return 
	 */
	@Observer("system-timer-event")
	public void systemTimerListener() {
		System.out.println("Executing automatic EIDSS integration...");
		execute();
	}
	/**
	 * get configuration, load from database if empty
	 * @return
	 */
	public EidssIntConfig getConfig(){
		if (config == null){
			config = EIDSS.loadConfig();
		}
		return config;
	}
		
	/**
	 * save configuration to the SysConfig table
	 * use reflection to determine keys name and fixed prefix for these keys
	 */
	public void saveConfig() {
		if (config != null){
			Field[] flds = config.getClass().getFields();
			for(Field fld : flds){
				try {
					if (fld.getType().getName().contains("Date")){
						Date dt = (Date) fld.get(config);
						SimpleDateFormat sdf;
						if (dt.getHours()==0 && dt.getMinutes()==0)
							sdf = new SimpleDateFormat("yyyy-MM-dd");
						else
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						saveParameter(prefix + "." + fld.getName(),sdf.format(dt));
					}else
						saveParameter(prefix + "." + fld.getName(),fld.get(config).toString());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

	}
	

	private EntityManager getEntityManager(){
		return (EntityManager)Component.getInstance("entityManager");
	}
	

	/**
	 * Save configuration parameter to the common config table
	 * @param key
	 * @param value
	 */
	protected void saveParameter(String key, String value) {
		SystemParam p;
		try {
			p = getEntityManager().find(SystemParam.class, key);
		} catch (Exception e) {
			p = null;
		}

		if (p == null) {
			p = new SystemParam();
			p.setKey(key);
			//TODO ?

			//defaultWorkspace = entityManager.merge(defaultWorkspace);
			p.setWorkspace(EIDSS.getObservWorkspace());
		}
		p.setValue(value.toString());
		getEntityManager().persist(p);
		getEntityManager().flush();
	}

	/**
	 * Notify user about current operation.
	 * @return notify message
	 * @return 
	 */
	public String notifyUser() {
		throw new UnsupportedOperationException();
	}



	/**
	 * 
	 * @return 
	 */
	public void setDefaultWorkspace() {
		try{
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			if (defaultWorkspace==null)
				if (userLogin==null)
					defaultWorkspace = em.find(Workspace.class, 8);
				else{
					if(getUserLogin().getLoginDate()!=null)
						workspace = em.find(Workspace.class, getUserLogin().getDefaultWorkspace().getId());
					else
						defaultWorkspace = em.find(Workspace.class, 8);
				}
			if (workspace==null){
				workspace = defaultWorkspace;
				Contexts.getApplicationContext().set("defaultWorkspace", defaultWorkspace);
			}
		}
		catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	public UserLogin getUserLogin() {
		return userLogin;
	}


	public void setUserLogin(UserLogin userLogin) {
		this.userLogin = userLogin;
	}

	/**
	 * 
	 * @return 
	 */
	public void setCurrentUser() {
		if (userLogin == null)
			userLogin = (UserLogin)Component.getInstance("userLogin");
		if (userLogin == null){
			userLogin = new UserLogin();
			userLogin.setWorkspace(EIDSS.getObservWorkspace());
			if (getConfig().getDefaultUser()!=null){
				User us = (User) getEntityManager().find(User.class, getConfig().getDefaultUser());
				userLogin.setUser(us);
				//userLogin.setId(us.getId());
				Query q = getEntityManager().createQuery("from UserWorkspace uw where uw.workspace.id="+EIDSS.getObservWorkspace().getId()+" and uw.user.id=:uid")
				.setParameter("uid", us.getId());
				UserWorkspace uw = (UserWorkspace) q.getResultList().get(0);
				Contexts.getApplicationContext().set("userWorkspace", uw);
				Contexts.getApplicationContext().set("userLogin", userLogin);
			}
			else
				userLogin = null;
		}
	}


	public Workspace getWorkspace() {
		if (workspace == null)
			return EIDSS.getObservWorkspace();
		return workspace;
	}

	public void saveAndExecute(){
		saveConfig();
		if (config.auto)
			sysStartupAZ.start();
		else
			sysStartupAZ.cancel();
		//check();
	}

	public List<SelectItem> getIntervals() {
		if (intervals==null){
			intervals = new ArrayList<SelectItem>();
			for (int i = 1; i <= 6; i++) {
				SelectItem si = new SelectItem(i*2, Integer.toString(i*2));
				intervals.add(si);
			}
		}
		return intervals;
	}


	public void rewriteDatesInConfig() {
		setDefaultWorkspace();
		int deep=0;
		if (getConfig()!=null){
			if (config.getDeepdays()!=null)
				deep = config.getDeepdays();
			else
				deep = 2;
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			config.setToDate(today);
			saveParameter(prefix+".toDate", sdf.format(today));

			Calendar d = Calendar.getInstance();
			d.setTime(today);
			d.add(Calendar.DAY_OF_YEAR, -deep);
			config.setFrom(d.getTime());
			saveParameter(prefix+".from", sdf.format(d.getTime()));
		}
	}
	
	@Override
	public String toString() {
		return "EidssIntHome";
	}
}
