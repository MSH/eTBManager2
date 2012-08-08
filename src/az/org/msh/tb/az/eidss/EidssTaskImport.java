package org.msh.tb.az.eidss;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;
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

import ua.com.theta.bv.client.Loader;

import com.bv.eidss.AddressInfo;
import com.bv.eidss.ArrayOfHumanCaseListInfo;
import com.bv.eidss.HumanCaseInfo;
import com.bv.eidss.HumanCaseListInfo;


@Name("eidssTaskImport")
public class EidssTaskImport extends AsyncTaskImpl {

	EidssIntHome eidssIntHome;
	@In TaskManager taskManager;
	
	List<CaseInfo> infoForExport=new ArrayList<CaseInfo>();
	CaseImporting ci;
	private EidssIntConfig config;
	public static String stateMessage;
	private StringBuffer log = new StringBuffer();
	private UserTransaction transaction;
	private Loader loader;
	private static long beginMills;
	//case info in EIDSS system
	private class CaseShortInfo {
		public String caseId;   //case id 
		public Date caseDate;   //case entered date 
		public CaseShortInfo(String id, Date d){
			this.caseId=id;
			this.caseDate=d;
		}
	}
	private List<CaseShortInfo> caseIds = new ArrayList<CaseShortInfo>();
	private List<CaseShortInfo> caseIdsClean = new ArrayList<CaseShortInfo>();
	private String refList;

	@Override
	protected void starting() {
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); //to avoid JBoss bug
		initContext();
		setStateMessage("eidss.import.starting");
		addTimeLog("Started");
		Calendar beginExec = GregorianCalendar.getInstance();
		beginMills = beginExec.getTimeInMillis();
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		setStateMessage("eidss.import.finished");
		addTimeLog("Finished");
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
	 ci=new CaseImporting();
		setStateMessage("eidss.import.setting.environment");
		config = (EidssIntConfig)getParameter("config");
		if (config != null ){
			if (loginEIDSS()){
				if (notCanceled()){
					addLog("INFO login to EIDSS successfull");
					addLog("URL: "+config.getUrl());
					addLog("Diagnosis: "+config.getDiagnosis());
					addLog("States to reject: "+config.getCaseStates());
					//Calendar c1=Calendar.getInstance();
					addLog("EIDSS date: from "+ DateFormat.getDateInstance().format(config.getFrom())+
							" to "+DateFormat.getDateInstance().format(config.getToDate()));
					boolean success =loadCasesList();
					sayAboutLoad(success);
					if((caseIds.size()>0) && notCanceled()){
						Iterator<CaseShortInfo> it = caseIds.iterator();
						while (it.hasNext()){
							CaseShortInfo caseI = it.next();
							if (!ci.CheckIfExistInEtb(caseI.caseId)){
								caseIdsClean.add(caseI);
							}
						}
					}
					sayAboutCheckList();
					if((caseIdsClean.size()>0) && notCanceled()){
						Iterator<CaseShortInfo> it = caseIdsClean.iterator();
						 refList="";
						while (it.hasNext()){
							CaseShortInfo caseI = it.next();
							//setStateMessage("load case " + caseIdsClean.indexOf(caseId) + " total to load " + caseIdsClean.size());
							refList=refList+" "+loadCaseById(caseI);
						}
						
						
					}
					if ((infoForExport.size()> 0) && notCanceled()){
						exportToDB();
					}
				}
				Integer reject=caseIdsClean.size()-infoForExport.size();
				if (reject>0) {
					String slog="Totally "+reject.toString() +" case(s) rejected";
					if (!refList.trim().equalsIgnoreCase("")) {
						slog=slog+": "+refList;
					}
						addLog(slog);
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
		Integer iw=0;
		Integer ia=0;
		
		Iterator<CaseInfo> it=infoForExport.iterator();
		while (it.hasNext()){
			CaseInfo c = it.next();
			String result="";
			if (config.auto){
				result=ci.checkImportRecords(c);
			} else {
				 result=ci.importRecords(c);
			}
			if (!result.equalsIgnoreCase("error")){
				Integer age=c.getAge();
				String toLog=c.getLastName()+" "+c.getFirstName()+" "+c.getMiddleName()+", age "+age.toString()+", diag "+DateFormat.getDateInstance().format(c.getFinalDiagnosisDate())+", "+c.getCaseID();
				String entDate=DateFormat.getDateInstance().format(c.getEnteringDate());
				if (result.equalsIgnoreCase(CaseImporting.WRITED)){
					iw=iw+1;
					addLog(entDate+" + "+toLog);
				}
				if (result.equalsIgnoreCase(CaseImporting.UPDATED)){
					ia=ia+1;
					addLog(entDate+" ^ "+toLog);
				}
			}else
				addLog("Error writing  "+c.getLastName());
		}
		addLog(iw.toString()+ " cases written successfully");
		addLog(ia.toString()+ " cases updated successfully");
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
			mess = "total found " + caseIds.size() + " cases in all states";
			if (!success)
				mess = mess + " but some errors occured, see above";
		}else{
			if (!success)
				mess = "ERROR load case list";
			else
				mess = "total found 0 cases, check dates and diagnosis";
		}
		addTimeLog(mess);
	}
	private void sayAboutCheckList(){
		String mess = "";
		mess = "not found in etb " + caseIdsClean.size() + " cases ";
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
		while(!loadDate.after(currentDate)){
			//load by date and diagnosis, (state only in the full case)
			String loadMess = getLoadMess(loadDate);
			//setStateMessage(loadMess);
			//addLog(loadMess);
			for(String diag : diagnosis){
				if (notCanceled()){
					ArrayOfHumanCaseListInfo caseList = loader.getTBCasesList(loadDate, diag);
					if (caseList != null){
						Iterator<HumanCaseListInfo> it = caseList.getHumanCaseListInfo().iterator();
						int i = 0;
						while(it.hasNext()){
							HumanCaseListInfo info = it.next();
							Date d=ConvertToDate(info.getEnteredDate());
							CaseShortInfo csi=new CaseShortInfo(info.getCaseID(),d);
							caseIds.add(csi);
							i++;
						}
						addLog(DateFormat.getDateInstance().format(loadDate.getTime())+" "+diag + " found " + i+" case(s)");

					}else{
						addLog(DateFormat.getDateInstance().format(loadDate.getTime())+" "+diag + " found 0 case");
						String errorMess = loader.getErrorMessage();
						if (errorMess.length() > 0){
							noError = false;
							addLog("ERROR " + errorMess);
						}
					}
					//loadDate.add(Calendar.DATE, 1); //next date
				}else
					return noError;
			}
			loadDate.add(Calendar.DATE, 1); //next date
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
	public String loadCaseById(CaseShortInfo caseI) {
		String refusedList="";
		if (notCanceled()){
			HumanCaseInfo info = getLoader().getFullCase(caseI.caseId);
			if (info != null){
				if (suitable(info,config.getCaseStates(),"")) {
					addCase(info,caseI.caseDate);
				} else {
					refusedList=caseI.caseId;
				}
			}else{
				addLog("ERROR " + getLoader().getErrorMessage());
			}
		}
		return refusedList;
	}
	
	/* 
	 * is case suitable for export to etb
	 */
	
	private static boolean suitable(HumanCaseInfo cI,String patStateName, String caseStateName) {
		   //reject 10035002
		
			if (cI.getPatientState()!=null){
				if (cI.getPatientState().getId().toString().equalsIgnoreCase(patStateName)) {
				
					return false;
				}
			}
			/*
			//Refused
			if (cI.getCaseClassification()!=null){
				if (cI.getCaseClassification().getName().equalsIgnoreCase(caseStateName)) {
					return false;
				}
			}
			*/
			return true;
		}
	/**
	 * Convert case info to CaseInfo DTO and add this DTO to the result list
	 * Convert only info in allowed by config states
	 * @param info case info from the web service
	 */
	private void addCase(HumanCaseInfo EIDSSData, Date d) {
		boolean noError=true;
		CaseInfo onecase=new CaseInfo();
		String firstName = EIDSSData.getFirstName();
		if (firstName==null){
			firstName="XXXXXXXX";
			noError=false;
		}
		if (firstName.equalsIgnoreCase("")){
			firstName="XXXXXXXX";
			noError=false;
		}
		String lastName = EIDSSData.getLastName();
		if (lastName.startsWith("*")){
			lastName=lastName.substring(1);
		}
		String fatherName="";
		if (EIDSSData.getMiddleName()!=null) fatherName = EIDSSData.getMiddleName();
		onecase.setLastName(lastName);
		onecase.setFirstName(firstName);
		onecase.setMiddleName(fatherName);
		if (EIDSSData.getPatientGender()==null){
			onecase.setPatientGender("1");
		} else {
		onecase.setPatientGender(EIDSSData.getPatientGender().getId().toString());
		}
		if (EIDSSData.getDateOfBirth()!=null){
		onecase.setDateOfBirth(ConvertToDate(EIDSSData.getDateOfBirth()));
		}
		if (EIDSSData.getPatientAgeType()==null){
			onecase.setAge(0);
			noError=false;
		} else {
			onecase.setAge(CalcAge(EIDSSData.getPatientAge(),EIDSSData.getPatientAgeType().getId().toString()));
			
		}
		if (EIDSSData.getPatientAge()>100){
			onecase.setAge(EIDSSData.getPatientAge());
			noError=false;
		}
		String notification="";
		if (EIDSSData.getNotificationSentBy()!=null) {
			notification=EIDSSData.getNotificationSentBy().getName();
		}
		AddressInfo addr=EIDSSData.getCurrentResidence();
		onecase.setFinalDiagnosisDate(ConvertToDate(EIDSSData.getTentativeDiagnosisDate()));
		String country="";
		if (addr.getCountry()!=null)country=addr.getCountry().getName()+", ";
		String region="";
		if (addr.getRegion()!=null)region=addr.getRegion().getName()+", ";
		String settlement="";
		if (addr.getSettlement()!=null)settlement=addr.getSettlement().getName()+", ";
		String rayon="";
		if (addr.getRayon()!=null)rayon=addr.getRayon().getName()+", ";
		String addInfo=country+region+settlement+rayon;
		if 	(addr.getStreet()!=null)					addInfo=addInfo+addr.getStreet();
		onecase.setAdditionalComment(notification+" / "+addInfo);	
		onecase.setCaseID(EIDSSData.getCaseID().toString());	
		onecase.setEnteringDate(d);
		if (noError){
			infoForExport.add(onecase);
		}else{
			Integer age=onecase.getAge();
			String toLog=lastName+" "+firstName+" "+fatherName+", age "+age.toString()+", diag "+DateFormat.getDateInstance().format(onecase.getFinalDiagnosisDate())+", "+onecase.getCaseID();
			String entDate=DateFormat.getDateInstance().format(d);
			addLog(entDate+" - "+toLog);
		
		}
	}
	
	private Date ConvertToDate(XMLGregorianCalendar param){
		return param.toGregorianCalendar().getTime();
		/*
		Date tmpDate =new Date();
	int d=1;
	int m=0;
	int y=1;
	if ( param!=null) {
		param.getDay();
		m=param.getMonth();
		y=param.getYear();
		tmpDate.setDate(d);
		tmpDate.setMonth(m-1);
		tmpDate.setYear(y-1900);
	}
	return tmpDate;
*/
}
	private int CalcAge(int age, String ageType){
		String Days = "10042001";
		String Month = "10042002";
		String Years = "10042003";
		if (ageType.equalsIgnoreCase(Years)){
			return age;

		} else {
			return 0;
		}
	
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
	public void addTimeLog(String s) {
		Calendar cal = GregorianCalendar.getInstance();
		Date dt = cal.getTime();
		String dateStamp = LocaleDateConverter.getDisplayDate(dt, true);
		log.append(dateStamp + " " + s);
		log.append('\n');
	}
	public void addLog(String s) {
		
		log.append(s);
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