package org.msh.tb.az.export;

 import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.ExamMicroscopyAZ;
import org.msh.tb.az.entities.ExamXRayAZ;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.CaseDispensing;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.export.CaseExport;
import org.msh.tb.export.DstExport;
import org.msh.tb.export.ExcelCreator;
import org.msh.utils.date.DateUtils;

@Name("caseExportAZ")
public class CaseExportAZ extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;
	private String hqlSelect;
	private String hqlFrom;
	private List<TbCaseAZ> cases;
	
	private List<Object[]> microscopyList;
	private DstExport dstExport;
	private List<Object[]> xrayList;
	ExcelCreator excel;
	private Date treatmDate;
	public CaseExportAZ(ExcelCreator excel) {
		super(excel);
	}

	@Override
	protected String getHQLValidationState() {
		return null;
	}
	
	@Override
	public void addTitles() {
		// add title line
		 excel = getExcel();
		InfoCountryLevels levelInfo = getLevelInfo();
		
		excel.addTextFromResource("Patient.name", "title");
		excel.addTextFromResource("CaseClassification", "title");
		excel.addTextFromResource("Patient.caseNumber", "title");
		excel.addTextFromResource("Patient.recordNumber", "title");
		excel.addTextFromResource("CaseState", "title");
		excel.addTextFromResource("TbCase.registrationCode", "title");
		excel.addTextFromResource("Gender", "title");
		excel.addTextFromResource("Patient.birthDate", "title");
		excel.addTextFromResource("TbCase.age", "title");
		excel.addTextFromResource("Nationality", "title");
		excel.addTextFromResource("Address", "title");
		if (levelInfo.isHasLevel1()) 
			excel.addText(levelInfo.getNameLevel1().toString(), "title");
		if (levelInfo.isHasLevel2()) 
			excel.addText(levelInfo.getNameLevel2().toString(), "title");
		if (levelInfo.isHasLevel3()) 
			excel.addText(levelInfo.getNameLevel3().toString(), "title");
		if (levelInfo.isHasLevel4()) 
			excel.addText(levelInfo.getNameLevel4().toString(), "title");
		if (levelInfo.isHasLevel5()) 
			excel.addText(levelInfo.getNameLevel5().toString(), "title");
//		excel.addTextFromResource("Address.zipCode", "title");
//		excel.addTextFromResource("TbCase.phoneNumber", "title");
//		excel.addTextFromResource("TbCase.mobileNumber", "title");
		excel.addTextFromResource("az_AZ.TbCase.notificationUnit", "title");
		// notification health unit
		excel.addText(levelInfo.getNameLevel1().toString(), "title");
		excel.addTextFromResource("TbCase.registrationDate", "title");
		excel.addTextFromResource("TbCase.diagnosisDate", "title");
		excel.addTextFromResource("DiagnosisType", "title");
		excel.addTextFromResource("TbCase.iniTreatmentDate", "title");
		excel.addTextFromResource("TbCase.endTreatmentDate", "title");
		excel.addTextFromResource("DrugResistanceType", "title");
		excel.addTextFromResource("InfectionSite", "title");
		excel.addTextFromResource("TbField.PULMONARY_TYPES", "title");
		excel.addTextFromResource("TbField.EXTRAPULMONARY_TYPES", "title");
		excel.addTextFromResource("TbField.EXTRAPULMONARY_TYPES", "title");
		excel.addTextFromResource("PatientType", "title");
		
		excel.addTextFromResource("cases.outcome", "title");
		excel.addTextFromResource("TbCase.outcomeDate", "title");
		excel.addTextFromResource("TbCase.ownerUnit", "title");
		excel.addTextFromResource("TbCase.ownerUnitAdr", "title");
		excel.addTextFromResource("cases.system.create", "title");
		excel.addText(App.getMessage("cases.system.create")+" ("+App.getMessage("User")+")", "title");
		excel.addTextFromResource("cases.system.edit", "title");
		excel.addText(App.getMessage("cases.system.edit")+" ("+App.getMessage("User")+")", "title");
				excel.addTextFromResource("excel.dateInEIDSS", "title");
		excel.addText(getMessages().get("User.name")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("Patient.lastName")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("Patient.middleName")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("az_EIDSS_Notify_LPU"), "title");
		excel.addText(getMessages().get("az_EIDSS_Address")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addTextFromResource("excel.dateRegAfterEIDSS", "title");
		excel.addText(getMessages().get("az_EIDSS_Age")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("Patient.birthDate")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("excel.dateNotifEIDSS")+" "+getMessages().get("excel.fromEIDSS"), "title");
				excel.addTextFromResource("TbCase.eidssid", "title");
		excel.addText(getMessages().get("az_AZ.case.unicalID")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText("(last) X-ray result - Presentation", "title");
		excel.addText("(last) X-ray result - Localization", "title");
		excel.addText("(last) DST Type - Streptomycin (S)", "title");
		excel.addText("(last) DST Type - Isoniazid (H)", "title");
		excel.addText("(last) DST Type - Rifampicin (R)", "title");
		excel.addText("(last) DST Type - Ethambutol (E)", "title");
		excel.addText("(last) DST Type - Pyrazinamide (Z)", "title");
		excel.addText("(last) DST Type - Protionamide (Pto)", "title");
		excel.addText("(last) DST Type - Ofloxacin (Ofx)", "title");
		excel.addText("(last) DST Type - Cycloserine (Cs)", "title");
		excel.addText("(last) DST Type - P-Aminosalicylic Acid (PAS)", "title");
		excel.addText("(last) DST Type - Capreomycin (Cm)", "title");
		excel.addText("(last) DST Type - Amikacin (Am)", "title");
		excel.addText("(last) DST Type - Moxifloxacin (Mfx)", "title");
		excel.addText("(last) DST Type - Kanamycin (Km)", "title");
		excel.addText("(last) DST Type - Ciprofloxacin (Cfx)", "title");
		excel.addText("(last) DST Type - Gatifloxacin (Gati)", "title");
		excel.addText("(last) DST Type - Rifabutin (Rfb)", "title");
		excel.addText("(last) DST Type - Ethionamide (Eto)", "title");
		excel.addText("(last) DST Type - Levofloxacin (Lfx)", "title");
		excel.addText("(last) DST Type - Terizidone (Trd)", "title");
		excel.addTextFromResource("Regimen", "title");
		excel.addText(getMessages().get("TbField.SEVERITY_MARKS")+" - Cavitation", "title");
		excel.addText(getMessages().get("excel.counttests")+getMessages().get("cases.exammicroscopy"), "title");
		excel.addText(getMessages().get("excel.counttests")+getMessages().get("excel.tests.cult"), "title");
		excel.addText(getMessages().get("excel.counttests")+getMessages().get("excel.tests.mol"), "title");
		excel.addText(getMessages().get("excel.counttests")+getMessages().get("cases.examxray"), "title");
		excel.addText(getMessages().get("excel.counttests")+getMessages().get("excel.tests.dst"), "title");
		excel.addTextFromResource("excel.countconsult", "title");
		excel.addTextFromResource("cases.comorbidities", "title");
		excel.addTextFromResource("cases.sideeffects", "title");
		excel.addTextFromResource("excel.treat.alldays", "title");
		excel.addTextFromResource("excel.treat.reldays", "title");
		excel.addTextFromResource("TbCase.referToOtherTBUnit", "title");
		excel.addTextFromResource("az_AZ.ValidationState.title", "title");
		
		excel.addText("(last) X-ray result - date");
		excel.addText("(1st) X-ray result - Presentation");
		excel.addText("(1st) X-ray result - Localization");
		excel.addText("(1st) X-ray result - date");
		// microscopy
		String examDate=getMessages().get("cases.exammicroscopy")+"-"+getMessages().get("cases.details.date");
		String examRes=getMessages().get("cases.exammicroscopy")+"-"+getMessages().get("cases.details.result");
		excel.addText("(last)"+examDate, "title"); 
		excel.addText("(last)"+examRes, "title"); 
		excel.addText("(1st)"+examDate, "title"); 
		excel.addText("(1st)"+examRes, "title"); 
		excel.addText("(6mth)"+examDate, "title"); 
		excel.addText("(6mth)"+examRes, "title"); 
		//culture
		 examDate=getMessages().get("cases.examculture")+"-"+getMessages().get("cases.details.date");
		 examRes=getMessages().get("cases.examculture")+"-"+getMessages().get("cases.details.result");
		 String examMethod=getMessages().get("cases.examculture")+"-method";
		 excel.addText("(last)"+examDate, "title"); 
		 excel.addText("(last)"+examRes, "title"); 
		 excel.addText("(last)"+examMethod, "title"); 
		 excel.addText("(1st)"+examDate, "title"); 
		 excel.addText("(1st)"+examRes, "title"); 
		 excel.addText("(1st)"+examMethod, "title"); 
		 excel.addText("(6mth)"+examDate, "title"); 
		 excel.addText("(6mth)"+examRes, "title"); 
		 excel.addText("(6mth)"+examMethod, "title"); 
		 //DST
		 excel.addText(" (last) DST result - date");
		 excel.addText(" (last) DST result - method");
		 excel.addText(" (1st) DST result - date");
		 excel.addText(" (1st) DST result - method");
			excel.addText("(1st) DST Type - Streptomycin (S)", "title");
			excel.addText("(1st) DST Type - Isoniazid (H)", "title");
			excel.addText("(1st) DST Type - Rifampicin (R)", "title");
			excel.addText("(1st) DST Type - Ethambutol (E)", "title");
			excel.addText("(1st) DST Type - Pyrazinamide (Z)", "title");
			excel.addText("(1st) DST Type - Protionamide (Pto)", "title");
			excel.addText("(1st) DST Type - Ofloxacin (Ofx)", "title");
			excel.addText("(1st) DST Type - Cycloserine (Cs)", "title");
			excel.addText("(1st) DST Type - P-Aminosalicylic Acid (PAS)", "title");
			excel.addText("(1st) DST Type - Capreomycin (Cm)", "title");
			excel.addText("(1st) DST Type - Amikacin (Am)", "title");
			excel.addText("(1st) DST Type - Moxifloxacin (Mfx)", "title");
			excel.addText("(1st) DST Type - Kanamycin (Km)", "title");
			excel.addText("(1st) DST Type - Ciprofloxacin (Cfx)", "title");
			excel.addText("(1st) DST Type - Gatifloxacin (Gati)", "title");
			excel.addText("(1st) DST Type - Rifabutin (Rfb)", "title");
			excel.addText("(1st) DST Type - Ethionamide (Eto)", "title");
			excel.addText("(1st) DST Type - Levofloxacin (Lfx)", "title");
			excel.addText("(1st) DST Type - Terizidone (Trd)", "title");
		excel.addText("",null);
	}
	
	@Override
	public TbCase exportContent(int index) {
	
		InfoCountryLevels levelInfo = getLevelInfo();
		TbCaseAZ tbcase = getCasesAz().get(index);
		
		excel.addText(tbcase.getPatient().getFullName());
		excel.addTextFromResource(tbcase.getClassification().getKey());
		excel.addText(tbcase.getDisplayCaseNumber());
		excel.addText(tbcase.getPatient().getSecurityNumber());
		if (tbcase.isFirstEIDSSBind())
			excel.addTextFromResource("cases.detailseidsstitle");
		else
			excel.addValue(tbcase, "state");
		excel.addText(tbcase.getRegistrationCode());
		excel.addValue(tbcase.getPatient(), "gender");
		excel.addDate(tbcase.getPatient().getBirthDate());
		excel.addNumber(tbcase.getAge());
		excel.addValue(tbcase, "nationality");
		excel.addText(tbcase.getNotifAddress().getAddress());
		if (levelInfo.isHasLevel1())
			excel.addValue(tbcase, "notifAddress.adminUnit.parentLevel1");
		if (levelInfo.isHasLevel2())
			excel.addValue(tbcase, "notifAddress.adminUnit.parentLevel2");
		if (levelInfo.isHasLevel3())
			excel.addValue(tbcase, "notifAddress.adminUnit.parentLevel3");
		if (levelInfo.isHasLevel4())
			excel.addValue(tbcase, "notifAddress.adminUnit.parentLevel4");
		if (levelInfo.isHasLevel5())
			excel.addValue(tbcase, "notifAddress.adminUnit.parentLevel5");
//		excel.addValue(tbcase, "notifAddress.zipCode");
//		excel.addText(tbcase.getPhoneNumber());
//		excel.addText(tbcase.getMobileNumber());
		excel.addValue(tbcase, "notificationUnit.name");
		excel.addValue(tbcase, "notificationUnit.adminUnit.parentLevel1");
		excel.addDate(tbcase.getRegistrationDate());
		excel.addDate(tbcase.getDiagnosisDate());
		excel.addValue(tbcase, "diagnosisType");
		excel.addValue(tbcase, "treatmentPeriod.iniDate");
		excel.addValue(tbcase, "treatmentPeriod.endDate");
		excel.addValue(tbcase, "drugResistanceType");
		excel.addValue(tbcase, "infectionSite");
		excel.addValue(tbcase, "pulmonaryType");
		excel.addValue(tbcase, "extrapulmonaryType");
		excel.addValue(tbcase, "extrapulmonaryType2");
		excel.addValue(tbcase, "patientType");

		if (tbcase.getState().ordinal()>2){
			excel.addValue(tbcase, "state");
			excel.addDate(tbcase.getOutcomeDate());
			}
		else{
			excel.addText("");
			excel.addText("");
		}
		Tbunit tu = tbcase.getOwnerUnit();
		if (tu == null){
			excel.addText("");
			excel.addText("");
		}
		else{
			excel.addText(tu.toString());
			excel.addText(tu.getAdminUnit().getName().getDefaultName());
		}
		
		excel.addDate(tbcase.getSystemDate());
		excel.addValue(tbcase,"createUser.name");
		excel.addDate(tbcase.getEditingDate());
		excel.addValue(tbcase,"editingUser.name");
		
		
		String[] ei = new String[] {"",""}; 
		if (tbcase.getEIDSSComment()!=null)
			ei = tbcase.getEIDSSComment().split(" / ");
		
		if (ei.length>=3){
			Date dInEIDSS;//inner date EIDSS
			try {
				dInEIDSS = new SimpleDateFormat("dd-MM-yyyy").parse(ei[2]);
				excel.addDate(dInEIDSS);
			} catch (ParseException e) {
				excel.addText(ei[2]);
			}
		}
		else{
			excel.addText("");
			excel.addText("");
			excel.addText("");
		}
		// BEGIN name from EIDSS 
		if (ei.length>=4){
			String [] names = ei[3].split(" ");
			excel.addText(names.length>=1 ? names[0]:"");
			excel.addText(names.length>=2 ? names[1]:"");
			excel.addText(names.length>=3 ? names[2]:"");
		}
		else
			excel.addText(""); 
		// END name from EIDSS 
		excel.addText(ei[0]);
		excel.addText(ei[1]);
		excel.addDate(tbcase.getSystemDate());
		if (ei.length>=5)
			excel.addNumber(Integer.parseInt(ei[4]));
		else
			excel.addText("");
		
		if (ei.length>=6){
			Date eidssDBirth;
			try {
				eidssDBirth = new SimpleDateFormat("dd-MM-yyyy").parse(ei[5]);
				excel.addDate(eidssDBirth);
			} catch (ParseException e) {
				excel.addText(ei[5]);
			}
		}
		else
			excel.addText("");
		
		if (ei.length>=7){
			Date notifDate;
			try {
				notifDate = new SimpleDateFormat("dd-MM-yyyy").parse(ei[6]);
				excel.addDate(notifDate);
			} catch (ParseException e) {
				excel.addText(ei[6]);
			}
		}
		else
			excel.addText("");
		
		excel.addText(tbcase.getLegacyId());
		excel.addText(tbcase.getUnicalID());
		AddXrayData (tbcase,"last");
		ExamDST DSTexam=getLastDST(tbcase);
		excel.addText(getLastDSTExam(DSTexam, "S"));
		excel.addText(getLastDSTExam(DSTexam, "H"));
		excel.addText(getLastDSTExam(DSTexam, "R"));
		excel.addText(getLastDSTExam(DSTexam, "E"));
		excel.addText(getLastDSTExam(DSTexam, "Z"));
		excel.addText(getLastDSTExam(DSTexam, "Pto"));
		excel.addText(getLastDSTExam(DSTexam, "Ofx"));
		excel.addText(getLastDSTExam(DSTexam, "Cs"));
		excel.addText(getLastDSTExam(DSTexam, "PAS"));
		excel.addText(getLastDSTExam(DSTexam, "Cm"));
		excel.addText(getLastDSTExam(DSTexam, "Am"));
		excel.addText(getLastDSTExam(DSTexam, "Mfx"));
		excel.addText(getLastDSTExam(DSTexam, "Km"));
		excel.addText(getLastDSTExam(DSTexam, "Cfx"));
		excel.addText(getLastDSTExam(DSTexam, "Gati"));
		excel.addText(getLastDSTExam(DSTexam, "Rfb"));
		excel.addText(getLastDSTExam(DSTexam, "Eto"));
		excel.addText(getLastDSTExam(DSTexam, "Lfx"));
		excel.addText(getLastDSTExam(DSTexam, "Trd"));

		excel.addValue(tbcase,"regimen");
		if (indCarvity(tbcase)>-1)
			excel.addText("Yes");
		else
			excel.addText("No");
		excel.addNumber(tbcase.getExamsMicroscopy().size());
		excel.addNumber(tbcase.getExamsCulture().size());
		excel.addNumber(0);//TODO Кол-во тестов: Молекулярные методы
		excel.addNumber(tbcase.getResXRay().size());
		excel.addNumber(tbcase.getExamsDST().size());
		excel.addNumber(tbcase.getExaminations().size());
		if (tbcase.getComorbidities().size()>0)
			excel.addText("Some");
		else
			excel.addText("No any");
		if (tbcase.getSideEffects().size()>0)
			excel.addText("Some");
		else
			excel.addText("No any");
		
		int disp = 0;
		for (CaseDispensing cd:tbcase.getDispensing()){
			disp += cd.getTotalDays();
		}
		
		excel.addNumber(mountPlanned(tbcase));
		excel.addNumber(disp);
		excel.addTextFromResource(tbcase.isReferToOtherTBUnit()?"TbCase.referUnit.other":"TbCase.referUnit.own");
		excel.addTextFromResource("az_AZ."+tbcase.getValidationState().getKey());
		AddXrayData (tbcase,"first");
		if( tbcase.getTreatmentPeriod()!=null){
			treatmDate=tbcase.getTreatmentPeriod().getIniDate();
		} else{
			treatmDate=null;
		}
		addMicroData(tbcase);
		addCultureData(tbcase);
		if (DSTexam!=null){
			excel.addValue(DSTexam.getDateRelease()!=null?DSTexam.getDateRelease():DSTexam.getDateCollected());
			excel.addValue(DSTexam.getMethod());
		}else{
			excel.addText("");
			excel.addText("");
		}
		DSTexam=getFirstDST(tbcase);
		if (DSTexam!=null){
			excel.addValue(DSTexam.getDateRelease()!=null?DSTexam.getDateRelease():DSTexam.getDateCollected());
			excel.addValue(DSTexam.getMethod());
		}else{
			excel.addText("");
			excel.addText("");
		}
		excel.addText(getLastDSTExam(DSTexam, "S"));
		excel.addText(getLastDSTExam(DSTexam, "H"));
		excel.addText(getLastDSTExam(DSTexam, "R"));
		excel.addText(getLastDSTExam(DSTexam, "E"));
		excel.addText(getLastDSTExam(DSTexam, "Z"));
		excel.addText(getLastDSTExam(DSTexam, "Pto"));
		excel.addText(getLastDSTExam(DSTexam, "Ofx"));
		excel.addText(getLastDSTExam(DSTexam, "Cs"));
		excel.addText(getLastDSTExam(DSTexam, "PAS"));
		excel.addText(getLastDSTExam(DSTexam, "Cm"));
		excel.addText(getLastDSTExam(DSTexam, "Am"));
		excel.addText(getLastDSTExam(DSTexam, "Mfx"));
		excel.addText(getLastDSTExam(DSTexam, "Km"));
		excel.addText(getLastDSTExam(DSTexam, "Cfx"));
		excel.addText(getLastDSTExam(DSTexam, "Gati"));
		excel.addText(getLastDSTExam(DSTexam, "Rfb"));
		excel.addText(getLastDSTExam(DSTexam, "Eto"));
		excel.addText(getLastDSTExam(DSTexam, "Lfx"));
		excel.addText(getLastDSTExam(DSTexam, "Trd"));
		return tbcase;
	}

	/**
	 * Fill calendar with planned prescription days 
	 */
	protected int mountPlanned(TbCase tbcase) {
		int res=0;
		if (tbcase.getTreatmentPeriod() == null) return 0;
		Date inidt = tbcase.getTreatmentPeriod().getIniDate();
		Date enddt = tbcase.getTreatmentPeriod().getEndDate();
		
		if ((inidt == null) || (enddt == null))
			return 0;
		while (!inidt.after(enddt)) {
			if (tbcase.isDayPrescription(inidt))
				res++;
			inidt = DateUtils.incDays(inidt, 1);
		}
		return res;
	}
	
	/**
	 * @return the Carvity index from SeverityMarks list of case
	 */
	private int indCarvity(TbCaseAZ tc){
		for (int i = 0; i < tc.getSeverityMarks().size(); i++) {
			if (tc.getSeverityMarks().get(i).getSeverityMark().getId().intValue() == 939370)
				return i;
		}
		return -1;
	}
	
	private ExamXRay getFirstXRayExam(TbCase tc){
	
		ExamXRay res = tc.getResXRay().get(0);
		for (int i = 1; i < tc.getResXRay().size(); i++) {
			ExamXRay x = tc.getResXRay().get(i);
			if (x.getDate().before(res.getDate()))
				res = x;
		}
		return res;
	}
	
	private ExamXRay getLastXRayExam(TbCase tc){
		
		ExamXRay res = tc.getResXRay().get(0);
		for (int i = 1; i < tc.getResXRay().size(); i++) {
			ExamXRay x = tc.getResXRay().get(i);
			if (x.getDate().after(res.getDate()))
				res = x;
		}
		return res;
	}
	
	/**
	 * @param tbcase
	 * add x-ray First Result and last result
	 * @param result 
	 */
	private void AddXrayData (TbCase tbcase, String result){
	int count=2;
	ExamXRayAZ lastX=null;
	if(result.equalsIgnoreCase("first"))
		count=4;	
		if (tbcase.getResXRay() == null){
			for (int i=0;i<count;i++)excel.addText("");
			return;
		}
		if (tbcase.getResXRay().size() == 0) {
			for (int i=0;i<count;i++)excel.addText("");
			return;
		}
		if (tbcase.getResXRay().size() > 1){
			ExamXRay x = getLastXRayExam(tbcase);
			lastX = (ExamXRayAZ) getEntityManager().find(ExamXRayAZ.class, x.getId());
		}
		if (result.equalsIgnoreCase("last")){
			if (tbcase.getResXRay().size() > 1){
						
				excel.addText(lastX.getPresentation()!=null ? lastX.getPresentation().getName().getDefaultName() : "");
				excel.addText(lastX.getLocalization()!=null ? lastX.getLocalization().getName().getDefaultName() : "");
			} else{
				excel.addText("");
				excel.addText("");
			}
		}
		if (result.equalsIgnoreCase("first")){
			ExamXRay x = getFirstXRayExam(tbcase);
			if (lastX!=null) {
				excel.addValue(lastX.getDate());
			} else excel.addText("");
			
			if (x!=null){
				ExamXRayAZ xaz = (ExamXRayAZ) getEntityManager().find(ExamXRayAZ.class, x.getId());
				excel.addText(xaz.getPresentation()!=null ? xaz.getPresentation().getName().getDefaultName() : "");
				excel.addText(xaz.getLocalization()!=null ? xaz.getLocalization().getName().getDefaultName() : "");
				excel.addValue(xaz.getDate());
			}
		}
	}

	
	/**
	 * Get last DST-result
	 * @param tc case
	 * @param s abbrevname of substance, ex. "H"
	 * */
	
	private ExamDST getLastDST(TbCase tc){
		
		if (tc.getExamsDST() == null) return null;
		if (tc.getExamsDST().size() < 2) return null;
		ExamDST res=tc.getExamsDST().get(0);
		for (int i = 0; i < tc.getExamsDST().size(); i++) {
			ExamDST ex = tc.getExamsDST().get(i);
			if (ex.getDateCollected().after(res.getDateCollected())) res=ex;
		}
		return res;
	}
private ExamDST getFirstDST(TbCase tc){
		
		if (tc.getExamsDST() == null) return null;
		if (tc.getExamsDST().size() == 0) return null;
		ExamDST res=tc.getExamsDST().get(0);
		for (int i = 0; i < tc.getExamsDST().size(); i++) {
			ExamDST ex = tc.getExamsDST().get(i);
			if (ex.getDateCollected().before(res.getDateCollected())) res=ex;
		}
		return res;
	}
	
private String getLastDSTExam(ExamDST ex, String s){
	if (ex==null) return "";
	String res="";
	DstResult r = null;
	for (ExamDSTResult exr: ex.getResults()){
		if (exr.getSubstance().getAbbrevName().getName1().equals(s))
			r = exr.getResult();
	}

	if (r == null)	res = "NA";
	else
		switch (r){
		case BASELINE: res = "BL"; break;
		case CONTAMINATED: res = "C"; break;
		case NOTDONE: res = "NA";break;
		case RESISTANT: res = "R";break;
		case SUSCEPTIBLE: res = "S";break;
		}
	return res;
}

	/*private String getLastDSTExam(TbCase tc, String s){
		if (tc.getExamsDST() == null) return "";
		if (tc.getExamsDST().size() == 0) return "";
		String res = "";
		DstResult r = null;
		for (int i = 0; i < tc.getExamsDST().size(); i++) {
			ExamDST ex = tc.getExamsDST().get(i);
			for (ExamDSTResult exr: ex.getResults()){
				if (exr.getSubstance().getAbbrevName().getName1().equals(s))
					r = exr.getResult();
			}
		}
		if (r == null)	res = "NA";
		else
			switch (r){
			case BASELINE: res = "BL";
			case CONTAMINATED: res = "C";
			case NOTDONE: res = "NA";
			case RESISTANT: res = "R";
			case SUSCEPTIBLE: res = "S";
			}
		return res;
	}
*/
	/**
	 * @param tbcase
	 * add ExamCulture last result, first result, 6 month result
	 */
	
	protected void addCultureData(TbCase tbcase) {

		List<ExamCulture> cultureList=null;
		cultureList = tbcase.getExamsCulture();	
		if (cultureList.isEmpty()){
			for (int i=0;i<9;i++)			excel.addText("");
			return;
		}
		int ind=cultureList.size()-1;
		if (ind==0) {
			excel.addText("");
			excel.addText("");
			excel.addText("");
			excel.addValue(cultureList.get(0).getDateRelease()!=null?cultureList.get(0).getDateRelease():cultureList.get(0).getDateCollected());
			excel.addValue(cultureList.get(0).getResult());
			excel.addValue(cultureList.get(0).getMethod());
			
		} else {
			ExamCulture first=cultureList.get(0);
			for (int i = 1; i < cultureList.size(); i++) {
				ExamCulture x = cultureList.get(i);
				if (x.getDateCollected().before(first.getDateCollected()))
					first = x;
			}
			ExamCulture last=cultureList.get(0);
			for (int i = 1; i < cultureList.size(); i++) {
				ExamCulture x = cultureList.get(i);
				if (x.getDateCollected().after(last.getDateCollected()))
					last = x;
			}
			excel.addValue(last.getDateRelease()!=null?last.getDateRelease():last.getDateCollected());
			excel.addValue(last.getResult());
			excel.addValue(last.getMethod());
			excel.addValue(first.getDateRelease()!=null?first.getDateRelease():first.getDateCollected());
			excel.addValue(first.getResult());
			excel.addValue(first.getMethod());
		}
		ExamCulture exam6=null;
		if (treatmDate!=null){
			for (int i = 0; i < cultureList.size(); i++) {
				Date x = cultureList.get(i).getDateCollected();
				if (x.after(getStart6mntDay()) & x.before(getEnd6mntDay())) 
					exam6=cultureList.get(i);
			}
		}
		if (exam6!=null){
			excel.addValue(exam6.getDateRelease()!=null?exam6.getDateRelease():exam6.getDateCollected());
			excel.addValue(exam6.getResult());	
			excel.addValue(exam6.getMethod());
		} else{
			excel.addText("");
			excel.addText("");
			excel.addText("");
		}
	}
	/**
	 * @param tbcase
	 * add ExamMicroscopy last result+data, First Result+data , 6 month result+data
	 */
	
	protected void addMicroData(TbCase tbcase) {

		List<ExamMicroscopy> mList=null;
		mList = tbcase.getExamsMicroscopy();
		if (mList.isEmpty()){
			for (int i=0;i<6;i++)			excel.addText("");
			return;
		}
		int ind=mList.size()-1;
		if (ind==0) {
			excel.addValue(mList.get(0).getDateRelease()!=null?mList.get(0).getDateRelease():mList.get(0).getDateCollected());
			excel.addValue(mList.get(0).getResult());	
		
			excel.addText("");
			excel.addText("");
		
		}
		else
		{
			ExamMicroscopy first=mList.get(0);
			for (int i = 1; i < mList.size(); i++) {
				ExamMicroscopy x = mList.get(i);
				if (x.getDateCollected().before(first.getDateCollected()))
					first = x;
			}
			ExamMicroscopy last=mList.get(0);
			for (int i = 1; i < mList.size(); i++) {
				ExamMicroscopy x = mList.get(i);
				if (x.getDateCollected().after(last.getDateCollected()))
					last = x;
			}
			excel.addValue(last.getDateRelease()!=null?last.getDateRelease():last.getDateCollected());
			excel.addValue(last.getResult());
			excel.addValue(first.getDateRelease()!=null?first.getDateRelease():first.getDateCollected());
			excel.addValue(first.getResult());
		}
		//6 month 
		ExamMicroscopy exam6=null;
		if (treatmDate!=null){
			for (int i = 0; i < mList.size(); i++) {
				Date x = mList.get(i).getDateCollected();
				if (x.after(getStart6mntDay()) & x.before(getEnd6mntDay())) 
					exam6=mList.get(i);
			}
		}
		if (exam6!=null){
			excel.addValue(exam6.getDateRelease()!=null?exam6.getDateRelease():exam6.getDateCollected());
			excel.addValue(exam6.getResult());	
		} else{
			excel.addText("");
			excel.addText("");
		}
	}
	
	private Date getStart6mntDay(){
		Calendar st=Calendar.getInstance();
		st.setTime(treatmDate);
		 st.roll(Calendar.MONTH, 5);
		 st.roll(Calendar.DATE, 14);
		 return  st.getTime();
	}
	
	
	private Date getEnd6mntDay(){
		Calendar st=Calendar.getInstance();
		st.setTime(treatmDate);
		 st.roll(Calendar.MONTH, 6);
		 st.roll(Calendar.DATE, 14);
		 return  st.getTime();
	
	}
	
	public List<TbCaseAZ> getCasesAz() {
		if (cases == null)
			createCases();
		return cases;
	}
	@Override
	protected void createCases() {
		//setNewCasesOnly(true);
		cases = createQuery().getResultList();
	}
	
	@Override
	protected String getHQLFrom() {
		return "from TbCaseAZ c";
	}
	@Override
	protected String getHQLSelect() {
		return "select c";
	}
	
	@Override
	protected String getHQLJoin() {
		return "join fetch c.patient p";
	}
	
	@Override
	protected String getHQLWhere() {
		String w = super.getHQLWhere();
		w = w.replace("c.notificationUnit.workspace", "p.workspace");
		return w;
	}
	
	@Override
	public int getResultCount() {
		return getCasesAz().size();
	}
}
