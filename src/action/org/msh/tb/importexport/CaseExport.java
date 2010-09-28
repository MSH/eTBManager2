package org.msh.tb.importexport;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamSputumSmear;
import org.msh.mdrtb.entities.TbCase;
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

	
	/**
	 * Selected group of information to be exported
	 */
	private ExportContent exportContent = ExportContent.CASEDATA;

	
	/**
	 * List of cases exported
	 */
	private List<TbCase> cases;

	
	private CaseExportTable table;
	
	/**
	 * Used inside the class to determine the on-going temporary operation to generate the data set to be exported 
	 */
	private ExportContent ongoingOperation;
	private String hqlFrom;


	
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
	 * Execute the download of the excel file
	 * @return page representing excel file
	 */
	@Begin(join=true)
	public String download() {
		return "download";
	}

	
	/**
	 * Return the list of cases based on the filters in {@link IndicatorFilters} session variable
	 * @return list of objects {@link TbCase}
	 */
	public List<TbCase> getCases() {
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
		else return super.getHQLJoin();
	}


	@Override
	protected String getHQLFrom() {
		if (ExportContent.EXAMS.equals(ongoingOperation)) {
			return hqlFrom;
		}
		else return super.getHQLFrom();
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

		hqlFrom = "from ExamCulture exam join fetch exam.sample s join fetch s.tbcase c";
		List<ExamCulture> examsCulture = createQuery().getResultList();
		for (ExamCulture examCulture: examsCulture) {
			table.addExamCulture(examCulture.getSample().getTbcase().getId(), examCulture);
		}

		hqlFrom = "from ExamSputumSmear exam join fetch exam.sample s join fetch s.tbcase c";
		List<ExamSputumSmear> examsSputumSmear = createQuery().getResultList();
		for (ExamSputumSmear examSputumSmear: examsSputumSmear) {
			table.addExamSputumSmear(examSputumSmear.getSample().getTbcase().getId(), examSputumSmear);
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
}
