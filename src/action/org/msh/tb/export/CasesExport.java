package org.msh.tb.export;

import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.entities.TbCase;

@Name("casesExport")
public class CasesExport {
	
	@In(create=true) CasesQuery cases;
	private ExcelCreator excel;
	private InfoCountryLevels levelInfo;
	private List<TbCase> caseList;
	
	public CasesExport(){
		super();
		levelInfo = (InfoCountryLevels)Component.getInstance("levelInfo");
	}
	
	public void download() {
		generateContent();
		excel.sendResponse();
	}
	
	protected void generateContent() {
		Map<String, String> msgs = Messages.instance();
		int sheetNumber = 0;
		
		excel = new ExcelCreator();
		excel.setFileName(msgs.get("cases"));
		excel.createWorkbook();
		excel.addSheet(msgs.get("cases"), sheetNumber);
		
		excel.setRow(2);
		
		addTitles();

		excel.setColumnsAutoside(0, excel.getColumn() - 1);

		cases.setLoadCaseData(true);
		cases.setMaxResults(new Integer(999999999));
		
		// generate excel file content
		int qtdPerSheet = 1;
		for (CaseResultItem c : cases.getResultList()) {
			qtdPerSheet++;
			if(qtdPerSheet >= 65000){
				sheetNumber++;
				
				excel.addSheet(msgs.get("cases") + sheetNumber, sheetNumber);
				excel.setRow(2);
				addTitles();
				excel.setColumnsAutoside(0, excel.getColumn() - 1);
			}
			excel.lineBreak();
			exportContent(c.getTbcase());
		}
	}
	
	private void addTitles() {
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
	}
	
	private void exportContent(TbCase tbcase) {
		
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
		excel.addValue(tbcase, "notifAddress.zipCode");
		excel.addText(tbcase.getPhoneNumber());
		excel.addText(tbcase.getMobileNumber());
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
	}
}
