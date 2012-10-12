package org.msh.tb.az;

 import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.az.entities.ExamXRayAZ;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.CaseDispensing;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.export.CaseExport;
import org.msh.tb.export.ExcelCreator;
import org.msh.utils.date.DateUtils;

@Name("caseExportAZ")
public class CaseExportAZ extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;

	private List<TbCaseAZ> cases;
	
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
		ExcelCreator excel = getExcel();
		InfoCountryLevels levelInfo = getLevelInfo();
		
		excel.addTextFromResource("Patient.name", "title");
		excel.addTextFromResource("CaseClassification", "title");
		excel.addTextFromResource("Patient.caseNumber", "title");
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
		excel.addTextFromResource("TbCase.notificationUnit", "title");
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
		excel.addTextFromResource("excel.dateInEIDSS", "title");
		excel.addText(getMessages().get("User.name")+" "+getMessages().get("Patient.lastName")+" "+getMessages().get("Patient.middleName")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("az_EIDSS_Notify_LPU")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("az_EIDSS_Address")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addTextFromResource("excel.dateRegAfterEIDSS", "title");
		excel.addText(getMessages().get("az_EIDSS_Age")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addText(getMessages().get("Patient.birthDate")+" "+getMessages().get("excel.fromEIDSS"), "title");
		//excel.addTextFromResource("TbCase.eidssid", "title");
		excel.addText(getMessages().get("az_AZ.case.unicalID")+" "+getMessages().get("excel.fromEIDSS"), "title");
		excel.addTextFromResource("excel.xray.presentation", "title");
		excel.addTextFromResource("excel.xray.localization", "title");
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
	}
	
	@Override
	public TbCase exportContent(int index) {
		ExcelCreator excel = getExcel();
		InfoCountryLevels levelInfo = getLevelInfo();
		TbCaseAZ tbcase = getCasesAz().get(index);
		
		excel.addText(tbcase.getPatient().getFullName());
		excel.addTextFromResource(tbcase.getClassification().getKey());
		excel.addText(tbcase.getDisplayCaseNumber());
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
		else
			excel.addText("");
		
		excel.addText(ei.length>=4 ? ei[3] : ""); // name from EIDSS
		excel.addText(ei[0]);
		excel.addText(ei[1]);
		excel.addText(""); //TODO дата приемки ТБ учреждением
		//excel.addDate(tbcase.getRegistrationDate());
		
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
		excel.addText(""); //TODO unicID
		ExamXRay x = getFirstXRayExam(tbcase);
		
		if (x!=null){
			ExamXRayAZ xaz = (ExamXRayAZ) getEntityManager().find(ExamXRayAZ.class, x.getId());
			excel.addText(xaz.getPresentation()!=null ? xaz.getPresentation().getName().getDefaultName() : "");
			excel.addText(xaz.getLocalization()!=null ? xaz.getLocalization().getName().getDefaultName() : "");
		}
		else{
			excel.addText("");
			excel.addText("");
		}
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
	
	public List<TbCaseAZ> getCasesAz() {
		if (cases == null)
			createCases();
		return cases;
	}
	@Override
	protected void createCases() {
		setNewCasesOnly(true);
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
	public int getResultCount() {
		return getCasesAz().size();
	}
}
