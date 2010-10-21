package org.msh.tb.importexport;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamMicroscopy;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.indicators.core.CaseHQLBase;
import org.msh.tb.indicators.core.IndicatorFilters;


/**
 * Handle export of cases to an Excel file
 * @author Ricardo Memoria
 *
 */
@Name("caseExport")
@Scope(ScopeType.CONVERSATION)
public class CaseExport extends CaseHQLBase {
	private static final long serialVersionUID = 7234788990898793962L;

	@In(create=true) InfoCountryLevels levelInfo;
	
	/**
	 * Selected group of information to be exported
	 */
	private ExportContent exportContent = ExportContent.CASEDATA;
	
	/**
	 * List of cases exported
	 */
	private List<Object> cases;

	
	private CaseExportTable table;
	
	/**
	 * Used inside the class to determine the on-going temporary operation to generate the data set to be exported 
	 */
	private ExportContent ongoingOperation;
	private String hqlFrom;
	private String hqlSelect;

	/**
	 * Creator of the Excel file 
	 */
	private ExcelCreator excel;
	private List<Object[]> hivList;
	private List<Object[]> cultureList;
	private List<Object[]> microscopyList;
	private DstExport dstExport;
	private List<Object[]> xrayList;

	
	/**
	 * Group of information to be exported
	 * @author Ricardo Memoria
	 *
	 */
	public enum ExportContent {
		CASEDATA,
		REGIMENS,
		EXAMS,
		MEDEXAM;
		
		public String getKey() {
			return getClass().getSimpleName().concat("." + name());
		}
	}
	static private final ExportContent contents[] = {
		ExportContent.CASEDATA, ExportContent.EXAMS
	};

	

	/**
	 * Create the excel file and send it to the client browser
	 */
	public void download() {
		Map<String, String> msgs = Messages.instance();

		excel = new ExcelCreator();
		excel.setFileName(msgs.get("cases"));
		excel.createWorkbook();
		excel.addSheet(msgs.get("cases"), 0);
		
		excel.setRow(2);

		addTitles();
		if (ExportContent.EXAMS.equals(exportContent))
			addExamsTitles();

		excel.setColumnsAutoside(0, excel.getColumn() - 1);

		hqlFrom = null;
		hqlSelect = null;
		// generate excel file content
		for (Object obj: getCases()) {
			addCase(obj);
			excel.setColIdent(0);
			
			TbCase tbcase = getTbCase(obj);

			if (ExportContent.EXAMS.equals(exportContent)) {
				ongoingOperation = ExportContent.EXAMS;
				addExams(tbcase);
			}
		}

		excel.sendResponse();
	}


	protected TbCase getTbCase(Object obj) {
		return (TbCase)obj;
	}
	
	/**
	 * Add titles of the excel file
	 */
	protected void addTitles() {
		// add title line
		excel.addTextFromResource("Patient.name", "title");
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
		excel.addTextFromResource("cases.details.notifhu", "title");
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


	/**
	 * Add exams titles 
	 */
	protected void addExamsTitles() {
		// HIV
		excel.addColumnMark("hiv");
		excel.addGroupHeaderFromResource("cases.examhiv", 2, "title");
		excel.addTextFromResource("cases.exams.dateRelease", "title");
		excel.addTextFromResource("cases.details.result", "title");
		// culture
		excel.addColumnMark("culture");
		excel.addGroupHeaderFromResource("cases.examculture", 2, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		excel.addTextFromResource("cases.details.result", "title");
		// microscopy
		excel.addColumnMark("microscopy");
		excel.addGroupHeaderFromResource("cases.exammicroscopy", 2, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		excel.addTextFromResource("cases.details.result", "title");
		
		if (dstExport == null)
			dstExport = new DstExport(this);
		ongoingOperation = ExportContent.EXAMS;
		dstExport.addTitles();
		ongoingOperation = null;

		// xray
		excel.addColumnMark("xray");
		excel.addGroupHeaderFromResource("cases.examxray", 3, "title");
		excel.addTextFromResource("cases.exams.dateRelease", "title");
		excel.addTextFromResource("TbField.XRAYPRESENTATION", "title");
		excel.addTextFromResource("XRayEvolution", "title");
	}


	/**
	 * Add cases to the excel file
	 */
	protected void addCase(Object obj) {
		TbCase tbcase = (TbCase)obj;

		// add content
		excel.lineBreak();
		
		excel.addText(tbcase.getPatient().getFullName());
		excel.addText(tbcase.getDisplayCaseNumber());
		excel.addTextFromResource(tbcase.getState().getKey());
		excel.addText(tbcase.getRegistrationCode());
		excel.addTextFromResource(tbcase.getPatient().getGender().getKey());
		excel.addDate(tbcase.getPatient().getBirthDate());
		excel.addNumber(tbcase.getAge());
		excel.addTextFromResource(tbcase.getNationality().getKey());
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



	protected void addExams(TbCase tbcase) {
		int currentRow = excel.getRow();
		
		addHivExam(tbcase);
		
		addCultureData(tbcase);
		
		excel.setRow(currentRow);
		addMicroscopyData(tbcase);
		
		excel.setRow(currentRow);
		addDstData(tbcase);
		
		excel.setRow(currentRow);
		addXRayData(tbcase);

		excel.setColIdent(0);
		excel.setRow(excel.getMaxRow());
	}

	/**
	 * Add culture data
	 * @param tbcase
	 */
	protected void addHivExam(TbCase tbcase) {
		if (hivList == null) {
			hqlFrom = "from ExamHIV exam join exam.tbcase c";
			hqlSelect = "select c.id, exam.date, exam.result";
			setOrderByFields("exam.tbcase.id, exam.date");
			hivList = createQuery().getResultList();			
		}
		addArrayValues(tbcase, hivList, "hiv");
	}


	/**
	 * Add culture data
	 * @param tbcase
	 */
	protected void addCultureData(TbCase tbcase) {
		if (cultureList == null) {
			hqlFrom = "from ExamCulture exam join exam.tbcase c";
			hqlSelect = "select c.id, exam.dateCollected, exam.result";
			setOrderByFields("exam.tbcase.id, exam.dateCollected");
			cultureList = createQuery().getResultList();			
		}
		addArrayValues(tbcase, cultureList, "culture");
	}
	
	protected void addMicroscopyData(TbCase tbcase) {
		if (microscopyList == null) {
			hqlFrom = "from ExamMicroscopy exam join exam.tbcase c";
			hqlSelect = "select c.id, exam.dateCollected, exam.result";
			setOrderByFields("exam.tbcase.id, exam.dateCollected");
			microscopyList = createQuery().getResultList();			
		}
		addArrayValues(tbcase, microscopyList, "microscopy");
	}
	
	protected void addXRayData(TbCase tbcase) {
		if (xrayList == null) {
			hqlFrom = "from ExamXRay exam join exam.tbcase c";
			hqlSelect = "select c.id, exam.date, exam.presentation.name, exam.evolution";
			setOrderByFields("exam.tbcase.id, exam.date");
			xrayList = createQuery().getResultList();			
		}
		addArrayValues(tbcase, xrayList, "xray");
	}


	protected void addDstData(TbCase tbcase) {
		dstExport.export(tbcase);
	}

	/**
	 * Add an array of values to the excel file. The first value of the array is the case id
	 * and is not included in the Excel file, 
	 * and just array of same id as the id of the tbcase parameters are included in the file.
	 * @param tbcase
	 * @param lst
	 */
	protected void addArrayValues(TbCase tbcase, List<Object[]> lst, String columnMark) {
		if (!excel.gotoMark(columnMark))
			return;
	
		excel.setColIdent(excel.getColumn());
		
		boolean bFirst = true;
		for (Object values[]: lst) {
			int id = (Integer)values[0];
			if (tbcase.getId().equals(id)) {
				// if it's first line, doesn't break it
				if (!bFirst)
					excel.lineBreak();
				else bFirst = false;

				for (int i = 1; i < values.length; i++) {
					excel.addValue(values[i]);
				}
			}
		}		
	}


	/**
	 * Return the list of cases based on the filters in {@link IndicatorFilters} session variable
	 * @return list of objects {@link TbCase}
	 */
	public List<Object> getCases() {
		if (cases == null)
			createCases();
		return cases;
	}

	
	/**
	 * Create the list of cases based on the filters in the {@link IndicatorFilters} session variable 
	 */
	protected void createCases() {
		setNewCasesOnly(true);
		cases = createQuery().getResultList();
	}


	public ExportContent getExportContent() {
		return exportContent;
	}


	public void setExportContent(ExportContent exportContent) {
		this.exportContent = exportContent;
	}

	public ExportContent[] getContents() {
		return contents;
	}


	@Override
	protected String getHQLJoin() {
		if (isGeneratingCaseData())
			return super.getHQLJoin().concat(" join fetch c.notificationUnit nu " +
				"join fetch nu.adminUnit " +
				"join fetch c.pulmonaryType");
		else return null;
	}

	@Override
	protected String getHQLSelect() {
		return (hqlSelect != null? hqlSelect: super.getHQLSelect());
	}

	@Override
	protected String getHQLFrom() {
		return (hqlFrom != null? hqlFrom: super.getHQLFrom());
	}

	protected TbCase getCaseFromResultset(Object obj) {
		return (TbCase)obj;
	}
	
	protected Object getDataFromResultset(Object obj) {
		return null;
	}

	
	protected void createTable() {
		table = new CaseExportTable();

		ongoingOperation = ExportContent.CASEDATA; 
		// create list of cases to be exported
		for (Object c: getCases()) {
			table.addTbcase(getCaseFromResultset(c), getDataFromResultset(c));
		}
		
		if (ExportContent.EXAMS.equals(exportContent)) {
			generateExamsData();
		}
		
		table.organize();
	}
	
	
	protected void generateExamsData() {
		ongoingOperation = ExportContent.EXAMS;
		hqlFrom = "from ExamHIV exam join fetch exam.tbcase c";
		List<ExamHIV> examsHIV = createQuery().getResultList();
		for (ExamHIV examHIV: examsHIV) {
			table.addExamHIV(examHIV.getTbcase().getId(), examHIV);
		}

		hqlFrom = "from ExamCulture exam join fetch exam.tbcase c";
		List<ExamCulture> examsCulture = createQuery().getResultList();
		for (ExamCulture examCulture: examsCulture) {
			table.addExamCulture(examCulture.getTbcase().getId(), examCulture);
		}

		hqlFrom = "from ExamMicroscopy exam join fetch exam.tbcase c";
		List<ExamMicroscopy> examsMicroscopy = createQuery().getResultList();
		for (ExamMicroscopy examMicroscopy: examsMicroscopy) {
			table.addExamMicroscopy(examMicroscopy.getTbcase().getId(), examMicroscopy);
		}
	}

	
	/**
	 * Check if it's the step to generating case data
	 * @return
	 */
	protected boolean isGeneratingCaseData() {
		return ((ongoingOperation == null) || (ongoingOperation.equals(ExportContent.CASEDATA)));
	}

	public CaseExportTable getTable() {
		if (table == null)
			createTable();
		return table;
	}

	protected ExportContent getOngoingOperation() {
		return ongoingOperation;
	}


	public String getHqlFrom() {
		return hqlFrom;
	}


	public void setHqlFrom(String hqlFrom) {
		this.hqlFrom = hqlFrom;
	}


	public String getHqlSelect() {
		return hqlSelect;
	}


	public void setHqlSelect(String hqlSelect) {
		this.hqlSelect = hqlSelect;
	}


	public ExcelCreator getExcel() {
		return excel;
	}
}
