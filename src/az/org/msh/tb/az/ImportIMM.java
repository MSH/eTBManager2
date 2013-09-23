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
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Regimen;
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
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.misc.SequenceGenerator;
import org.msh.utils.date.Period;
import org.jboss.seam.international.Messages;
import org.jboss.seam.transaction.UserTransaction;
@Name("importIMM")
public class ImportIMM {
	private EntityManager entityManager;
	private InputStream file;
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
	public boolean execute() {
		try {
			Workbook workbook = Workbook.getWorkbook(new File("c:/temp/test.xls"));
			Sheet sheet = workbook.getSheet(0);
			if (entityManager==null){
				entityManager = (EntityManager)Component.getInstance("entityManager");
			}
			units=getTbunit();
			lastCols=sheet.getColumns()-1;
			method = entityManager.find(FieldValue.class, 939311);   //Levestain-Jensen
			method2 = entityManager.find(FieldValue.class, 939316);//GeneXpert 
			laboratory=entityManager.find(Laboratory.class, 942680);  //IMM lab
			for (int i = 2; i < sheet.getRows(); i++) {
				Cell[] r = sheet.getRow(i);
				importRecord(r);
			}
	
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void importRecord(Cell[] r) {
		MedicalExamination me=new MedicalExamination();
		TbCaseAZ tc = new TbCaseAZ();
		List<MedicalExamination> examinations=tc.getExaminations();
		me.setTbcase(tc);
		examinations.add(me);
		Patient p = new Patient();	
		tc.setPatient(p);
		generateCaseNumber(tc);
		AdministrativeUnit a=entityManager.find(AdministrativeUnit.class, adminunitID);
		
		Address addr=new Address();
		addr.setAdminUnit(a);
		tc.setNotifAddress(addr);
		tc.setCurrentAddress(addr);
		p.setWorkspace(getWorkspace());
		tc.setValidationState(ValidationState.WAITING_VALIDATION);
		p.setSecurityNumber(r[1].getContents());//b
		tc.setClassification(CaseClassification.TB);
		tc.setDiagnosisType(DiagnosisType.CONFIRMED); 
		Regimen regimen=entityManager.find(Regimen.class, 940744);
		tc.setRegimen(regimen);
		tc.setRegimenIni(regimen);
		try {
			DateCell dc = (DateCell)r[2];//c
			p.setBirthDate(dc.getDate());
			
			dc = (DateCell)r[3];//d
			tc.setRegistrationDate(dc.getDate());
			tc.setDiagnosisDate(dc.getDate());
			tc.setAge(calculateAge(p.getBirthDate(), tc.getRegistrationDate()));
			p.setLastName(r[4].getContents());//e
			p.setName(r[5].getContents()); //f
			p.setMiddleName(r[6].getContents()); //g
			
			p.setMotherName(r[7].getContents());  //h
			
			String s = r[8].getContents();
			if ("M".equals(s))
				p.setGender(Gender.MALE);
			if ("F".equals(s))
				p.setGender(Gender.FEMALE);
			
			s = r[9].getContents();
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
					s = r[11].getContents(); 
			if (s.length() == 2)
				tc.setReferToOtherTBUnit(false);
			else
				tc.setReferToOtherTBUnit(true);
			s=r[12].getContents();
			tc.setReferToTBUnit(getTbunitbyName(s));
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
			if (!s.equalsIgnoreCase("")) me.setWeight(new Double(s));
			s=r[17].getContents();//r-height
			if (!s.equalsIgnoreCase("")) me.setHeight(new Float(s));
			me.setDate(tc.getDiagnosisDate());
			me.setUsingPrescMedicines(YesNoType.YES);
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
			//U - localisation
			s=r[21].getContents();//V - case finding
			if (s.equalsIgnoreCase("")) tc.setCaseFindingStrategy(CaseFindingStrategy.NA);
			if (s.equalsIgnoreCase("ACTIVE")) tc.setCaseFindingStrategy(CaseFindingStrategy.ACTIVE);
			if (s.equalsIgnoreCase("PASSIV")) tc.setCaseFindingStrategy(CaseFindingStrategy.PASSIVE);
			s=r[22].getContents();//W - prev. treatment
			s=r[23].getContents();//x - prison
			if (s.equalsIgnoreCase("")) {
				tc.setNumberOfImprisonments(1);
			} else {
				tc.setNumberOfImprisonments(new Integer(s));
			}
			if	(r[24].getType()==CellType.DATE) {
				dc = (DateCell)r[24];
				tc.setInprisonIniDate(dc.getDate());
			}
			s=r[lastCols].getContents();
			if (!s.equalsIgnoreCase("")) setComorbidity(tc,s);
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
				Period treatmPd=new Period (dc.getDate(),tc.getOutcomeDate());
				tc.setTreatmentPeriod(treatmPd);
				me.setDate(dc.getDate());
				currunit.setPeriod(treatmPd);
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
			//commitTransaction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ExamDST setExamDST(int start, Cell[] r, TbCaseAZ tc,int round) {
		ExamDSTAZ ed=null;
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
		String comments;
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

	public void setFile(InputStream file) {
		this.file = file;
	}

	public InputStream getFile() {
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
		System.out.println(unit.getName().getName1());
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
		System.out.println( result.getName().getName1());
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
		System.out.println( result.getName().getName1());
		return result;
	}
	
	public Integer calculateAge(Date birthday, Date registrationDate)
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
	
	public ExamMicroscopy setMicroExam(int start, Cell[] r, TbCase tbcase){
		ExamMicroscopyAZ emr=null;
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
		return emr;
		
	}
	public ExamCulture setCultureExam(int start, Cell[] r,TbCase tbcase){
		ExamCulture_Az ec=null;
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
		return ec;

	}
	
	/**
	 * Generating a new patient number if it was not generated yet
	 * @return
	 */
	public void generateCaseNumber(TbCase tbcase){		
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
	
	//---------------------------------
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	
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
	protected UserTransaction getTransaction() {
		if (transaction == null)
			transaction = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		return transaction;
	}
	public Workspace getWorkspace() {
		return getEntityManager().find(Workspace.class, workspaceID);
	}
}
