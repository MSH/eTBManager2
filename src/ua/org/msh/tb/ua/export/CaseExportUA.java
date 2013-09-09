package org.msh.tb.ua.export;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.TbCase;
import org.msh.tb.export.CaseExport;
import org.msh.tb.export.ExcelCreator;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.ua.entities.CaseDataUA;

@Name("caseExportUA")
public class CaseExportUA extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;

	public CaseExportUA(ExcelCreator excel) {
		super(excel);
	}


	private List<CaseDataUA> cases;

	/**
	 * Return the list of cases based on the filters in {@link IndicatorFilters} session variable
	 * @return list of objects {@link TbCase}
	 */
	public List<CaseDataUA> getCasesUA() {
		if (cases == null)
			createCases();
		return cases;
	}


	/**
	 * Create the list of cases based on the filters in the {@link IndicatorFilters} session variable 
	 */
	@Override
	protected void createCases() {
		setNewCasesOnly(true);
		cases = createQuery().getResultList();
	}

	@Override
	protected String getHQLFrom() {
		return "from CaseDataUA data join fetch data.tbcase c";
	}


	@Override
	protected String getHQLJoin() {
		return super.getHQLJoin().concat(" join fetch c.notificationUnit nu " +
				"join fetch nu.adminUnit " +
				"left join fetch c.pulmonaryType " +
		"left join fetch c.extrapulmonaryType");
	}

	@Override
	protected String getHQLValidationState() {
		return null;
	}

	public void addTitles() {
		ExcelCreator excel = getExcel();
		InfoCountryLevels levelInfo = getLevelInfo();

		// add title line
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
		excel.addTextFromResource("Address.zipCode", "title");
		excel.addTextFromResource("TbCase.phoneNumber", "title");
		excel.addTextFromResource("TbCase.mobileNumber", "title");
		excel.addTextFromResource("LocalityType", "title");
		excel.addTextFromResource("uk_UA.employerName", "title");
		excel.addTextFromResource("TbField.POSITION", "title");
		excel.addTextFromResource("TbCase.closestContact", "title");
		excel.addTextFromResource("TbCase.notificationUnit", "title");
		// notification health unit
		excel.addText(levelInfo.getNameLevel1().toString(), "title");
		excel.addTextFromResource("TbCase.diagnosisDate", "title");
		excel.addTextFromResource("DiagnosisType", "title");
		excel.addTextFromResource("TbCase.iniTreatmentDate", "title");
		excel.addTextFromResource("TbCase.endTreatmentDate", "title");

		excel.addTextFromResource("uk_UA.dateFirstSymptoms", "title");
		excel.addTextFromResource("uk_UA.dateFirstVisitGMC", "title");
		excel.addTextFromResource("TbCase.registrationDate", "title");
		excel.addTextFromResource("uk_UA.hospitalizationDate", "title");
		excel.addTextFromResource("PatientType", "title");
		excel.addTextFromResource("TbField.TBDETECTION", "title");
		excel.addTextFromResource("TbField.DIAG_CONFIRMATION", "title");
		excel.addTextFromResource("TbField.REGISTRATION_CATEGORY", "title");

		excel.addTextFromResource("DrugResistanceType", "title");
		excel.addTextFromResource("InfectionSite", "title");
		excel.addTextFromResource("TbField.PULMONARY_TYPES", "title");
		excel.addTextFromResource("TbField.EXTRAPULMONARY_TYPES", "title");
		excel.addTextFromResource("TbField.EXTRAPULMONARY_TYPES", "title");

		excel.addTextFromResource("uk_UA.pulmonaryDestruction", "title");
		excel.addTextFromResource("uk_UA.pulmonaryMBT", "title");
		excel.addTextFromResource("cases.examhiv.vct", "title");
		excel.addTextFromResource("global.date", "title");
		excel.addTextFromResource("uk_UA.alcoholAbuse", "title");
		excel.addTextFromResource("uk_UA.injectableDrugUse", "title");
		excel.addTextFromResource("TbCase.tbContact", "title");
		excel.addTextFromResource("uk_UA.otherFeature", "title");
		excel.addTextFromResource("uk_UA.homeless", "title");
		excel.addTextFromResource("uk_UA.unemployed", "title");
		excel.addTextFromResource("uk_UA.healthWorker", "title");
		excel.addTextFromResource("uk_UA.healthWorker.TB", "title");
		excel.addTextFromResource("uk_UA.healthWorker.GMC", "title");
		excel.addTextFromResource("uk_UA.migrant", "title");
		excel.addTextFromResource("uk_UA.refugee", "title");
		excel.addTextFromResource("uk_UA.prisioner", "title");
		excel.addTextFromResource("cases.exams.date", "title");
		excel.addTextFromResource("TbCase.outcomeDate", "title");
		excel.addTextFromResource("cases.examhiv.art", "title");
		excel.addTextFromResource("ExportContent.REGIMENS", "title");
		excel.addText("");
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.export.CaseIterator#exportContent(int)
	 */
	public TbCase exportContent(int index) {
		ExcelCreator excel = getExcel();
		InfoCountryLevels levelInfo = getLevelInfo();

		CaseDataUA data = getCasesUA().get(index);
		TbCase tbcase = data.getTbcase();

		excel.addText(tbcase.getPatient().getFullName());
		excel.addTextFromResource(tbcase.getClassification().getKey());
		excel.addText(tbcase.getDisplayCaseNumber());

		String s = getMessages().get( tbcase.getState().getKey() );
		if (data.getExtraOutcomeInfo() != null) {
			String extra = getMessages().get( data.getExtraOutcomeInfo().getKey() );
			if ((extra != null) && (!extra.isEmpty()))
				s += " - " + extra;
		}
		excel.addValue(s);
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
		excel.addValue(tbcase, "notifAddress.zipCode");
		excel.addText(tbcase.getPhoneNumber());
		excel.addText(tbcase.getMobileNumber());
		excel.addValue(tbcase, "notifAddress.localityType");
		excel.addText(data.getEmployerName());
		excel.addValue(data, "position");
		excel.addValue(data, "closestContact");
		excel.addValue(tbcase, "notificationUnit.name");
		excel.addValue(tbcase, "notificationUnit.adminUnit.parentLevel1");
		excel.addDate(tbcase.getDiagnosisDate());
		excel.addValue(tbcase, "diagnosisType");
		excel.addValue(tbcase, "treatmentPeriod.iniDate");
		excel.addValue(tbcase, "treatmentPeriod.endDate");
		excel.addValue(data.getDateFirstSymptoms());
		excel.addValue(data.getDateFirstVisitGMC());
		excel.addValue(tbcase.getRegistrationDate());
		excel.addValue(data.getHospitalizationDate());
		excel.addValue(tbcase, "patientType");
		excel.addValue(data, "detection");
		excel.addValue(data, "diagnosis");
		excel.addValue(data, "registrationCategory");
		excel.addValue(tbcase, "drugResistanceType");
		excel.addValue(tbcase, "infectionSite");
		excel.addValue(tbcase, "pulmonaryType");
		excel.addValue(tbcase, "extrapulmonaryType");
		excel.addValue(tbcase, "extrapulmonaryType2");

		excel.addValue(data, "pulmonaryDestruction");
		excel.addValue(data, "pulmonaryMBT");
		excel.addValue(translateYesNo( data.isVCTstarted() ));
		excel.addValue(data.getStartedVCTdate());
		excel.addValue(translateYesNo( data.isAlcoholAbuse() ));
		excel.addValue(translateYesNo( data.isInjectableDrugUse() ));
		excel.addValue(translateYesNo( data.isTbContact() ));
		excel.addValue(data.getOtherFeature());
		excel.addValue(translateYesNo( data.isHomeless() ));
		excel.addValue(translateYesNo( data.isUnemployed() ));
		excel.addValue(translateYesNo( data.isHealthWorker() ));
		excel.addValue(translateYesNo( data.isHealthWorkerTB() ));
		excel.addValue(translateYesNo( data.isHealthWorkerGMC() ));
		excel.addValue(translateYesNo( data.isMigrant() ));
		excel.addValue(translateYesNo( data.isRefugee() ));
		excel.addValue(translateYesNo( data.isPrisioner() ));
		excel.addDate(rightDSTTest(tbcase).getDateCollected());
		excel.addDate(rightDSTTest(tbcase).getDateRelease());
		Date artDate = null;
		for (ExamHIV ex:tbcase.getResHIV()){
			if (ex.getStartedARTdate() != null){
				artDate = ex.getStartedARTdate();
				break;
			}
		}
		excel.addDate(artDate);
		excel.addValue(tbcase,"regimen.name");
		return tbcase;
	}


	/**
	 * @return DST-test, which is up to quality, namely first month of treatment and worst test from several 
	 */

	private ExamDST rightDSTTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamDST> lst = new ArrayList<ExamDST>();
		for (ExamDST ex: tc.getExamsDST()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				if (tc.getDiagnosisDate() != null){
					dateIniTreat.setTime(tc.getDiagnosisDate());
				}else{
					dateIniTreat.setTime(tc.getRegistrationDate());
				}
			long testperiod = dateTest.getTimeInMillis()-dateIniTreat.getTimeInMillis();
			testperiod/=86400000;

			if (testperiod<=30) {
				res++;
				lst.add(ex);
			}	
		}

		switch (lst.size()) {
		case 0: 
			return new ExamDST();
		case 1: 
			return lst.get(0);
		default:
			return WorstRes(lst);
		}
	}
	/**
	 * @return worst DST-test from several 
	 */
	private ExamDST WorstRes(List<ExamDST> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamDST minEl = null;
		for (ExamDST el:lst){
			tmp = 0;
			tmp = el.getNumResistant();
			/*for (ExamDSTResult exr: el.getResults()){
				tmp += (exr.getResult().ordinal()==0 ? 4 : exr.getResult().ordinal());
			}*/
			if (tmp > max) {
				max = tmp; 
				minEl=el;
			}
		}
		return minEl;
	}


	protected String translateYesNo(boolean value) {
		if (value)
			return getMessages().get("global.yes");
		else return "-";
	}


	public int getResultCount() {
		return getCasesUA().size();
	}

	public int getCount() {
		String hql= "select count(*) "+ createHQL();
		//hql = hql.replace(getHQLJoin(), "");
		hql = hql.replace("fetch ", "");
		Query q = getEntityManager().createQuery(hql);
		setQueryParameters(q);
		return ((Long)q.getSingleResult()).intValue();
		//return getCasesUA().size();
	}
}
