package org.msh.tb.az;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.ExamCulture_Az;
import org.msh.tb.az.entities.ExamDSTAZ;
import org.msh.tb.az.entities.ExamMicroscopyAZ;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.az.entities.enums.CaseFindingStrategy;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CaseComorbidity;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineRegimen;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.misc.SequenceGenerator;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.date.Period;
import org.jboss.seam.international.Messages;
import org.jboss.seam.transaction.UserTransaction;

/**
 * @author »нка
 *
 */
@Name("importIMM")
public class ImportIMM {
	private EntityManager entityManager;
	private String file;
	private Map<String, String> messages;
	int adminunitID=970579;
	int tbunitIMM=941203;
	List <Tbunit> units;
	private UserTransaction transaction;
	private static final Integer workspaceID = 8;
	int lastCols=0;
	private FieldValue method;
	private FieldValue method2;
	private Laboratory laboratory;
	String lastname;
	String  name;
	String middlename;
	Date birstday;
	Regimen cat1;
	Regimen cat2;
		private StringBuffer log = new StringBuffer();
	private Period treatmPd;
	private Source source;
	private TbCaseAZ tc;
	private int weight;
	private int lineNumber;
	
	public boolean execute() {

		try {
			String s=getFile();
			if (s.equalsIgnoreCase("")) s="c:/temp/test.xls";
			Workbook workbook = Workbook.getWorkbook(new File(s));
			addLog("Import cases from file "+s);
			if (workbook==null)	addLog("File not found "+s);
			else{
				Sheet sheet = workbook.getSheet(0);
				if (entityManager==null){
					entityManager = (EntityManager)Component.getInstance("entityManager");
				}
				units=getTbunit();
				lastCols=sheet.getColumns()-1;
				method = entityManager.find(FieldValue.class, 939311);   //Levestain-Jensen
				method2 = entityManager.find(FieldValue.class, 939316);//GeneXpert 
				laboratory=entityManager.find(Laboratory.class, 942680);  //IMM lab
				source=entityManager.find(Source.class, 940558);
				cat1=getRegimen(1);
				//TODO  add medicines for cat1
				cat2=getRegimen(2);
				for (int i = 2; i < sheet.getRows(); i++) {
					Cell[] r = sheet.getRow(i);
					importRecord(r);
					getEntityManager().flush();
					lineNumber++;
				}
			}
			//clearEntityManager();
		} catch (BiffException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {

			e2.printStackTrace();
		}

		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "import IMM", null, null, null);
		return true;
	}

	public void clearEntityManager() {
		EntityManager em = getEntityManager();
		em.flush();
		em.clear();
	}
	
	private void importRecord(Cell[] r ) {
		String code=r[1].getContents();
		if	(isCaseExist(code)){
			addLog(Integer.toString(lineNumber)+": "+code + "- case already exists");
			return;
		}
		MedicalExamination me=new MedicalExamination();
		 tc = new TbCaseAZ();
		
		Patient p;
		try {
			//get person data from excel
			if (isWrongCase(r, code)) return;
			birstday = ((DateCell)r[2]).getDate();//c
			lastname=r[4].getContents().substring(0, 1)+r[4].getContents().substring(1).toLowerCase();//e
			name=r[5].getContents().substring(0, 1)+r[5].getContents().substring(1).toLowerCase(); //f
			middlename=r[6].getContents().substring(0, 1)+r[6].getContents().substring(1).toLowerCase(); //g
			p=getPatient();
			if (p==null) p=createNewPatient(r);
			tc.setPatient(p);
			generateCaseNumber(tc);
			AdministrativeUnit a=entityManager.find(AdministrativeUnit.class, adminunitID);
			Address addr=new Address();
			addr.setAdminUnit(a);
			tc.setNotifAddress(addr);
			tc.setCurrentAddress(addr);
			tc.setValidationState(ValidationState.VALIDATED);
			tc.setRegistrationCode(r[1].getContents());
			tc.setClassification(CaseClassification.TB);
			tc.setDiagnosisType(DiagnosisType.CONFIRMED); 
		   DateCell dc = (DateCell)r[3];//d
			tc.setRegistrationDate(dc.getDate());
			tc.setDiagnosisDate(dc.getDate());
			tc.setAge(calculateAge(p.getBirthDate(), tc.getRegistrationDate()));
			String	s = r[9].getContents();
			if ("N".equals(s))
				tc.setPatientType(PatientType.NEW);
			if ("T".equals(s))
				tc.setPatientType(PatientType.AFTER_DEFAULT);
			if ("R".equals(s))
				tc.setPatientType(PatientType.RELAPSE);
			if ("F".equals(s))
				tc.setPatientType(PatientType.FAILURE_FT);
			if ("O".equals(s))
				tc.setPatientType(PatientType.OTHER);

			s=r[10].getContents();
			tc.setNotificationUnit(getTbunitbyName(s));
			tc.setOwnerUnit(tc.getNotificationUnit());
			//set owner unit
			s = r[11].getContents(); 
			if (s.length() == 2){  
				tc.setReferToOtherTBUnit(false);
				tc.setReferToTBUnit(getTbunitbyName("3"));
			}
			else {
				tc.setReferToOtherTBUnit(true);
				s=r[12].getContents();
				tc.setReferToTBUnit(getTbunitbyName(s));
			}
			List<TreatmentHealthUnit> units=tc.getHealthUnits();
			TreatmentHealthUnit currunit=new TreatmentHealthUnit();
			units.add(currunit);
			currunit.setTbcase(tc);
			currunit.setTbunit(tc.getOwnerUnit());
			//tc.setHealthUnits(tc.getNotificationUnit());
			//  infectionSite  N
			s = r[13].getContents();
			if (s.equalsIgnoreCase(getMessages().get("import.infectionSite1")))
				tc.setInfectionSite(InfectionSite.PULMONARY);
			if (s.equalsIgnoreCase(getMessages().get("import.infectionSite2")))
				tc.setInfectionSite(InfectionSite.EXTRAPULMONARY);
			if (s.equalsIgnoreCase(getMessages().get("import.infectionSite3")))
				tc.setInfectionSite(InfectionSite.BOTH);	

			//PULMONARY_TYPES o
			s = r[14].getContents();
			if (!s.equalsIgnoreCase("")) tc.setPulmonaryType(getPulmonaryType(new Integer(s)));
			s = r[15].getContents();
			if (!s.equalsIgnoreCase("")) tc.setExtrapulmonaryType(getExtraPulmonaryType(new Integer(s)));
			s=r[16].getContents();//q-weight
			//add medical examination if weight not empty
			if (!s.equalsIgnoreCase("")) {
				List<MedicalExamination> examinations=tc.getExaminations();
				me.setTbcase(tc);
				examinations.add(me);
				me.setWeight(new Double(s));
				weight=new Integer(s);
				s=r[17].getContents();//r-height
				if (!s.equalsIgnoreCase("")) me.setHeight(new Float(s));
				me.setDate(tc.getDiagnosisDate());
				me.setUsingPrescMedicines(YesNoType.YES);
			}
			else weight=60;
			s=r[18].getContents();//s-outcome
			if (!s.equalsIgnoreCase("")){
				tc.setState(getState(s));
			}else{
				tc.setState(CaseState.ONTREATMENT);
			}
			if (r[19].getType()==CellType.DATE){
				dc = (DateCell)r[19];
				tc.setOutcomeDate(dc.getDate());
			} 
			Calendar datePhase=Calendar.getInstance();
		//TODO isCat2
			boolean isCat2=true;
			isCat2 =(code.indexOf("11721")==-1);
			if (isCat2){
				datePhase.setTime(tc.getRegistrationDate());
				datePhase.add(Calendar.MONTH, 8);
				datePhase.add(Calendar.DAY_OF_YEAR, -1);
				treatmPd=new Period (tc.getRegistrationDate(),datePhase.getTime());
				if (tc.getOutcomeDate()!=null){
					if (tc.getOutcomeDate().before(datePhase.getTime()) )
						treatmPd=new Period (tc.getRegistrationDate(),tc.getOutcomeDate());
				}
				tc.setRegimen(cat2);
				tc.setRegimenIni(cat2);
				tc.setTreatmentPeriod(treatmPd);
				//TODO Medicine
				//List<MedicineRegimen> getMedicines()
				currunit.setPeriod(treatmPd);
				datePhase.setTime(tc.getRegistrationDate());
				datePhase.add(Calendar.MONTH, 3);
				tc.setIniContinuousPhase(datePhase.getTime());
				List<PrescribedMedicine> mdlist =createPrescribedMedicineList2(tc);
				tc.setPrescribedMedicines(mdlist);
			}else{
				datePhase.setTime(tc.getRegistrationDate());
				datePhase.add(Calendar.MONTH, 6);
				datePhase.add(Calendar.DAY_OF_YEAR, -1);
				treatmPd=new Period (tc.getRegistrationDate(),datePhase.getTime());
				if (tc.getOutcomeDate()!=null){
					if (tc.getOutcomeDate().before(datePhase.getTime()) )
						treatmPd=new Period (tc.getRegistrationDate(),tc.getOutcomeDate());
				}
				tc.setRegimen(cat1);
				tc.setRegimenIni(cat1);
				tc.setTreatmentPeriod(treatmPd);
				//TODO Medicine
				//List<MedicineRegimen> getMedicines()
				currunit.setPeriod(treatmPd);
				datePhase.setTime(tc.getRegistrationDate());
				datePhase.add(Calendar.MONTH, 2);
				tc.setIniContinuousPhase(datePhase.getTime());
				List<PrescribedMedicine> mdlist =createPrescribedMedicineList(tc);
				tc.setPrescribedMedicines(mdlist);
			}
				//U - localisation
			s=r[21].getContents();//V - case finding
			if (s.equalsIgnoreCase("")) tc.setCaseFindingStrategy(CaseFindingStrategy.NA);
			if (s.equalsIgnoreCase("ACTIVE")) tc.setCaseFindingStrategy(CaseFindingStrategy.ACTIVE);
			if (s.equalsIgnoreCase("PASSIV")) tc.setCaseFindingStrategy(CaseFindingStrategy.PASSIVE);
			s=r[22].getContents();//W - prev. treatment
			s=r[23].getContents();//x - prison
			if (!s.equalsIgnoreCase(""))  {
				tc.setNumberOfImprisonments(new Integer(s));
			}
			if	(r[24].getType()==CellType.DATE) {
				dc = (DateCell)r[24];
				tc.setInprisonIniDate(dc.getDate());
				
			}
			if (r.length==lastCols){
				s=r[lastCols].getContents();
				if (!s.equalsIgnoreCase("")) setComorbidity(tc,s);
			}
			//exams
			//hiv
			int hivCol=26;
			s=r[hivCol].getContents(); 
			if (!s.equalsIgnoreCase("")){
				List<ExamHIV> listhiv = tc.getResHIV();
				ExamHIV hiv=new ExamHIV();
				listhiv.add(hiv);
				if (s.equalsIgnoreCase("NEG"))hiv.setResult(HIVResult.NEGATIVE);
				if (s.equalsIgnoreCase("POS"))hiv.setResult(HIVResult.POSITIVE);
				if (r[hivCol+1].getType()==CellType.DATE) hiv.setDate(((DateCell)r[hivCol+1]).getDate());
				s=r[hivCol+2].getContents(); 
				if (s.equalsIgnoreCase("Y"))hiv.setARTstarted(s.equalsIgnoreCase("Y"));
				if (r[hivCol+3].getType()==CellType.DATE) hiv.setStartedARTdate(((DateCell)r[hivCol+3]).getDate());
				hiv.setTbcase(tc);
			}
			//	Microscopy
			int microCol=30;
			if (r[microCol+2].getType()==CellType.DATE){
				dc = (DateCell)r[microCol+2];	
				me.setDate(dc.getDate());	
			}
			List<ExamMicroscopy> microExams=tc.getExamsMicroscopy();
			for (int i=0;i<5; i++){
				ExamMicroscopy e=setMicroExam(microCol+i*3,r,tc);
				if (e!=null)microExams.add(e);
			}
			tc.setExamsMicroscopy(microExams); 
			//Culture
			int cultureCol=45;
			List<ExamCulture> examsC=tc.getExamsCulture();
			for (int i=0;i<6; i++){
				ExamCulture e=setCultureExam(cultureCol+i*4,r,tc);
				if (e!=null)examsC.add(e);
			}
			tc.setExamsCulture(examsC); 
			//DST   cols=14
			int sCol=69;
			List<ExamDST> exams=tc.getExamsDST();
			for (int i=0;i<5; i++){
				ExamDST e=setExamDST(sCol+i*14,r,tc,i);
				if (e!=null)exams.add(e);
			}
			ExamDST e=setExamDST(sCol+5*14,r,tc,6);
			if (e!=null)exams.add(e);
			tc.setExamsDST(exams);
			//beginTransaction();
			entityManager.persist(p);
			entityManager.persist(tc);
			entityManager.flush();
			addLog(Integer.toString(lineNumber)+": "+ code+ " added");
			System.out.println(code);
			//commitTransaction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isWrongCase(Cell[] r, String code) {
		if (r[2].getType()!=CellType.DATE){
			addLog(Integer.toString(lineNumber)+": "+code + "- Error. Birstday date is empty");
			return true;
		}

		if (r[4].getContents().equalsIgnoreCase("") | r[5].getContents().equalsIgnoreCase("")| r[6].getContents().equalsIgnoreCase("")){
			addLog(Integer.toString(lineNumber)+": "+code + "- Error. Name is empty");
			return true;
		}
		return false;
	}

	private Patient createNewPatient(Cell[] r) {
	
		Patient p = new Patient();	
		p.setSecurityNumber(r[1].getContents());
		p.setBirthDate(birstday);
		p.setWorkspace(getWorkspace());
		p.setLastName(lastname);//e
		p.setName(name); //f
		p.setMiddleName(middlename); //g
		p.setMotherName(r[7].getContents());  //h
		String s = r[8].getContents();
		if ("M".equals(s))
			p.setGender(Gender.MALE);
		if ("F".equals(s))
			p.setGender(Gender.FEMALE);
		
		return p;
	}

	private Patient getPatient(){
		Patient existP=null;
		List<Patient> lst= entityManager.createQuery("from Patient p where lastName=:f and name=:i and middleName=:o and birthDate=:d")
		.setParameter("f", lastname)
		.setParameter("i", name)
		.setParameter("o", middlename)
		.setParameter("d",birstday)
		.getResultList();
		if (lst!=null){
			if (lst.size()>0)existP=lst.get(0);
		}
		return existP;
	}
	private boolean isCaseExist(String code){
		Long i= (Long) entityManager.createQuery("select count (*) from TbCase c where registrationCode=:cd").setParameter("cd", code).getSingleResult();
		return i>0;
		
	}
	private ExamDST setExamDST(int start, Cell[] r, TbCaseAZ tc,int round) {
		ExamDSTAZ ed=null;
		if (r.length>start+1){
		if (r[start+1].getType()==CellType.DATE) {
			ed=new ExamDSTAZ();
			String s=r[start].getContents();
			if (!s.equalsIgnoreCase(""))	ed.setSampleNumber(s);
			DateCell dc=(DateCell)r[start+1];
			ed.setDateCollected(dc.getDate());
			ed.setDatePlating(dc.getDate());
			if (r[start+2].getType()==CellType.DATE) ed.setDateRelease(((DateCell)r[start+2]).getDate());
			List<ExamDSTResult> le = ed.getResults();
			//S H R E Pto Z Ofx Cs PAS Cm Am
			if (round==6){
				le.add(createExamDSTResult("H",r[start+3].getContents(),ed));
				le.add(createExamDSTResult("R",r[start+4].getContents(),ed));
				ed.setMethod(method2);//GeneXpert=9
			} else {
				le.add(createExamDSTResult("S",r[start+3].getContents(),ed));
				le.add(createExamDSTResult("H",r[start+4].getContents(),ed));
				le.add(createExamDSTResult("R",r[start+5].getContents(),ed));
				le.add(createExamDSTResult("E",r[start+6].getContents(),ed));
				le.add(createExamDSTResult("Pto",r[start+7].getContents(),ed));
				le.add(createExamDSTResult("Z",r[start+8].getContents(),ed));
				le.add(createExamDSTResult("Ofx",r[start+9].getContents(),ed));
				le.add(createExamDSTResult("Cs",r[start+10].getContents(),ed));
				le.add(createExamDSTResult("PAS",r[start+11].getContents(),ed));
				le.add(createExamDSTResult("Cm",r[start+12].getContents(),ed));
				le.add(createExamDSTResult("Am",r[start+13].getContents(),ed));
				ed.setMethod(method);
			}
			ed.setResults(le);
			ed.setTbcase(tc);
			ed.setLaboratory(laboratory);
		}
	}
		return ed;
	}
	private ExamDSTResult createExamDSTResult(String substName, String s, ExamDST em){
		ExamDSTResult re=new ExamDSTResult();
		Substance sb=(Substance) entityManager.createQuery("from Substance s where abbrevName.name1=:n and s.workspace.id =8").setParameter("n", substName).getSingleResult();
		re.setSubstance(sb);
		re.setExam(em);
		re.setResult(getResult(s));
		return re;
	}
	private DstResult getResult(String s){
		DstResult res=null;
		if (s.equalsIgnoreCase("")) res=DstResult.NOTDONE;
		if (s.equalsIgnoreCase("1")) res=DstResult.RESISTANT;
		if (s.equalsIgnoreCase("0")) res=DstResult.SUSCEPTIBLE;
		return res;
	}
	
	
	private void setComorbidity(TbCaseAZ tc, String s) {
		CaseComorbidity cb=new CaseComorbidity();
		FieldValue fv=entityManager.find(FieldValue.class, 939235);
		cb.setComorbidity(fv);
			if (s.equalsIgnoreCase("HBCV"))	cb.setComment("Hepatitis BC");
		if (s.equalsIgnoreCase("HBV"))	cb.setComment("Hepatitis B");
		if (s.equalsIgnoreCase("HCV"))	cb.setComment("Hepatitis C");
		cb.setTbcase(tc);
		List<CaseComorbidity> cbs;
		cbs=tc.getComorbidities();
		cbs.add(cb);
		
tc.setComorbidities(cbs);
	}
	private CaseState getState(String s) {
		CaseState res=CaseState.DEFAULTED;
		if (s.equalsIgnoreCase("CURED")) res=CaseState.CURED;
		if (s.equalsIgnoreCase("DEATH")) res=CaseState.DIED;
		if (s.equalsIgnoreCase("DOTSFAILURE")| s.equalsIgnoreCase("DOTSPLUS"))	res=CaseState.FAILED;	
		if (s.equalsIgnoreCase("COMPLETEDTREATMENT")) res=CaseState.TREATMENT_COMPLETED;
		if (s.equalsIgnoreCase("TRANSFEROUT")) res=CaseState.TRANSFERRED_OUT;

		return res;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return file;
	}
	protected Map<String, String> getMessages() {
		if (messages == null)
			messages = Messages.instance();
		return messages;
	}
	
	
	private List<Tbunit> getTbunit(){
		List<Tbunit> lst=null;
		try {
			 lst = entityManager.createQuery(" from Tbunit a " 		+
					" where a.healthSystem.id = 932 and  a.adminUnit.id=970579") 
							
					.getResultList();
			

		} catch (Exception e) {
	
		}
		return lst;
	}
	 
	
	
	
	/**
	 * select tbunit by other system name
	 * @param name
	 * @return
	 */
	private Tbunit getTbunitbyName(String name){
		
		Tbunit unit=null;
		if (units.size() != 0) {
			Iterator <Tbunit> it= units.iterator();
			while  (it.hasNext()){
				Tbunit u =it.next();
				if (name.equalsIgnoreCase("3")) {
					if (u.getId()==tbunitIMM) unit=u;
				}
				String newname="CM-"+name;
				if (u.getName().getName1().contains(newname)) unit=u;
				if (name.equalsIgnoreCase("GOB")) if (u.getId()==942109) unit=u; 
				if (name.equalsIgnoreCase("SZ1")) if (u.getId()==942110) unit=u; 
				if (name.equalsIgnoreCase("SZ2")) if (u.getId()==942111) unit=u; 
				if (name.equalsIgnoreCase("SZ3")) if (u.getId()==942112) unit=u;
				if (name.equalsIgnoreCase("CPH")) if (u.getId()==942113) unit=u;

			}
		}
		return unit;

	}
	private FieldValue getPulmonaryType(int typeNumber){
		List<FieldValue> lst=null;
		FieldValue result=null;
		try {
			lst = entityManager.createQuery(" from FieldValue a " 		+
			" where field = 13") 

			.getResultList();


		} catch (Exception e) {

		}
		if (lst.size()!=0){
			Iterator <FieldValue> it= lst.iterator();
			while  (it.hasNext()){
				FieldValue f =it.next();
				switch (typeNumber) {
				case 1:
					if (f.getId().compareTo(new Integer(939292))==0) result=f;
					break;	
				case 2:
					if (f.getId().compareTo(new Integer(939293))==0) result=f;
					break;	
				case 3:
					if (f.getId().compareTo(new Integer(939294))==0) result=f;
					break;	
				case 4:
					if (f.getId().compareTo(new Integer(939295))==0) result=f;
					break;	
				case 5:
					if (f.getId().compareTo(new Integer(939296))==0) result=f;
					break;
				case 6:
					if (f.getId().compareTo(new Integer(939297))==0) result=f;
					break;
				case 7:
					if (f.getId().compareTo(new Integer(939298))==0) result=f;
					break;	
				case 8:
					if (f.getId().compareTo(new Integer(939291))==0) result=f;
					break;	
				case 9:
					if (f.getId().compareTo(new Integer(939289))==0) result=f;
					break;		
				}
			}
		}
			return result;
	}
	private FieldValue getExtraPulmonaryType(int typeNumber){
		List<FieldValue> lst=null;
		FieldValue result=null;
		try {
			lst = entityManager.createQuery(" from FieldValue a " 		+
			" where field = 14") 

			.getResultList();


		} catch (Exception e) {

		}
		if (lst.size()!=0){
			Iterator <FieldValue> it= lst.iterator();
			while  (it.hasNext()){
				FieldValue f =it.next();
				switch (typeNumber) {
				case 1:
					if (f.getId().compareTo(new Integer(939301))==0) result=f;
					break;	
				case 2:
					if (f.getId().compareTo(new Integer(939302))==0) result=f;
					break;	
				case 3:
					if (f.getId().compareTo(new Integer(939300))==0) result=f;
					break;	
				case 4:
					if (f.getId().compareTo(new Integer(939303))==0) result=f;
					break;	
				case 5:
					if (f.getId().compareTo(new Integer(939305))==0) result=f;
					break;
				case 6:
					if (f.getId().compareTo(new Integer(939306))==0) result=f;
					break;
				case 7:
					if (f.getId().compareTo(new Integer(939308))==0) result=f;
					break;	
				case 8:
					if (f.getId().compareTo(new Integer(939304))==0) result=f;
					break;	
				case 9:
					if (f.getId().compareTo(new Integer(939307))==0) result=f;
					break;	
				case 10:
					if (f.getId().compareTo(new Integer(939309))==0) result=f;
					break;
				case 11:
					if (f.getId().compareTo(new Integer(939290))==0) result=f;
					break;
				case 12:
					if (f.getId().compareTo(new Integer(939310))==0) result=f;
					break;
				}
			}
		}
		
		return result;
	}
	
	private Integer calculateAge(Date birthday, Date registrationDate)
	{
		Calendar dob = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
today.setTime(registrationDate);
		dob.setTime(birthday);
		// include day of birth
		dob.add(Calendar.DAY_OF_MONTH, -1);

		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
		if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR)) {
			age--;
		}
		return age;
	}
	
	private ExamMicroscopy setMicroExam(int start, Cell[] r, TbCase tbcase){
		ExamMicroscopyAZ emr=null;
		if (r.length>start+2){
		if (r[start+2].getType()==CellType.DATE) {
		emr=new ExamMicroscopyAZ();
			String s=r[start].getContents();
		if (!s.equalsIgnoreCase(""))	emr.setSampleNumber(s);
		s=r[start+1].getContents();
		MicroscopyResult result=MicroscopyResult.POSITIVE;
		if (s.equalsIgnoreCase("NEG"))	result=MicroscopyResult.NEGATIVE;
		if (s.equalsIgnoreCase("1+"))result=MicroscopyResult.PLUS;	
		if (s.equalsIgnoreCase("2+"))result=MicroscopyResult.PLUS2;
		if (s.equalsIgnoreCase("3+"))result=MicroscopyResult.PLUS3;
		if (s.equalsIgnoreCase("4+"))result=MicroscopyResult.PLUS4;
		emr.setResult(result);
		DateCell dc=(DateCell)r[start+2];
		emr.setDateCollected(dc.getDate());
		emr.setTbcase(tbcase);
		Laboratory laboratory=entityManager.find(Laboratory.class, 942680);
		emr.setLaboratory(laboratory);
			//adminunit lab
		}
		}
		return emr;
		
	}
	private ExamCulture setCultureExam(int start, Cell[] r,TbCase tbcase){
		ExamCulture_Az ec=null;
		if (r.length>start+1){
		if (r[start+1].getType()==CellType.DATE) {
			ec=new ExamCulture_Az();
			String s=r[start].getContents();
			ec.setSampleNumber(s);
			DateCell dc=(DateCell)r[start+1];
			ec.setDateCollected(dc.getDate());
			if (r[start+2].getType()==CellType.DATE) ec.setDateRelease(((DateCell)r[start+2]).getDate());
			s=r[start+3].getContents();
			if (s.equalsIgnoreCase("NEG"))	ec.setResult(CultureResult.NEGATIVE);
			if (s.equalsIgnoreCase("POS"))	ec.setResult(CultureResult.POSITIVE);
			if (s.equalsIgnoreCase("CON"))	ec.setResult(CultureResult.CONTAMINATED);	
			ec.setMethod(method);
			ec.setTbcase(tbcase);
			ec.setLaboratory(laboratory);
		}
		}
		return ec;

	}
	
	/**
	 * Generating a new patient number if it was not generated yet
	 * @return
	 */
	private void generateCaseNumber(TbCase tbcase){		
		Patient p = tbcase.getPatient();
		if (p.getRecordNumber() == null) {
			SequenceGenerator sequenceGenerator = (SequenceGenerator) App.getComponent("sequenceGenerator");
			int val = sequenceGenerator.generateNewNumber("CASE_NUMBER");
			p.setRecordNumber(val);
		}
		// generate new case number
		//Gets the major case number of this patient with diagnosisdate before the case in question.
		Integer caseNum = (Integer)App.getEntityManager().createQuery("select max(c.caseNumber) from TbCase c where c.patient.id = :id and c.diagnosisDate < :dt")
			.setParameter("id", p.getId())
			.setParameter("dt", tbcase.getDiagnosisDate())
			.getResultList().get(0);
		
		if (caseNum == null)
			//If there is no casenum before this one it sets the digit 1 for this case.
			caseNum = 1;
		else{
			//If there is a casenum before this case it sets this number plus one.
			caseNum++;
		}
		
		// Returns the ids of the cases cronologicaly after the case in memory
		ArrayList<Integer> lst = (ArrayList<Integer>) App.getEntityManager().createQuery("select id from TbCase c " + 
		        "where c.patient.id = :id and c.diagnosisDate > :dt order by c.diagnosisDate")
		        .setParameter("dt", tbcase.getDiagnosisDate())
		        .setParameter("id", p.getId())
		        .getResultList();
		
		// updates caseNums according to the cronological order
		int num = caseNum + 1;
		for (Integer id: lst) {
		   App.getEntityManager().createQuery("update TbCase set caseNumber = :num where id=:id")
		      .setParameter("num", num)
		      .setParameter("id", id)
		      .executeUpdate();
		   num++;
		}
		
		tbcase.setCaseNumber(caseNum);

		
	}
	public void addLog(String s) {	
		log.append(s);
		log.append('\n');
	}
	
	//---------------------------------
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	
/*	public void beginTransaction() {
		try {
			getTransaction().begin();
			getEntityManager().joinTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	*//**
	 * Commit a transaction that is under progress 
	 *//*
	public void commitTransaction() {
		try {
			getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	protected UserTransaction getTransaction() {
		if (transaction == null)
			transaction = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		return transaction;
	}
*/	public Workspace getWorkspace() {
		return getEntityManager().find(Workspace.class, workspaceID);
	}

private PrescribedMedicine getMedicineByShortName(String name,String st){
	List<Medicine> lst=(List<Medicine>) entityManager.createQuery("from Medicine m where abbrevName=:n and strength=:st and workspace.id =8")
	.setParameter("n", name)
		.setParameter("st", st)
	.getResultList();
	PrescribedMedicine pm = new PrescribedMedicine();
	pm.setMedicine(lst.get(0));
	pm.setFrequency(7);
	pm.setSource(source);
	pm.setTbcase(tc);
	return pm;
}

private int getDoseUnit(){
	int w=3;
	if (weight<51)
		w=1;
	else if (weight<60)
		w=2;
		int dose=1;
	//Etambutol (100, 400 mq) таб	25 mq/kq	800-1200 mq	1200-1600 mq	1600-2000 mq
	//Pirazinamid (500 mq) таб	30-40 mq/kq	1000-1750 mq	1750-2000 mq	2000-2500 mq	
		switch (w){
 		case 1: dose=3;
 		break;
 		case 2: dose=4;
 		break;
 		case 3: dose=5;
 		break;
 		}
return dose;
}

/**
 * Create list of prescribed medicines according to the list of {@link MedicineRegimen} objects
 * II categoria
 * @param lst
 * @return
 */
protected List<PrescribedMedicine> createPrescribedMedicineList2( TbCase tbcase) {
	List<PrescribedMedicine> meds = new ArrayList<PrescribedMedicine>();
	Calendar endInt=Calendar.getInstance();
	endInt.setTime(tbcase.getIniContinuousPhase());
	endInt.add(Calendar.DAY_OF_YEAR, -1);
	Period intPeriod=new Period(tbcase.getRegistrationDate(),endInt.getTime());
	Period intConPeriod=new Period(tbcase.getIniContinuousPhase(),treatmPd.getEndDate());
	Calendar datePhase=Calendar.getInstance();
	datePhase.setTime(tbcase.getRegistrationDate());
	datePhase.add(Calendar.MONTH, 2);
	Period sPeriod=new Period(tbcase.getRegistrationDate(),datePhase.getTime());
	
	PrescribedMedicine pm = getMedicineByShortName("S","1000");
	pm.setPeriod(sPeriod);
	pm.setDoseUnit(1);
	meds.add(pm);
	
	pm = getMedicineByShortName("H","300");
	pm.setPeriod(intPeriod);
	pm.setDoseUnit(1);
	meds.add(pm);
	
	pm = getMedicineByShortName("R","150");
	pm.setPeriod(intPeriod);
	pm.setDoseUnit(4);
	meds.add(pm);
	
	pm = getMedicineByShortName("HR","75/150");
	pm.setPeriod(intConPeriod);
	pm.setDoseUnit(4);
	meds.add(pm);
	
	pm = getMedicineByShortName("E","400");
	pm.setPeriod(intPeriod);
	pm.setDoseUnit(getDoseUnit());
	meds.add(pm);
	
	pm = getMedicineByShortName("E","400");
	pm.setPeriod(intConPeriod);
	pm.setDoseUnit(getDoseUnit());
	meds.add(pm);
	
	pm = getMedicineByShortName("Z","500");
	pm.setPeriod(intPeriod);
	
	pm.setDoseUnit(getDoseUnit());
	meds.add(pm);
	
	return meds;
}

/**
 *  * I categoria
 * @param tbcase
 * @return
 */
protected List<PrescribedMedicine> createPrescribedMedicineList( TbCase tbcase) {
	List<PrescribedMedicine> meds = new ArrayList<PrescribedMedicine>();
	Calendar endInt=Calendar.getInstance();
	endInt.setTime(tbcase.getIniContinuousPhase());
	endInt.add(Calendar.DAY_OF_YEAR, -1);
		Period sPeriod=new Period(tbcase.getRegistrationDate(),endInt.getTime());
	Period intConPeriod=new Period(tbcase.getIniContinuousPhase(),treatmPd.getEndDate());
	//Calendar datePhase=Calendar.getInstance();
	//datePhase.setTime(tbcase.getRegistrationDate());
	//datePhase.add(Calendar.MONTH, 2);
		
	PrescribedMedicine pm = getMedicineByShortName("H","300");
	pm.setPeriod(sPeriod);
	pm.setDoseUnit(1);
	meds.add(pm);
	
	pm = getMedicineByShortName("R","150");
	pm.setPeriod(sPeriod);
	pm.setDoseUnit(4);
	meds.add(pm);
	
	pm = getMedicineByShortName("HR","75/150");
	pm.setPeriod(intConPeriod);
	pm.setDoseUnit(4);
	meds.add(pm);
	
	pm = getMedicineByShortName("E","400");
	pm.setPeriod(sPeriod);
	pm.setDoseUnit(getDoseUnit());
	meds.add(pm);
	
	pm = getMedicineByShortName("E","400");
	pm.setPeriod(intConPeriod);
	pm.setDoseUnit(getDoseUnit());
	meds.add(pm);
	
	pm = getMedicineByShortName("Z","500");
	pm.setPeriod(sPeriod);
	pm.setDoseUnit(getDoseUnit());
	meds.add(pm);
	
	return meds;
}
private Regimen getRegimen(int n){
	String name;
	if (n==1)
	name="Kateqoriya I";
	 else name="Kateqoriya II";
	List<Regimen>lst=entityManager.createQuery("from Regimen r where regimen_name=:n  and workspace.id =8")
	.setParameter("n", name).getResultList();
	return lst.get(0);
}

}
