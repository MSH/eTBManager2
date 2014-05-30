package org.msh.tb.export;

import java.util.List;

import org.msh.tb.entities.TbCase;
import org.msh.tb.indicators.core.CaseHQLBase;

public class ExamsExport extends CaseHQLBase {
	private static final long serialVersionUID = 3489784726746142880L;

	private ExcelCreator excel;

	private String hqlSelect;
	private String hqlFrom;

	private List<Object[]> hivList;
	private List<Object[]> cultureList;
	private List<Object[]> microscopyList;
	private DstExport dstExport;
	private List<Object[]> xrayList;

	
	public ExamsExport(ExcelCreator excel) {
		super();
		this.excel = excel;
	}

	public void addTitles() {
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
			dstExport = new DstExport(excel);
		dstExport.addTitles();

		// xray
		excel.addColumnMark("xray");
		excel.addGroupHeaderFromResource("cases.examxray", 3, "title");
		excel.addTextFromResource("cases.exams.dateRelease", "title");
		excel.addTextFromResource("TbField.XRAYPRESENTATION", "title");
		excel.addTextFromResource("XRayEvolution", "title");
	}
	
	/**
	 * Add the exams of a specific case
	 * @param tbcase
	 */
	public void addExams(TbCase tbcase) {
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
		excel.addArrayValues(tbcase, hivList, "hiv");
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
		excel.addArrayValues(tbcase, cultureList, "culture");
	}
	
	protected void addMicroscopyData(TbCase tbcase) {
		if (microscopyList == null) {
			hqlFrom = "from ExamMicroscopy exam join exam.tbcase c";
			hqlSelect = "select c.id, exam.dateCollected, exam.result";
			setOrderByFields("exam.tbcase.id, exam.dateCollected");
			microscopyList = createQuery().getResultList();			
		}
		excel.addArrayValues(tbcase, microscopyList, "microscopy");
	}
	
	protected void addXRayData(TbCase tbcase) {
		if (xrayList == null) {
			hqlFrom = "from ExamXRay exam join exam.tbcase c";
			hqlSelect = "select c.id, exam.date, exam.presentation.name, exam.evolution";
			setOrderByFields("exam.tbcase.id, exam.date");
			xrayList = createQuery().getResultList();			
		}
		excel.addArrayValues(tbcase, xrayList, "xray");
	}


	protected void addDstData(TbCase tbcase) {
		dstExport.export(tbcase);
	}

	
	@Override
	protected String getHQLJoin() {
		return null;
	}

	@Override
	protected String getHQLSelect() {
		return (hqlSelect != null? hqlSelect: super.getHQLSelect());
	}

	@Override
	protected String getHQLFrom() {
		return (hqlFrom != null? hqlFrom: super.getHQLFrom());
	}
}
