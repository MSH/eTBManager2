package org.msh.tb.az.eidss;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.application.tasks.TaskStatus;
import org.msh.tb.az.admin.UserAZControl;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.date.DateUtils;

import ua.com.theta.bv.client.Loader;

import com.bv.eidss.AddressInfo;
import com.bv.eidss.ArrayOfHumanCaseListInfo;
import com.bv.eidss.HumanCaseInfo;
import com.bv.eidss.HumanCaseListInfo;


@Name("eidssTaskImport")
public class EidssTaskImport extends AsyncTaskImpl {
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	EidssIntHome eidssIntHome;
	@In TaskManager taskManager;
	
	List<CaseInfo> infoForExport=new ArrayList<CaseInfo>();
	CaseImporting ci;
	private EidssIntConfig config;
	public static String stateMessage;
	private StringBuffer log = new StringBuffer();
	private String colCasesByDates = "";
	private UserTransaction transaction;
	private Loader loader;
	private static long beginMills;
	//case info in EIDSS system
	private class CaseShortInfo {
		public long caseDBId;	// primary DB key
		public String caseId;   //case id 
		public Date caseDate;   //case entered date 
		public CaseShortInfo(String id,long dbId, Date d){
			this.caseDBId = dbId;
			this.caseId=id;
			this.caseDate=d;
		}
	}
	
	private class ExportDetails{
		public String log;
		public char key;
		public ExportDetails(String s, char c){
			this.log = s; //string for log
			this.key = c; //symbol of type - '!','+','^','-' or 'e'(error)
		}
	}
	
	private List<CaseShortInfo> caseIds = new ArrayList<CaseShortInfo>();
	private List<CaseShortInfo> caseIdsClean = new ArrayList<CaseShortInfo>();
	private String refList;
	private List<CaseShortInfo> casesExistInEtb = new ArrayList<CaseShortInfo>();
	private Map<Date,List<ExportDetails>> exportDetailsList = new TreeMap<Date,List<ExportDetails>>();
	private boolean success = false;

	
	@Override
	protected void starting() {
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); //to avoid JBoss bug
		initContext();
		setStateMessage("eidss.import.starting");
		EidssIntHome eih = (EidssIntHome)Component.getInstance("eidssIntHome"); 
		if (eih!=null)
			if (eih.getUserLogin()!=null)
				if (eih.getUserLogin().getLoginDate()==null)
					addLog("Executing automatic EIDSS integration...");
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
		printExportDetails();
		addTimeLog("Finished");
		// save result to log file
		if (!success)
			if (!sendSystemEmail())
				addLog("Sending to email was failed");
		
		beginTransaction();
		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "EIDSS Integration", null, null, null);
		commitTransaction();
	}

	private boolean sendSystemEmail() {
		try {
			if (config.emails!=null && !config.emails.isEmpty()){
				List<String> emails = Arrays.asList(config.emails.split(","));
				if (!emails.isEmpty()){
					Contexts.getEventContext().set("emails", emails);
					String info = log.toString().replaceAll("\n", "<br/>");
					Contexts.getEventContext().set("info", info);
					Renderer.instance().render("/custom/az/mail/importexception.xhtml");
					return true;
				}
			}
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}



	private void printExportDetails() {
		for (Date d: exportDetailsList.keySet()) {
			List<ExportDetails> edlist = exportDetailsList.get(d); 
			Collections.sort(edlist, new Comparator<ExportDetails>() {

				@Override
				public int compare(ExportDetails o1, ExportDetails o2) {
					Integer prior1 = 0;
					Integer prior2 = 0;
					switch (o1.key) {
					case '!':
						prior1 = 1;
						break;
					case '+':
						prior1 = 2;
						break;
					case '^':
						prior1 = 3;
						break;
					case '-':
						prior1 = 4;
						break;
					case 'e':
						prior1 = 5;
						break;
					}
					switch (o2.key) {
					case '!':
						prior2 = 1;
						break;
					case '+':
						prior2 = 2;
						break;
					case '^':
						prior2 = 3;
						break;
					case '-':
						prior2 = 4;
						break;
					case 'e':
						prior2 = 5;
						break;
					}
					return prior1.compareTo(prior2);
				}
			});
			for (ExportDetails ed:edlist){
				addLog(ed.log);
			}
		}
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
					addLog(".");
					addLog("INFO: login to EIDSS successfull");
					addLog("URL: "+config.getUrl());
					addLog("Diagnosis: "+config.getDiagnosis());
					addLog("States to reject: "+config.getCaseStates());
					//Calendar c1=Calendar.getInstance();
					addLog("EIDSS entering date(s) to import: from "+ dateFormat.format(config.getFrom())+
							" to "+dateFormat.format(config.getToDate()));
					boolean success =loadCasesList();
					sayAboutLoad(success);
					if((caseIds.size()>0) && notCanceled()){
						Iterator<CaseShortInfo> it = caseIds.iterator();
						while (it.hasNext()){
							CaseShortInfo caseI = it.next();
							if (!ci.CheckIfExistInEtb(caseI.caseId)){
								caseIdsClean.add(caseI);
							}
							else{
								casesExistInEtb.add(caseI);
								addExportDetails(dateFormat.format(caseI.caseDate)+" ! "+caseI.caseId, '!', caseI.caseDate);
							}
						}
					}
					if((caseIdsClean.size()>0) && notCanceled()){
						Iterator<CaseShortInfo> it = caseIdsClean.iterator();
						 refList="";
						while (it.hasNext()){
							CaseShortInfo caseI = it.next();
							//setStateMessage("load case " + caseIdsClean.indexOf(caseId) + " total to load " + caseIdsClean.size());
							refList=refList+" "+loadCaseById(caseI);
						}	
					}
					
					addLog(colCasesByDates);
					addLog(casesExistInEtb.size() + " case(s) found in eTBm (!)");
					if ((infoForExport.size()> 0) && notCanceled()){
						exportToDB();
					}
					sayAboutExport();
					sayAboutRejected();
					sayAboutCheckList();
					//sayAboutLoad(success);
					//sayAboutCheckList();
				}
				else 
					sayAboutRejected();
				success  = true;
			} else{
				addLog("Error unable to login EIDSS " + getLoader().getErrorMessage());
			}
		}else{
			addLog("Error wrong parameters");
		}
	}
	
	private void sayAboutRejected(){
		Integer reject=caseIdsClean.size()-infoForExport.size();
		if (reject>0) {
			String slog = reject.toString() +" case(s) rejected (-)";
			if (!refList.trim().equalsIgnoreCase("")) {
				slog=slog+": "+refList;
			}
			addLog(slog);
		}
	}
	
	private void sayAboutExport(){
		int iw = 0;
		int ia = 0;
		for (Entry<Date, List<ExportDetails>> e : exportDetailsList.entrySet()) {
			for (ExportDetails ed:e.getValue()){
				if (ed.key == '+')
					iw++;
				else if (ed.key == '^')
					ia++;
			}
		}
		addLog(iw+ " case(s) written (+)");
		addLog(ia+ " case(s) updated (^)");
	}
	
	/**
	 * Export cases to eTBManager
	 */
	private void exportToDB() {
		Integer iw=0;
		Integer ia=0;
		try{
			Iterator<CaseInfo> it=infoForExport.iterator();
			while (it.hasNext()){
				CaseInfo c = it.next();
				String result=ci.importRecords(c);
				/*if (config.auto){
					result=ci.checkImportRecords(c);
				} else {
					 result=ci.importRecords(c);
				}*/
	
				if (!result.equalsIgnoreCase("error")){
					try{
						String ad=c.getAdditionalComment();
						int pos=ad.indexOf("/");
						if (pos <14){
							ad=ad.substring(0, pos);
						} else {
							ad=ad.substring(0, 14);
						}
						Integer age=c.getAge();
						String toLog=c.getLastName()+" "+c.getFirstName()+" "+c.getMiddleName()+
						", age "+age.toString()+", diag "+dateFormat.format(c.getFinalDiagnosisDate())+", "+ad+" "+c.getCaseID();
						String entDate=dateFormat.format(c.getEnteringDate());
						
						
						if (result.equalsIgnoreCase(CaseImporting.WRITED)){
							addExportDetails(entDate+" + "+toLog, '+',c.getEnteringDate());
							iw++;
						}
						if (result.equalsIgnoreCase(CaseImporting.UPDATED)){
							addExportDetails(entDate+" ^ "+toLog, '^',c.getEnteringDate());
							ia++;
						}
					} catch (Exception e){
						addExportDetails("Error writing  "+c.getLastName(),'e',c.getEnteringDate());
					}
				}else
					addExportDetails("Error writing  "+c.getLastName(),'e',c.getEnteringDate());
			}
			//addLog(iw.toString()+ " cases written (+)");
			//addLog(ia.toString()+ " cases updated (^)");
		}
		catch (Exception e){
			addLog("ERROR in EidssTaskImport.exportToDB(): " + e.getMessage());
			//addLog(iw.toString()+ " cases written (+)");
			//addLog(ia.toString()+ " cases updated (^)");
		}
		
	}

	private void addExportDetails(String s, char c, Date d){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
		Date dt = d;
		try {
			dt = sdf.parse(sdf.format(d));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (exportDetailsList.keySet().contains(dt)){
			exportDetailsList.get(dt).add(new ExportDetails(s, c));
		}
		else{
			exportDetailsList.put(dt, new ArrayList<ExportDetails>());
			exportDetailsList.get(dt).add(new ExportDetails(s, c));
		}
		//exportDetails += s+'\n';
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
			mess =caseIds.size() + " case(s) totally found in EIDSS";
			if (!success)
				mess = mess + " but some errors occured, see above";
		}else{
			if (!success)
				mess = "ERROR load case list";
			else
				mess = "total found 0 cases, check dates and diagnosis";
		}
		addLog("\n"+mess);
	}
	private void sayAboutCheckList(){
		String mess = "";
		mess = caseIdsClean.size() + " case(s) totally not found in eTBm";
		addLog(mess+"\n");
	}

	@Override
	public void cancel(){
		addLog("Canceled");
		// save result to log file
		boolean notHaveUser=false;
		try {
			if (getTransaction().isNoTransaction())
				notHaveUser = true;
		} catch (SystemException e) {
			e.printStackTrace();
		}
		if (notHaveUser)
			beginTransaction();
		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "EIDSS Integration", null, null, null);
		if (notHaveUser)
			commitTransaction();
		super.cancel();
	}
	/**
	 * Determine cases ID by date interval from begin date to today and for each diagnosis
	 * @return
	 */
	private boolean loadCasesList() {
		boolean noError = true;
		try{
			setStateMessage("eidss.import.load.data");
			// determine dates, diagnosis and states
			Calendar loadDate = GregorianCalendar.getInstance();
			Calendar currentDate = GregorianCalendar.getInstance();
			loadDate.setTime(config.getFrom());
			loadDate.set(Calendar.HOUR_OF_DAY, 12);
			loadDate.set(Calendar.MINUTE, 30);
			loadDate.set(Calendar.SECOND,0);
			currentDate.setTime(config.getToDate());
			currentDate.set(Calendar.HOUR_OF_DAY, 12);
			currentDate.set(Calendar.MINUTE, 31);
			currentDate.set(Calendar.SECOND,0);
			String[] diagnosis = config.getDiagnosis().split(",");
			while(!loadDate.after(currentDate)){
				//load by date and diagnosis, (state only in the full case)
				String loadMess = getLoadMess(loadDate);
				//setStateMessage(loadMess);
				//addLog(loadMess);
				int i = 0;
				if (notCanceled()){
				for(String diag : diagnosis){
						ArrayOfHumanCaseListInfo caseList = loader.getTBCasesList(loadDate, diag);
						if (caseList != null){
							Iterator<HumanCaseListInfo> it = caseList.getHumanCaseListInfo().iterator();
							while(it.hasNext()){
								HumanCaseListInfo info = it.next();
								Date d=ConvertToDate(info.getEnteredDate());
								CaseShortInfo csi=new CaseShortInfo(info.getCaseID(),info.getId(),d);
								caseIds.add(csi);
								i++;
							}
	
						}else{
							i=0;
							//addExportDetails(DateFormat.getDateInstance().format(loadDate.getTime())+" "+diag + " found 0 case");
							String errorMess = loader.getErrorMessage();
							if (errorMess.length() > 0){
								noError = false;
								colCasesByDates +="ERROR " + errorMess+"\n";
							}
						}
						//loadDate.add(Calendar.DATE, 1); //next date
				}
					}else
						return noError;
				colCasesByDates += dateFormat.format(loadDate.getTime())+" found " + i+" case(s)"+"\n";
				loadDate.add(Calendar.DATE, 1); //next date
			}			
			return noError;
		}
		catch(Exception e){
			noError = false;
			addLog("ERROR in EidssTaskImport.loadCaseList(): " + e.getMessage());
			return noError;
		}
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
		Query q = getEntityManager().createQuery("from UserWorkspace ws where ws.user.id = :uid and ws.workspace.id = :id")
									.setParameter("uid", user.getId())
									.setParameter("id", getWorkspace().getId());
		UserWorkspace ws = (UserWorkspace)q.getResultList().get(0);
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
				beginTransaction();
				Query q = getEntityManager().createQuery("from User u where u.id = "+userId);
				List<Object> lu = q.getResultList();
				commitTransaction();
				User user = (User)lu.get(0);
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
		String dbId="";
		if (notCanceled()){
			try{
				dbId = String.valueOf(caseI.caseDBId);
				HumanCaseInfo info = getLoader().getFullCase(dbId);
				if (info != null){
					if (suitable(info,config.getCaseStates(),"")) {
						addCase(info,caseI.caseDate);
					} else {
						refusedList=caseI.caseId;
					}
				}else{
					addLog("ERROR " + getLoader().getErrorMessage() + " info by case is null in "+dbId);
				}
			}
			catch (Exception e){
				addLog("ERROR in EidssTaskImport.loadCaseById(): " + e.getMessage()+" in "+dbId);
			}
		}
		return refusedList;
	}
	
	/* 
	 * is case suitable for export to etb
	 */
	
	private static boolean suitable(HumanCaseInfo cI,String patStateName, String caseStateName) {
		   //reject 10035002
		
			if (cI.getPatientState()!=null)
				if (cI.getPatientState().getId()!=null){
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
	
	private String replaceIfEmptyName(String n){
		if (n==null){
			return "XXXXX";
		}
		if (n.equalsIgnoreCase("")){
			return "XXXXX";
		}
		return n;
		
	}
	
	/**
	 * Convert case info to CaseInfo DTO and add this DTO to the result list
	 * Convert only info in allowed by config states
	 * @param info case info from the web service
	 */
	private void addCase(HumanCaseInfo EIDSSData, Date d) {
		try{
		boolean noError=true;
		String diag="";
		CaseInfo onecase=new CaseInfo();
		String firstName = EIDSSData.getFirstName();
		
		firstName = replaceIfEmptyName(firstName);
		noError = (firstName.equals("XXXXX") ? false : noError);
		/*if (firstName==null){
			firstName="XXXXX";
			noError=false;
		}
		if (firstName.equalsIgnoreCase("")){
			firstName="XXXXX";
			noError=false;
		}*/
		String lastName = EIDSSData.getLastName();
		if (lastName.startsWith("*")){
			lastName=lastName.substring(1);
		}
		
		lastName = replaceIfEmptyName(lastName);
		noError = (lastName.equals("XXXXX") ? false : noError);
		
		String fatherName="";
		if (EIDSSData.getMiddleName()!=null) 
			fatherName = EIDSSData.getMiddleName();
		
		fatherName = replaceIfEmptyName(fatherName);
		//noError = (fatherName.equals("XXXXX") ? false : noError);
		
		onecase.setLastName(lastName);
		onecase.setFirstName(firstName);
		onecase.setMiddleName(fatherName);
		if (EIDSSData.getPatientGender()==null){
			onecase.setPatientGender("1");
		} else {
			if (EIDSSData.getPatientGender().getId()==null)
				onecase.setPatientGender("1");
			else
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
			if (notification.contains("?") || notification.contains("\u0259"))
				System.out.println(notification);
		}
		AddressInfo addr=EIDSSData.getCurrentResidence();
		if (EIDSSData.getTentativeDiagnosisDate()!=null) {
		onecase.setFinalDiagnosisDate(ConvertToDate(EIDSSData.getTentativeDiagnosisDate()));
		}else {
			noError=false;
			diag="XXXXXXX";
		}
		String country="";
		if (addr.getCountry()!=null)country=addr.getCountry().getName()+", ";
		String region="";
		if (addr.getRegion()!=null)region=addr.getRegion().getName()+", ";
		String settlement="";
		if (addr.getSettlement()!=null)settlement=addr.getSettlement().getName()+", ";
		String rayon="";
		if (addr.getRayon()!=null)rayon=addr.getRayon().getName()+", ";
		String addInfo=country+region+settlement+rayon;
		if 	(addr.getStreet()!=null)	addInfo=addInfo+addr.getStreet();
		String comment = notification+" / "+
						addInfo+ " / "+
						DateUtils.formatDate(d,"dd-MM-yyyy")+" / "+
						lastName+" "+firstName+" "+fatherName+" / "+
						onecase.getAge()+" / "+
						(onecase.getDateOfBirth()!=null ? DateUtils.formatDate(onecase.getDateOfBirth(),"dd-MM-yyyy") : "")+" / "+
						(EIDSSData.getNotificationDate()!=null ? DateUtils.formatDate(ConvertToDate(EIDSSData.getNotificationDate()),"dd-MM-yyyy"):"");
		onecase.setAdditionalComment(comment);	
		onecase.setCaseID(EIDSSData.getCaseID().toString());	
		onecase.setEnteringDate(d);
		//addLog(lastName+" "+firstName+" "+fatherName);
		if (noError){
			infoForExport.add(onecase);
		}else{
			Integer age=onecase.getAge();
			String ageStr;
			if (diag.equalsIgnoreCase("")){
				diag=dateFormat.format(onecase.getFinalDiagnosisDate());
			}
			if (age==0){
				ageStr="XX";
			}else {
				ageStr=age.toString();
			}
			String toLog=lastName+" "+firstName+" "+fatherName+", age "+ageStr+", diag "+diag+", "+(notification.length()>14 ? notification.substring(0, 14) : notification)+" "+onecase.getCaseID();
			String entDate=dateFormat.format(d);
			addExportDetails(entDate+" - "+toLog, '-', d);
			//rejectDetails += entDate+" - "+toLog+"\n";
		
		}
		}
		catch (Exception e){
			addLog("ERROR in EidssTaskImport.addCase(): " + e.getMessage());
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
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dateStamp = df.format(dt);
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
	@Override
	public Workspace getWorkspace() {
		EidssIntHome eih = (EidssIntHome)Component.getInstance("eidssIntHome"); 
		return eih.getWorkspace();
	}
}