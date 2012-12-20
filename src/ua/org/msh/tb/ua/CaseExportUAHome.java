package org.msh.tb.ua;

import java.util.Map;

import jxl.write.WritableCellFormat;
import jxl.write.WriteException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.export.CaseExportHome;
import org.msh.tb.export.CaseIterator;
import org.msh.tb.export.ExcelCreator;

@Name("caseExportUAHome")
public class CaseExportUAHome extends CaseExportHome {

	@In(create=true) FacesMessages facesMessages;

	private static final int MAX_ROWS = 4000;
	private static final int MAX = 12000;
	private static final long serialVersionUID = -1749061457663113197L;
	private CaseExportUA caseExportUA;
	private boolean excelYes = true;

	@Override
	protected CaseIterator getCaseIterator() {
		if (caseExportUA == null)
			caseExportUA = new CaseExportUA(getExcel());
		return caseExportUA;
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

		int num = caseExportUA.getCount();
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
	/**
	 * Write "to many rows" warning on the top of the list
	 */
	private void writeWarning() {
		WritableCellFormat format = excel.createCellFormat("warning");
		try {
			format.setBackground(jxl.format.Colour.ORANGE);
		} catch (WriteException e) {
			e.printStackTrace();
		}
		excel.addText(getMessages().get("export_warning_too_many"), 0, 0, "warning");

	}

}
