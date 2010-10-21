package org.msh.tb.export;

import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.indicators.core.CaseHQLBase;


/**
 * Handle export of cases to an Excel file
 * @author Ricardo Memoria
 *
 */
@Name("caseExportHome")
public class CaseExportHome extends CaseHQLBase {
	private static final long serialVersionUID = 7234788990898793962L;

	@In(create=true) InfoCountryLevels levelInfo;
	
	/**
	 * Selected group of information to be exported
	 */
	private ExportContent exportContent = ExportContent.CASEDATA;

	
	/**
	 * Creator of the Excel file 
	 */
	private ExcelCreator excel;
	
	private CaseIterator caseIterator;
	
	private ExamsExport examsExport;

	
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
		generateContent();
		excel.sendResponse();
	}

	
	/**
	 * Generate the content of the excel file
	 */
	protected void generateContent() {
		Map<String, String> msgs = Messages.instance();

		excel = new ExcelCreator();
		excel.setFileName(msgs.get("cases"));
		excel.createWorkbook();
		excel.addSheet(msgs.get("cases"), 0);
		
		excel.setRow(2);

		// get reference to case iterator
		CaseIterator ci = getCaseIterator();

		boolean bExams = ExportContent.EXAMS.equals(exportContent); 
		
		ci.addTitles();
		if (bExams)
			getExamsExport().addTitles();

		excel.setColumnsAutoside(0, excel.getColumn() - 1);

		// generate excel file content
		
		int num = ci.getResultCount();
		
		for (int i = 0; i < num; i++) {
			excel.lineBreak();
			TbCase tbcase = ci.exportContent(i);
			if (bExams)
				getExamsExport().addExams(tbcase);
		}
	}
	
	/**
	 * Return reference to a {@link CaseIterator} interface, used to export the cases in a non dependency of the {@link TbCase} class.
	 * This way it'll be easier to implement it to specific workspaces with specific case data
	 * @return
	 */
	protected CaseIterator getCaseIterator() {
		if (caseIterator == null)
			caseIterator = new CaseExport(excel);
		return caseIterator;
	}

	protected ExamsExport getExamsExport() {
		if (examsExport == null)
			examsExport = new ExamsExport(excel);
		return examsExport;
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

	public ExcelCreator getExcel() {
		return excel;
	}
}
