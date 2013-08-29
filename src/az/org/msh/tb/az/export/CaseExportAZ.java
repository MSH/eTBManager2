package org.msh.tb.az.export;

 import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
		excel.addTextFromResource("excel.xray.presentation"+" 1st", "title");
		excel.addTextFromResource("excel.xray.localization", "title");
		excel.addTextFromResource("excel.xray.presentation"+" 2", "title");
		excel.addText("X-ray result (the last one) - Localization");
		excel.addTextFromResource("excel.dst.s", "title");
		excel.addTextFromResource("excel.dst.h", "title");
		excel.addTextFromResource("excel.dst.r", "title");
		excel.addTextFromResource("excel.dst.e", "title");
		excel.addTextFromResource("excel.dst.z", "title");
		excel.addTextFromResource("excel.dst.pto", "title");
		excel.addTextFromResource("excel.dst.ofx", "title");
		excel.addTextFromResource("excel.dst.cs", "title");
		excel.addTextFromResource("excel.dst.pas", "title");
		excel.addTextFromResource("excel.dst.cm", "title");
		excel.addTextFromResource("excel.dst.am", "title");
		excel.addTextFromResource("excel.dst.mfx", "title");
		excel.addTextFromResource("excel.dst.km", "title");
		excel.addTextFromResource("excel.dst.cfx", "title");
		excel.addTextFromResource("excel.dst.gati", "title");
		excel.addTextFromResource("excel.dst.rfb", "title");
		excel.addTextFromResource("excel.dst.eto", "title");
		excel.addTextFromResource("excel.dst.lfx", "title");
		excel.addTextFromResource("excel.dst.trd", "title");
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
		//culture
		excel.addColumnMark("culture");
		excel.addGroupHeaderFromResource("cases.examculture"+" 1", 2, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		excel.addTextFromResource("cases.details.result", "title");
		excel.addGroupHeaderFromResource("cases.examculture"+" 2", 2, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		excel.addTextFromResource("cases.details.result", "title");
		// microscopy
		excel.addColumnMark("microscopy");
		excel.addGroupHeaderFromResource("cases.exammicroscopy"+" 1"+" 1", 2, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		excel.addTextFromResource("cases.details.result", "title");
		excel.addGroupHeaderFromResource("cases.exammicroscopy"+" 2", 2, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		excel.addTextFromResource("cases.details.result", "title");
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
		AddXrayData (tbcase);
		excel.addText(getLastDSTExam(tbcase, "S"));
		excel.addText(getLastDSTExam(tbcase, "H"));
		excel.addText(getLastDSTExam(tbcase, "R"));
		excel.addText(getLastDSTExam(tbcase, "E"));
		excel.addText(getLastDSTExam(tbcase, "Z"));
		excel.addText(getLastDSTExam(tbcase, "Pto"));
		excel.addText(getLastDSTExam(tbcase, "Ofx"));
		excel.addText(getLastDSTExam(tbcase, "Cs"));
		excel.addText(getLastDSTExam(tbcase, "PAS"));
		excel.addText(getLastDSTExam(tbcase, "Cm"));
		excel.addText(getLastDSTExam(tbcase, "Am"));
		excel.addText(getLastDSTExam(tbcase, "Mfx"));
		excel.addText(getLastDSTExam(tbcase, "Km"));
		excel.addText(getLastDSTExam(tbcase, "Cfx"));
		excel.addText(getLastDSTExam(tbcase, "Gati"));
		excel.addText(getLastDSTExam(tbcase, "Rfb"));
		excel.addText(getLastDSTExam(tbcase, "Eto"));
		excel.addText(getLastDSTExam(tbcase, "Lfx"));
		excel.addText(getLastDSTExam(tbcase, "Trd"));

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
		addCultureData(tbcase);
		addMicroData(tbcase);
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
		if (tc.getResXRay() == null) return null;
		if (tc.getResXRay().size() == 0) return null;
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
	 */
	private void AddXrayData (TbCase tbcase){
		if (tbcase.getResXRay() == null) {
			excel.addText("");
			excel.addText("");
			excel.addText("");
			excel.addText("");
			return ;
		}
		if (tbcase.getResXRay().size() == 0) {
			excel.addText("");
			excel.addText("");
			excel.addText("");
			excel.addText("");
			return ;
		}
		ExamXRay x = getFirstXRayExam(tbcase);
		if (x!=null){
			ExamXRayAZ xaz = (ExamXRayAZ) getEntityManager().find(ExamXRayAZ.class, x.getId());
			excel.addText(xaz.getPresentation()!=null ? xaz.getPresentation().getName().getDefaultName() : "");
			excel.addText(xaz.getLocalization()!=null ? xaz.getLocalization().getName().getDefaultName() : "");
		}
		if (tbcase.getResXRay().size() > 1){
			 x = getLastXRayExam(tbcase);
				ExamXRayAZ xaz = (ExamXRayAZ) getEntityManager().find(ExamXRayAZ.class, x.getId());
				excel.addText(xaz.getPresentation()!=null ? xaz.getPresentation().getName().getDefaultName() : "");
				excel.addText(xaz.getLocalization()!=null ? xaz.getLocalization().getName().getDefaultName() : "");
		} else{
			excel.addText("");
			excel.addText("");
		}
	}
	
	/**
	 * Get last DST-result
	 * @param tc case
	 * @param s abbrevname of substance, ex. "H"
	 * */
	private String getLastDSTExam(TbCase tc, String s){
		if (tc.getExamsDST() == null) return "";
		if (tc.getExamsDST().size() == 0) return "";
		String res = "";
		DstResult r = null;
		List<ExamDST> lstsort = new ArrayList<ExamDST>();
		lstsort.addAll(tc.getExamsDST());
		Collections.sort(lstsort, new Comparator<ExamDST>() {

			@Override
			public int compare(ExamDST o1, ExamDST o2) {
				Date d1 = o1.getDateCollected();
				Date d2 = o2.getDateCollected();
				
				if (d1.after(d2))
					return 1;
				if (d1.before(d2))
					return -1;
				
				int r1 = o1.getNumResistant();
				int r2 = o2.getNumResistant();
				
				if (r1>r2)
					return -1;
				if (r1<r2)
					return 1;
				
				return 0;
			}

		});
		for (int i = 0; i < lstsort.size(); i++) {
			ExamDST ex = lstsort.get(i);
			for (ExamDSTResult exr: ex.getResults()){
				if (exr.getSubstance().getAbbrevName().getName1().equals(s))
					r = exr.getResult();
			}
		}
		if (r == null)	res = "NA";
		else
			switch (r){
				case BASELINE: res = "BL"; break;
				case CONTAMINATED: res = "C"; break;
				case NOTDONE: res = "NA"; break;
				case RESISTANT: res = "R"; break;
				case SUSCEPTIBLE: res = "S"; break;
			}
		return res;
	}

	/**
	 * @param tbcase
	 * add ExamCulture First Result and last result
	 */
	
	protected void addCultureData(TbCase tbcase) {

		List<ExamCulture> cultureList=null;
		cultureList = tbcase.getExamsCulture();	
		if (cultureList.isEmpty()){
			excel.addText("");
			excel.addText("");
			excel.addText("");
			excel.addText("");
			return;
		}
		int ind=cultureList.size()-1;
		if (ind==0) {
			excel.addValue(cultureList.get(0).getDateCollected());
			excel.addValue(cultureList.get(0).getResult());
			excel.addText("");
			excel.addText("");
			return;
		}
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
		excel.addValue(first.getDateCollected());
		excel.addValue(first.getResult());
		excel.addValue(last.getDateCollected());
		excel.addValue(last.getResult());
	}
	
	/**
	 * @param tbcase
	 * add ExamMicroscopy First Result and last result
	 */
	
	protected void addMicroData(TbCase tbcase) {

		List<ExamMicroscopy> mList=null;
		mList = tbcase.getExamsMicroscopy();
		if (mList.isEmpty()){
			excel.addText("");
			excel.addText("");
			excel.addText("");
			excel.addText("");
			return;
		}
		int ind=mList.size()-1;
		if (ind==0) {
			excel.addValue(mList.get(0).getDateCollected());
			excel.addValue(mList.get(0).getResult());
			excel.addText("");
			excel.addText("");
			return;
		}
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
		excel.addValue(first.getDateCollected());
		excel.addValue(first.getResult());
		excel.addValue(last.getDateCollected());
		excel.addValue(last.getResult());
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
