package org.msh.tb.az.eidss;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.application.tasks.TaskStatus;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.date.LocaleDateConverter;

import com.bv.eidss.ArrayOfHumanCaseListInfo;
import com.bv.eidss.HumanCaseInfo;
import com.bv.eidss.HumanCaseListInfo;

import ua.com.theta.bv.client.Loader;


@Name("eidssTaskImport")
public class EidssTaskImport extends AsyncTaskImpl {

	EidssIntHome eidssIntHome;
	@In TaskManager taskManager;
	List<String> cases;
	List<CaseInfo> infoForExport=new ArrayList<CaseInfo>();
	CaseImporting ci;
	private EidssIntConfig config;
	public static String stateMessage;
	private StringBuffer log = new StringBuffer();
	private UserTransaction transaction;
	private Loader loader;
	private static long beginMills;
	private List<String> caseIds = new ArrayList<String>();


	@Override
	protected void starting() {
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); //to avoid JBoss bug
		initContext();
		setStateMessage("eidss.import.starting");
		addLog("Started");
		Calendar beginExec = GregorianCalendar.getInstance();
		beginMills = beginExec.getTimeInMillis();
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		setStateMessage("eidss.import.finished");
		addLog("Finished");
		// save result to log file
		beginTransaction();
		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "EIDSS Integration", null, null);
		commitTransaction();
	}

	/**
	 * Execute the task
	 */
	@Override
	public void execute() {	
		setStateMessage("eidss.import.setting.environment");
		config = (EidssIntConfig)getParameter("config");
		if (config != null ){
			if (loginEIDSS()){
				if (notCanceled()){
					addLog("INFO login to EIDSS successfull");
					boolean success =loadCasesList();
					sayAboutLoad(success);
					if((caseIds.size()>0) && notCanceled()){
						addLog("INFO begin load full cases");
						Iterator<String> it = caseIds.iterator();
						while (it.hasNext()){
							String caseId = it.next();
							setStateMessage("load case " + caseIds.indexOf(caseId) + " total to load " + caseIds.size());
							loadCaseById(it.next());
						}
						if ((infoForExport.size()> 0) && notCanceled()){
							exportToDB();
						}
					}
				}
			} else{
				addLog("Error unable to login EIDSS " + getLoader().getErrorMessage());
			}
		}else{
			addLog("Error wrong parameters");
		}
	}
	/**
	 * Export cases to eTBManager
	 */
	private void exportToDB() {
		// TODO Auto-generated method stub
		
	}



	/**
	 * Do not continue the task exec in cancelled state!
	 * @return true, if not cancelled
	 */
	private boolean notCanceled() {
		return !(getStatus().equals(TaskStatus.CANCELING) ||
				getStatus().equals(TaskStatus.CANCELED) ||
				getStatus().equals(TaskStatus.FINISHING) ||
				getStatus().equals(TaskStatus.FINISHED) ||
				getStatus().equals(TaskStatus.ERROR));
	}



	/**
	 * write log message about load
	 * @param success
	 */
	private void sayAboutLoad(boolean success) {
		String mess = "";
		if (caseIds.size() > 0){
			mess = "total found " + caseIds.size() + " cases in any state";
			if (!success)
				mess = mess + " but some errors occured, see above";
		}else{
			if (!success)
				mess = "ERROR load case list";
			else
				mess = "total found 0 cases, check dates and diagnosis";
		}
		addLog(mess);
	}



	@Override
	public void cancel(){
		addLog("Canceled");
		// save result to log file
		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "EIDSS Integration", null, null);
		super.cancel();
	}
	/**
	 * Determine cases ID by date interval from begin date to today and for each diagnosis
	 * @return
	 */
	private boolean loadCasesList() {
		setStateMessage("eidss.import.load.data");
		boolean noError = true;
		// determine dates, diagnosis and states
		Calendar loadDate = GregorianCalendar.getInstance();
		Calendar currentDate = GregorianCalendar.getInstance();
		loadDate.setTime(config.getFrom());
		currentDate.setTime(config.getToDate());
		String[] diagnosis = config.getDiagnosis().split(",");
		while(loadDate.before(currentDate)){
			//load by date and diagnosis, (state only in the full case)
			String loadMess = getLoadMess(loadDate);
			setStateMessage(loadMess);
			addLog(loadMess);
			for(String diag : diagnosis){
				if (notCanceled()){
					ArrayOfHumanCaseListInfo caseList = loader.getTBCasesList(loadDate, diag);
					if (caseList != null){
						Iterator<HumanCaseListInfo> it = caseList.getHumanCaseListInfo().iterator();
						int i = 0;
						while(it.hasNext()){
							HumanCaseListInfo info = it.next();
							caseIds.add(info.getCaseID());
							i++;
						}
						addLog("total - " + i);
					}else{
						addLog("total 0");
						String errorMess = loader.getErrorMessage();
						if (errorMess.length() > 0){
							noError = false;
							addLog("ERROR " + errorMess);
						}
					}
					loadDate.add(Calendar.DATE, 1); //next date
				}else
					return noError;
			}
		}
		return noError;
	}


	/**
	 * Get nice message for facet and log
	 * @param loadDate data to load
	 * @return
	 */
	private String getLoadMess(Calendar loadDate) {
		return "start load " + loadDate.get(Calendar.YEAR)+"-" +
		(loadDate.get(Calendar.MONTH)  +1) +"-"+
		loadDate.get(Calendar.DATE)+"-"+
		" diagnosis " + config.getDiagnosis();
	}



	/**
	 * because of async character of task, we are need to init
	 * execution context
	 */
	private void initContext() {
		// initializing context variables
		User user = getUser();
		UserWorkspace ws = (UserWorkspace)getEntityManager().createQuery("from UserWorkspace ws where ws.user.id = :uid and ws.workspace.id = :id")
		.setParameter("uid", user.getId())
		.setParameter("id", getWorkspace().getId())
		.getSingleResult();
		Contexts.getEventContext().set("userWorkspace", ws);
		// avoid lazy initialization problems
		ws.getTbunit().getAdminUnit().getId();
		UserLogin userLogin = new UserLogin();
		userLogin.setUser(user);
		userLogin.setWorkspace(getWorkspace());
		Contexts.getEventContext().set("userLogin", userLogin);
		LocaleSelector.instance().setLocaleString(getWorkspace().getDefaultLocale());

	}
	/**
	 * get current user by task parameter
	 */
	@Override
	public User getUser(){
		if (super.getUser() == null){
			Integer userId = (Integer) getParameter("userId");
			if (userId != null){
				User user = (User)getEntityManager().createQuery("from User u where u.id = :uid")
				.setParameter("id", userId)
				.getSingleResult();
				if (user != null){
					setUser(user);
					return user;
				}
				else throw new RuntimeException("No user found for EIDSS import task");
			} else{
				throw new RuntimeException("No user found for EIDSS import task");
			}
		}else return super.getUser();
	}

	/**
	 * login to the EIDSS web services
	 * @return 
	 */
	public boolean loginEIDSS() {
		Loader loader = getLoader();
		setStateMessage("eidss.import.logging.in");
		return loader.login(config.getOrgName(), config.getLogin(),
				config.getPassword(), config.getLanguage(), config.getTimeout());

	}
	/**
	 * Get the EIDSS data loader
	 * @return
	 */
	public Loader getLoader() {
		if (loader == null){
			loader = new Loader();
		}
		return loader;
	}



	public void setLoader(Loader loader) {
		this.loader = loader;
	}




	/**
	 * Load case by case id from previous created case id list
	 * @param id case ID
	 */
	public void loadCaseById(String id) {
		if (notCanceled()){
			HumanCaseInfo info = getLoader().getFullCase(id);
			if (info != null){
				addCase(info);
			}else{
				addLog("ERROR " + getLoader().getErrorMessage());
			}
		}
	}
	/**
	 * Convert case info to CaseInfo DTO and add this DTO to the result list
	 * Convert only info in allowed by config states
	 * @param info case info from the web service
	 */
	private void addCase(HumanCaseInfo info) {
		// TODO Auto-generated method stub

	}



	/**
	 * If any real import occurred - rollback by delete imported cases
	 * @return 
	 */
	public boolean rollBack() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set special variable to cancel database record loop
	 * @return 
	 */
	public void cancelRecordLoop() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return human readable task message to show
	 * @return 
	 */
	public static String getStateMessage() {
		Calendar cal = GregorianCalendar.getInstance();
		long duration = cal.getTimeInMillis()-beginMills;
		return EidssTaskImport.stateMessage + " elapsed " + duration + " (ms)";
	}



	public void setStateMessage(String stateMessage) {
		EidssTaskImport.stateMessage = stateMessage;
	}

	/**
	 * Add log message
	 * @param s
	 */
	public void addLog(String s) {
		Calendar cal = GregorianCalendar.getInstance();
		Date dt = cal.getTime();
		String dateStamp = LocaleDateConverter.getDisplayDate(dt, true);
		log.append(dateStamp + " " + s);
		log.append('\n');
	}
	/**
	 * Return the {@link EntityManager} instance in use
	 * @return
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	/**
	 * Start a new transaction
	 */
	public void beginTransaction() {
		try {
			getTransaction().begin();
			getEntityManager().joinTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	/**
	 * Commit a transaction that is under progress 
	 */
	public void commitTransaction() {
		try {
			getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	/**
	 * Return the transaction in use by the task
	 * @return
	 */
	protected UserTransaction getTransaction() {
		if (transaction == null)
			transaction = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		return transaction;
	}

}