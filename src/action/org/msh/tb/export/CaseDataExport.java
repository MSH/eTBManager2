package org.msh.tb.export;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.utils.date.DateUtils;

/**
 * Handle export of cases to an Excel file, placing one case by row and displaying
 * the exams in 25 columns, one column for each treat month (max 25).
 * @author Mauricio Santos
 *
 */
@Name("caseDataExport")
public class CaseDataExport {
	@In(create=true) InfoCountryLevels levelInfo;
	@In(create=true) Map<String, String> messages;
	@In(create=true) Workspace defaultWorkspace;
	@In EntityManager entityManager;
	private UserTransaction transaction;
	
	private List<Object[]> casesData;
	
	private boolean exportExamHiv;
	private List<Object[]> examsHIV;
	
	private boolean exportExamMicroscopy;
	private List<Object[]> examsMicroscopy;	
	
	private boolean exportExamCulture;
	private List<Object[]> examsCulture;
	
	private boolean exportExamDST;
	private List<Object[]> examsDST;
	private List<Object[]> substances;
	
	private boolean exportExamXRay;
	private List<Object[]> examsXRay;
		
	private boolean exportCasesComorbidity;
	private List<Object[]> casesComorbidity;
	private List<Object[]> commorbidities;
	
	private boolean exportCaseSideEffect;
	private List<Object[]> caseSideEffect;
	private List<Object[]> sideEffects;
	
	private List<Object[]> numPrevTreat;
	private List<Object[]> inicialWeight;
	private List<Object[]> currentWeight;
	private List<Object[]> supervisedCases;
	private List<Object[]> firstMedExam;
	
	private String caseIds;
	
	protected ExcelCreator excel;
	
	private final static int MAX_CASES_PER_SHEET = 65500; // max: 65536 in excel 2003 or later
	private final static int TITLE_ROW = 1;

	/**
	 * Create the excel file and send it to the client browser
	 */
	public void download(List<Integer> casesId) {
		generateContent(casesId);
		excel.sendResponse();
	}

	private void concatenateIds(List<Integer> casesId){
		if(casesId == null || casesId.size() == 0){
			caseIds = null;
			return;
		}
		
		caseIds = "(";
		for(Integer caseId : casesId){
			caseIds += caseId;
			caseIds += ", ";
		}
		caseIds = caseIds.substring(0, caseIds.length()-2);
		caseIds += ")";
	}
	
	private void loadData(List<Integer> casesId){
		String q;
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		
		concatenateIds(casesId);
		
		//Closes possible opened transaction
		entityManager.flush();
		commitTransaction();
		entityManager.clear();
		
		q = "select c.id, p.recordNumber, c.caseNumber, p.securityNumber, p.name, p.motherName, c.classification, c.state, p.gender, p.birthDate, c.age, " +
				"c.nationality, c.currentAddress.address, c.currentAddress.complement, currAddAu, " +
				"c.currentAddress.zipCode, c.phoneNumber, c.mobileNumber, " +
				"notifunit.name, notifau, c.registrationDate, r.name, c.diagnosisDate, " +
				"c.treatmentPeriod.iniDate, c.treatmentPeriod.endDate, c.drugResistanceType, " +
				"c.infectionSite, pt.name.name1, ept.name.name1, ept2.name.name1, c.patientType " +
			"from TbCase c join c.patient p left join c.regimen r left join c.pulmonaryType pt left join c.extrapulmonaryType ept " +
				"left join c.extrapulmonaryType2 ept2 left join c.notificationUnit notifunit " +
				"left join c.notificationUnit.adminUnit notifau left join notifau.parent left join notifau.parent.parent left join notifau.parent.parent.parent left join notifau.parent.parent.parent.parent " +
				"left join c.currentAddress.adminUnit currAddAu left join currAddAu.parent left join currAddAu.parent.parent left join currAddAu.parent.parent.parent left join currAddAu.parent.parent.parent.parent " +
			"where c.id in " + caseIds;
		casesData = executeQuery(q, null);
		
		q = "select p.tbcase.id, count(p.tbcase.id) from PrevTBTreatment p where p.tbcase.id in " + caseIds + " group by p.tbcase.id";
		numPrevTreat = executeQuery(q, null);
		
		q = "select me.tbcase.id, me.weight "
			+ "from MedicalExamination me "
			+ "where me.tbcase.id in " + caseIds + " and me.date = (select min(me1.date) from MedicalExamination me1 where me1.tbcase.id = me.tbcase.id)";
		inicialWeight = executeQuery(q, null);
		
		q = "select me.tbcase.id, me.weight "
				+ "from MedicalExamination me "
				+ "where me.tbcase.id in " + caseIds + " and me.date = (select max(me1.date) from MedicalExamination me1 where me1.tbcase.id = me.tbcase.id)";
		currentWeight = executeQuery(q, null);
		
		q = "select me.tbcase.id, 0 from MedicalExamination me "
			+ "where me.tbcase.id in " + caseIds + " and me.supervisedTreatment = :yes "
			+ "and not exists (select me1.id from MedicalExamination me1 where me.supervisedTreatment = :no and me.tbcase.id = me1.tbcase.id)";
		parameters.put("yes", YesNoType.YES);
		parameters.put("no", YesNoType.NO);
		supervisedCases = executeQuery(q, parameters);
		parameters.clear();
		
		q = "select me.tbcase.id, min(me.date) from MedicalExamination me where me.tbcase.id in " + caseIds + " group by me.tbcase.id";
		firstMedExam = executeQuery(q, null);
		
		if(exportExamHiv){
			q = "select e.tbcase.id, e.date, e.result from ExamHIV e where e.tbcase.id in " + caseIds;
			examsHIV = executeQuery(q, null);
		}
		
		if(exportExamMicroscopy){
			q = "select e.tbcase.id, e.dateCollected, e.result from ExamMicroscopy e where e.tbcase.id in " + caseIds;
			examsMicroscopy = executeQuery(q, null);
		}
		
		if(exportExamCulture){
			q = "select e.tbcase.id, e.dateCollected, e.result from ExamCulture e where e.tbcase.id in " + caseIds;
			examsCulture = executeQuery(q, null);
		}

		if(exportExamDST){
			q = "select s.id, s.name.name1 from Substance s where s.dstResultForm = :true and s.workspace.id = :workspaceId";
			parameters.put("workspaceId", defaultWorkspace.getId());
			parameters.put("true", true);
			substances = executeQuery(q, parameters);
			parameters.clear();
			
			q = "select c.id, exam.dateCollected, res.result, res.substance.id"
					+ " from ExamDSTResult res join res.exam exam join exam.tbcase c"
					+ " where c.id in " + caseIds;
			examsDST = executeQuery(q, null);
		}
		
		if(exportExamXRay){
			q = "select e.tbcase.id, e.date, e.presentation.name.name1 from ExamXRay e where e.tbcase.id in " + caseIds;
			examsXRay = executeQuery(q, null);
		}
		
		if(exportCasesComorbidity){
			q = "select fv.id, fv.name.name1 from FieldValue fv where fv.field = :fieldType and fv.workspace.id = :workspaceId";
			parameters.put("fieldType", TbField.COMORBIDITY);
			parameters.put("workspaceId", defaultWorkspace.getId());
			commorbidities = executeQuery(q, parameters);
			parameters.clear();
			
			q = "select e.tbcase.id, e.comorbidity.id from CaseComorbidity e where e.tbcase.id in " + caseIds;
			casesComorbidity = executeQuery(q, null);
		}
		
		if(exportCaseSideEffect){
			q = "select fv.id, fv.name.name1 from FieldValue fv where fv.field = :fieldType and fv.workspace.id = :workspaceId";
			parameters.put("fieldType", TbField.SIDEEFFECT);
			parameters.put("workspaceId", defaultWorkspace.getId());
			sideEffects = executeQuery(q, parameters);
			parameters.clear();
			
			q = "select e.tbcase.id, e.sideEffect.value.id from CaseSideEffect e where e.tbcase.id in " + caseIds;
			caseSideEffect = executeQuery(q, null);
		}		
	}
	
	/**
	 * Generate the content of the excel file
	 */
	protected void generateContent(List<Integer> casesId) {
		int iSheets = 0;
		
		loadData(casesId);
		
		excel = new ExcelCreator();
		excel.setFileName(messages.get("cases"));
		excel.createWorkbook();
		
		excel.addSheet(messages.get("cases")+" - pg. " + iSheets+1, iSheets);
		addTitles();
		
		for(Object[] caseData : casesData){
			excel.lineBreak();
			exportData(caseData);
			
			if(excel.getRow() == MAX_CASES_PER_SHEET){
				iSheets += 1;
				excel.addSheet(messages.get("cases")+" - pg. " + iSheets+1, iSheets);
				addTitles();
			}
		}
		 
	}

	public void addTitles(){
		//set title row
		excel.setRow(TITLE_ROW);
		
		addCaseDataTitles();
		
		if(exportExamHiv)
			addExamTitles("hiv", "manag.hivreport.title1");
		
		if(exportExamMicroscopy)
			addExamTitles("microscopy", "cases.exammicroscopy");
		
		if(exportExamCulture)
			addExamTitles("culture", "cases.examculture");
		
		if(exportExamDST)
			addGeneralizedTittles(substances, "examdst", "cases.examdst");
		
		if(exportExamXRay)
			addExamTitles("xray", "cases.examxray");
		
		if(exportCasesComorbidity)
			addGeneralizedTittles(commorbidities, "commorbidities", "cases.comorbidities");
		
		if(exportCaseSideEffect)
			addGeneralizedTittles(sideEffects, "sideeffects", "cases.sideeffects.desc");
	}
	
	public void addCaseDataTitles() {
		// add title line
		excel.addColumnMark("casedata");
		excel.addGroupHeaderFromResource("cases.details.case", 32 + (levelInfo.getMaxLevel() * 2), "title");
		
		excel.addTextFromResource("Patient.caseNumber", "title");
		excel.addTextFromResource("Patient.securityNumber", "title");
		excel.addTextFromResource("Patient.name", "title");
		excel.addTextFromResource("Patient.motherName", "title");
		excel.addTextFromResource("CaseClassification", "title");
		excel.addTextFromResource("CaseState", "title");
		excel.addTextFromResource("Gender", "title");
		excel.addTextFromResource("Patient.birthDate", "title");
		excel.addTextFromResource("TbCase.age", "title");
		excel.addTextFromResource("Nationality", "title");
		excel.addText(messages.get("Address") + " (" + messages.get("cases.details.addrresscurr.abbrev") + ")", "title");
		excel.addText(messages.get("Address.complement") + " (" + messages.get("cases.details.addrresscurr.abbrev") + ")", "title");
		addAdminUnitTitle(messages.get("cases.details.addrresscurr.abbrev"));
		excel.addText(messages.get("Address.zipCode") + " (" + messages.get("cases.details.addrresscurr.abbrev") + ")", "title");
		excel.addTextFromResource("TbCase.phoneNumber", "title");
		excel.addTextFromResource("TbCase.mobileNumber", "title");
		excel.addTextFromResource("pt_BR.usOrigem", "title");
		addAdminUnitTitle(messages.get("TbCase.notificationUnit"));
		excel.addTextFromResource("TbCase.registrationDate", "title");
		excel.addTextFromResource("Regimen", "title");
		excel.addTextFromResource("TbCase.diagnosisDate", "title");
		excel.addTextFromResource("tbcase.firstMedExam", "title");
		excel.addTextFromResource("TbCase.iniTreatmentDate", "title");
		excel.addTextFromResource("TbCase.endTreatmentDate", "title");
		excel.addTextFromResource("DrugResistanceType", "title");
		excel.addTextFromResource("InfectionSite", "title");
		excel.addTextFromResource("TbField.PULMONARY_TYPES", "title");
		excel.addTextFromResource("TbField.EXTRAPULMONARY_TYPES", "title");
		excel.addTextFromResource("TbField.EXTRAPULMONARY_TYPES", "title");
		excel.addTextFromResource("PatientType", "title");
		excel.addTextFromResource("pt_BR.supervisedTreatment", "title");
		excel.addTextFromResource("case.inicialweight", "title");
		excel.addTextFromResource("case.currweight", "title");
		excel.addTextFromResource("cases.prevtreat.numprev", "title");
	}
	
	private void addExamTitles(String columnMark, String groupTitle){
		// add title line
		excel.addColumnMark(columnMark);
		excel.addGroupHeaderFromResource(groupTitle, TreatmentMonth.values().length, "title");
		
		for(TreatmentMonth t : TreatmentMonth.values()){
			if(t.equals(TreatmentMonth.DIGNOSIS))
				excel.addText(messages.get(t.getKey()), "title");
			else
				excel.addText(t.getMonth() + messages.get(t.getKey()), "title");
		}
	}
	
	/**
	 * @param list - Must have on the position 1 the name of the column and on the 
	 * position 0 the id of the respective object.
	 * @param columnMark
	 * @param groupTitle
	 */
	private void addGeneralizedTittles(List<Object[]> list, String columnMark, String groupTitle){
		// add title line
		excel.addColumnMark(columnMark);
		excel.addGroupHeaderFromResource(groupTitle, list.size(), "title");
		
		for(Object[] o : list){
			String s = (String) o[1];
			excel.addText(s, "title");
		}
	}
	
	/**
	 * Works only when wants to display all levels that workspace supports.
	 */
	private void addAdminUnitTitle(String witchAdmUnit){
		if (levelInfo.isHasLevel1()) 
			excel.addText(levelInfo.getNameLevel1().toString() + (witchAdmUnit!=null ? " (" + witchAdmUnit + ")" : "") , "title");
		if (levelInfo.isHasLevel2()) 
			excel.addText(levelInfo.getNameLevel2().toString() + (witchAdmUnit!=null ? " (" + witchAdmUnit + ")" : "") , "title");
		if (levelInfo.isHasLevel3()) 
			excel.addText(levelInfo.getNameLevel3().toString() + (witchAdmUnit!=null ? " (" + witchAdmUnit + ")" : "") , "title");
		if (levelInfo.isHasLevel5()) 
			excel.addText(levelInfo.getNameLevel5().toString() + (witchAdmUnit!=null ? " (" + witchAdmUnit + ")" : "") , "title");
		if (levelInfo.isHasLevel5()) 
			excel.addText(levelInfo.getNameLevel5().toString() + (witchAdmUnit!=null ? " (" + witchAdmUnit + ")" : "") , "title");
	}
	
	/**
	 * Works only when wants to display all levels that workspace supports.
	 */
	private void addAdminUnitsValue(AdministrativeUnit au){
		for(int i = 1; i < 6; i++){
			if (levelInfo.isHasLevel(i)){
				AdministrativeUnit au1 = (au != null ? au : null);
				AdministrativeUnit au2 = (au1 != null ? au1.getParent() : null);
				AdministrativeUnit au3 = (au2 != null ? au2.getParent() : null);
				AdministrativeUnit au4 = (au3 != null ? au3.getParent() : null);
				AdministrativeUnit au5 = (au4 != null ? au4.getParent() : null);
				
				if(au5 != null && au5.getLevel() == i)
					excel.addText(au5.getName().getName1());
				else if(au4 != null && au4.getLevel() == i)
						excel.addText(au4.getName().getName1());
				else if(au3 != null && au3.getLevel() == i)
						excel.addText(au3.getName().getName1());
				else if(au2 != null && au2.getLevel() == i)
						excel.addText(au2.getName().getName1());
				else if(au1 != null && au1.getLevel() == i)
						excel.addText(au1.getName().getName1());
				else
					excel.setColumn(excel.getColumn()+1);

			}
		}
	}
	
	/**Start to export the content**/
	
	private void exportData(Object[] caseData){
		exportCaseData(caseData);
		
		if(exportExamHiv)
			addExamHivContent(caseData);
		
		if(exportExamMicroscopy)
			addExamMicroscopyContent(caseData);
		
		if(exportExamCulture)
			addExamCultureContent(caseData);
		
		if(exportExamDST)
			addExamDSTContent(caseData);
		
		if(exportExamXRay)
			addExamXRayContent(caseData);
		
		if(exportCasesComorbidity)
			addCasesComorbidityContent(caseData);
		
		if(exportCaseSideEffect)
			addCaseSideEffectContent(caseData);
				
	}
	
	public void exportCaseData(Object[] caseData) {
		 
		excel.addText(parseCaseNumber(caseData));
		excel.addText(((String)caseData[3]));
		excel.addText(((String)caseData[4]));
		excel.addText(((String)caseData[5]));
		excel.addTextFromResource(((CaseClassification)caseData[6]).getKey());
		excel.addTextFromResource(((CaseState)caseData[7]).getKey());
		excel.addTextFromResource(((Gender)caseData[8]).getKey());
		excel.addDate(((Date)caseData[9]));
		excel.addNumber(((Integer)caseData[10]));
		excel.addValue(caseData, 11);
		excel.addText(((String)caseData[12]));
		excel.addText(((String)caseData[13]));
		addAdminUnitsValue((AdministrativeUnit) caseData[14]);
		excel.addValue(caseData, 15);
		excel.addValue(caseData, 16);
		excel.addValue(caseData, 17);		
		excel.addValue(caseData, 18);
		addAdminUnitsValue((AdministrativeUnit)caseData[19]);
		excel.addDate(((Date)caseData[20]));
		addRegimen(caseData);
		excel.addDate(((Date)caseData[22]));
		addCalculatedValue(caseData, firstMedExam); //don't use casedata position
		excel.addDate(getIniTreatmentDate(caseData));
		excel.addValue(caseData, 24);
		excel.addValue(caseData, 25);
		excel.addValue(caseData, 26);
		excel.addValue(caseData, 27);
		excel.addValue(caseData, 28);
		excel.addValue(caseData, 29);
		excel.addValue(caseData, 30);
		addSupervisedTreatment(caseData);
		addCalculatedValue(caseData, inicialWeight);
		addCalculatedValue(caseData, currentWeight);
		addCalculatedValue(caseData, numPrevTreat);
		
	}
	
	private String parseCaseNumber(Object[] caseData){
		Integer recordNumber = ((Integer) caseData[1]);
		Integer caseNumber = ((Integer) caseData[2]);
		String sRecordNumber = null;
		String sCaseNumber = null;
		
		if(recordNumber != null)
			sRecordNumber = Integer.toString(recordNumber);
		
		if(caseNumber != null && caseNumber.intValue() != 1)
			sCaseNumber = Integer.toString(caseNumber);
		
		if(sRecordNumber == null && sCaseNumber == null)
			return messages.get("cases.nonumber");
		
		return sCaseNumber == null ? sRecordNumber : sRecordNumber + "-" + sCaseNumber;
	}
	
	/**
	 * @param list - has to contain 2 values. 0 - caseIds, 1 - value to be displayed
	 * @param tbcase
	 */
	public void addCalculatedValue(Object[] caseData, List<Object[]> list){
		boolean isInList = false;
		Integer idcase = null;
		for(Object[] o : list){
			idcase = (Integer) o[0];
			if(idcase.equals((Integer) caseData[0])){
				String value = "";
				
				if(o[1] instanceof Long){
					value = Long.toString((Long) o[1]);
					excel.addText(value);
				}else if(o[1] instanceof Double){
					value = Double.toString((Double) o[1]);
					excel.addText(value);
				}else if(o[1] instanceof String){
					value = (String) o[1];
					excel.addText(value);
				}else if(o[1] instanceof Date){
					excel.addDate((Date) o[1]);
				}
				
				isInList = true;
				break;
			}
		}
		if(!isInList)
			excel.setColumn(excel.getColumn()+1);
		return;
	}
	
	private Date getIniTreatmentDate(Object[] caseData){
		return (Date) caseData[23];
	}
	
	public void addRegimen(Object[] caseData){
		if(caseData[21]!=null)
			excel.addText(((String)caseData[21]));
		else
			excel.addTextFromResource("regimens.individualized");
	}
	
	public void addSupervisedTreatment(Object[] caseData){
		boolean isInList = false;
		Integer idcase = null;
		for(Object[] o : supervisedCases){
			idcase = (Integer) o[0];
			if(idcase.equals((Integer)caseData[0])){
				excel.addTextFromResource(YesNoType.YES.getKey());
				isInList = true;
				break;
			}
		}
		if(!isInList)
			excel.addTextFromResource(YesNoType.NO.getKey());
		return;
	}
	
	public void addExamHivContent(Object[] caseData){
		if(examsHIV == null)
			return;
		
		boolean hasResult;
		
		for(TreatmentMonth t : TreatmentMonth.values()){
			hasResult = false;
			for(Object[] exam : examsHIV){
				Integer caseId = (Integer) exam[0];
				Date dateCollected = (Date) exam[1];
				HIVResult result = (HIVResult) exam[2];
				
				if(caseId.equals((Integer)caseData[0]) && dateCollected != null
						&& getMonthTreatment(dateCollected, getIniTreatmentDate(caseData)) == t.getMonth()){
					excel.addTextFromResource(result.getKey());
					hasResult = true;
					break;
				}
			}
			if(!hasResult)
				excel.setColumn(excel.getColumn()+1);
		}
	}
	
	public void addExamMicroscopyContent(Object[] caseData){
		if(examsMicroscopy == null)
			return;
		
		boolean hasResult;
		
		for(TreatmentMonth t : TreatmentMonth.values()){
			hasResult = false;
			for(Object[] exam : examsMicroscopy){
				Integer caseId = (Integer) exam[0];
				Date dateCollected = (Date) exam[1];
				MicroscopyResult result = (MicroscopyResult) exam[2];
				
				if(caseId.equals((Integer)caseData[0]) && dateCollected != null
						&& getMonthTreatment(dateCollected, getIniTreatmentDate(caseData)) == t.getMonth()){
					excel.addTextFromResource(result.getKey());
					hasResult = true;
					break;
				}
			}
			if(!hasResult)
				excel.setColumn(excel.getColumn()+1);
		}
	}

	public void addExamCultureContent(Object[] caseData){
		if(examsCulture == null)
			return;
		
		boolean hasResult;
		
		for(TreatmentMonth t : TreatmentMonth.values()){
			hasResult = false;
			for(Object[] exam : examsCulture){
				Integer caseId = (Integer) exam[0];
				Date dateCollected = (Date) exam[1];
				CultureResult result = (CultureResult) exam[2];
				
				if(caseId.equals((Integer) caseData[0]) && dateCollected != null
						&& getMonthTreatment(dateCollected, getIniTreatmentDate(caseData)) == t.getMonth()){
					excel.addTextFromResource(result.getKey());
					hasResult = true;
					break;
				}
			}
			if(!hasResult)
				excel.setColumn(excel.getColumn()+1);
		}
	}
	
	public void addExamDSTContent(Object[] caseData){
		if(examsDST == null)
			return;
		
		boolean hasResult;
		
		for(Object[] s : substances){
			Integer substanceId = (Integer) s[0];
			hasResult = false;
			for(Object[] exam : examsDST){
				Integer caseId = (Integer) exam[0];
				Date dateCollected = (Date) exam[1];
				DstResult result = (DstResult) exam[2];
				Integer caseSubstanceId = (Integer) exam[3];
				
				if(caseId.equals((Integer) caseData[0]) && dateCollected != null
						&& getMonthTreatment(dateCollected, getIniTreatmentDate(caseData)) == TreatmentMonth.DIGNOSIS.getMonth()
						&& caseSubstanceId.equals(substanceId)){
					excel.addTextFromResource(result.getKey());
					hasResult = true;
					break;
				}
			}
			if(!hasResult)
				excel.setColumn(excel.getColumn()+1);
		}
	}
	
	public void addExamXRayContent(Object[] caseData){
		if(examsXRay == null)
			return;
		
		boolean hasResult;
		
		for(TreatmentMonth t : TreatmentMonth.values()){
			hasResult = false;
			for(Object[] exam : examsXRay){
				Integer caseId = (Integer) exam[0];
				Date dateCollected = (Date) exam[1];
				String presentation = (String) exam[2];
				
				if(caseId.equals((Integer) caseData[0]) && dateCollected != null
						&& getMonthTreatment(dateCollected, getIniTreatmentDate(caseData)) == t.getMonth()){
					excel.addTextFromResource(presentation);
					hasResult = true;
					break;
				}
			}
			if(!hasResult)
				excel.setColumn(excel.getColumn()+1);
		}
	}
	
	public void addCasesComorbidityContent(Object[] caseData){
		if(casesComorbidity == null)
			return;
		boolean hasCommorbidity;
		for(Object[] com : commorbidities){
			hasCommorbidity = false;
			Integer commorbidityId = (Integer) com[0];
			for(Object[] caseCom : casesComorbidity){
				Integer caseId = (Integer) caseCom[0];
				Integer commorbidityId2 = (Integer) caseCom[1];
				
				if(caseId.equals((Integer) caseData[0]) && commorbidityId2.equals(commorbidityId)){
					excel.addTextFromResource("global.yes");
					hasCommorbidity = true;
					break;
				}
			}
			if(!hasCommorbidity)
				excel.addTextFromResource("global.no");
		}
	}
	
	public void addCaseSideEffectContent(Object[] caseData){
		if(caseSideEffect == null)
			return;
		
		boolean hasSideEffect;
		
		for(Object[] com : sideEffects){
			hasSideEffect = false;
			Integer sideEffectId = (Integer) com[0];
			for(Object[] caseSE : caseSideEffect){
				Integer caseId = (Integer) caseSE[0];
				Integer sideEffectId2 = (Integer) caseSE[1];
				
				if(caseId.equals((Integer) caseData[0]) && sideEffectId2.equals(sideEffectId)){
					excel.addTextFromResource("global.yes");
					hasSideEffect = true;
					break;
				}
			}
			if(!hasSideEffect)
				excel.addTextFromResource("global.no");
		}
	}
	
	/** Getters 'n' setters **/
	public ExcelCreator getExcel() {
		return excel;
	}

	public boolean isExportExamHiv() {
		return exportExamHiv;
	}

	public void setExportExamHiv(boolean exportExamHiv) {
		this.exportExamHiv = exportExamHiv;
	}

	public boolean isExportExamMicroscopy() {
		return exportExamMicroscopy;
	}

	public void setExportExamMicroscopy(boolean exportExamMicroscopy) {
		this.exportExamMicroscopy = exportExamMicroscopy;
	}

	public boolean isExportExamCulture() {
		return exportExamCulture;
	}

	public void setExportExamCulture(boolean exportExamCulture) {
		this.exportExamCulture = exportExamCulture;
	}

	public boolean isExportExamDST() {
		return exportExamDST;
	}

	public void setExportExamDST(boolean exportExamDST) {
		this.exportExamDST = exportExamDST;
	}

	public boolean isExportExamXRay() {
		return exportExamXRay;
	}

	public void setExportExamXRay(boolean exportExamXRay) {
		this.exportExamXRay = exportExamXRay;
	}

	public boolean isExportCasesComorbidity() {
		return exportCasesComorbidity;
	}

	public void setExportCasesComorbidity(boolean exportCasesComorbidity) {
		this.exportCasesComorbidity = exportCasesComorbidity;
	}

	public boolean isExportCaseSideEffect() {
		return exportCaseSideEffect;
	}

	public void setExportCaseSideEffect(boolean exportCaseSideEffect) {
		this.exportCaseSideEffect = exportCaseSideEffect;
	}	
	public List<Object[]> getNumPrevTreat() {
		return numPrevTreat;
	}

	public void setNumPrevTreat(List<Object[]> numPrevTreat) {
		this.numPrevTreat = numPrevTreat;
	}
	
	/**
	 * Return number of month of treatment based on the date and IniTreatDate
	 * Based On tbcase.getMonthTreatment(Date date)
	 * @param date
	 * @param iniTreatmentDate
	 * @return
	 */
	public int getMonthTreatment(Date date, Date iniTreatmentDate) {
		if ((iniTreatmentDate == null) || (date.before(iniTreatmentDate)))
			return -1;

		int num = DateUtils.monthsBetween(date, iniTreatmentDate) + 1;

		return num;
	}
	
	public List<Object[]> executeQuery(String q, HashMap<String, Object> parameters){
		List<Object[]> r;
		Query query;
		
		beginTransaction();
		try {
				query = entityManager.createQuery(q);
				
				if(parameters != null){
					Iterator it = parameters.entrySet().iterator();
					while (it.hasNext()) {
						Entry pairs = (Map.Entry)it.next();
						query.setParameter((String)pairs.getKey(), pairs.getValue());
						it.remove(); // avoids a ConcurrentModificationException
					}
				}
				
				r = query.getResultList();
				
				entityManager.flush();
				commitTransaction();
				entityManager.clear();

		}
		catch (Exception e) {
			rollbackTransaction();
			throw new RuntimeException(e);
		}
		
		return r;
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

	/**
	 * Start a new transaction
	 */
	public void beginTransaction() {
		try {
			getTransaction().begin();
			entityManager.joinTransaction();
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
	 * Roll back a transaction that is under progress 
	 */
	public void rollbackTransaction() {
		try {
			getTransaction().rollback();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Default number of Treatment Months to display on the excel file
	 * @author Mauricio Santos	
	 *
	 */
	public enum TreatmentMonth {
		DIGNOSIS(-1),
		MONTH_1(1),
		MONTH_2(2),
		MONTH_3(3),
		MONTH_4(4),
		MONTH_5(5),
		MONTH_6(6),
		MONTH_7(7),
		MONTH_8(8),
		MONTH_9(9),
		MONTH_10(10),
		MONTH_11(11),
		MONTH_12(12),
		MONTH_13(13),
		MONTH_14(14),
		MONTH_15(15),
		MONTH_16(16),
		MONTH_17(17),
		MONTH_18(18),
		MONTH_19(19),
		MONTH_20(20),
		MONTH_21(21),
		MONTH_22(22),
		MONTH_23(23),
		MONTH_25(24);
		
		TreatmentMonth(int month){
			this.month = month;
		}
		
		int month;
		
		public String getKey() {
			if(ordinal() == 0)
				return "cases.exams.prevdt";
			else
				return "global.monthth.2";
		}
		
		public int getMonth() {
			return month;
		}
	};
		
}
