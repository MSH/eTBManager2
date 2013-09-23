package org.msh.tb.az.export;

import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.export.CaseExportHome;
import org.msh.tb.export.CaseIterator;
import org.msh.tb.export.ExcelCreator;
import org.msh.tb.export.CaseExportHome.ExportContent;

@Name("caseExportAZHome")
public class CaseExportAZHome extends CaseExportHome {
	private static final long serialVersionUID = -1749061457663113197L;
	
	private static final int MAX = 2500;
	private boolean excelYes = true;
	private CaseExportAZ caseExportAZ;
	@In(create=true) FacesMessages facesMessages;
	@Override
	protected CaseIterator getCaseIterator() {
		if (caseExportAZ == null)
			caseExportAZ = new CaseExportAZ(getExcel());
		return caseExportAZ;
	}
	/**
	 * Create the excel file and send it to the client browser
	 */
	@Override
	public void download() {
		generateContent();
		if (excelYes)
			excel.sendResponse();
	}
	/**
	 * Generate the content of the excel file
	 */
	@Override
	protected void generateContent() {
		Map<String, String> msgs = Messages.instance();

		excel = new ExcelCreator();
		excel.setFileName(msgs.get("cases"));
		excel.createWorkbook();
		excel.addSheet(msgs.get("cases"), 0);
		excel.setRow(2);		
		boolean bExams = ExportContent.EXAMS.equals(exportContent);

		// get reference to case iterator
		CaseIterator ci = getCaseIterator();

		int num = caseExportAZ.getResultCount();
		if (num<=MAX){
			ci.addTitles();
			if (bExams)
				getExamsExport().addTitles();

			excel.setColumnsAutoside(0, excel.getColumn() - 1);

			num = ci.getResultCount();
			/*if (num > MAX_ROWS){
					num=MAX_ROWS;
					writeWarning();
				}*/
			for (int i = 0; i < num; i++) {
				excel.lineBreak();
				TbCase tbcase = ci.exportContent(i);
				if (bExams)
					getExamsExport().addExams(tbcase);
			}
		}
		else{
			facesMessages.addFromResourceBundle("export_warning_too_many");
			excelYes = false;
		}
	}
}
