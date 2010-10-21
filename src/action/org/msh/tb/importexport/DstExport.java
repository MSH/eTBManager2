package org.msh.tb.importexport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.DstResult;

/**
 * Export DST results to an Excel file. All results of all cases will be loaded at once, but exported case by case
 * @author Ricardo Memoria
 *
 */
public class DstExport {

	private List<Object[]> dstList;
	private List<String> medicines;
	private TbCase tbcase;
	private CaseExport caseExport;
	private ExcelCreator excel;


	public DstExport(CaseExport caseExport) {
		super();
		this.caseExport = caseExport;
	}


	/**
	 * Export DST results of an specific case to an Excel file
	 * @param caseExport
	 * @param tbcase
	 */
	public void export(TbCase tbcase) {
		this.tbcase = tbcase;
		exportCaseResults();
	}

	protected void initialize() {
		if (dstList != null)
			return;
		
		excel = caseExport.getExcel();
		loadData();
		mountMedicineList();
	}
	
	/**
	 * Load DST results into memory
	 */
	protected void loadData() {
		caseExport.setHqlFrom("from ExamDSTResult res join res.exam exam join exam.tbcase c");
		caseExport.setHqlSelect("select c.id, exam.dateCollected, res.result, res.substance.abbrevName.name1");
		caseExport.setOrderByFields("exam.tbcase.id, exam.dateCollected");
		dstList = caseExport.createQuery().getResultList();			
	}


	/**
	 * Mount list of medicines that has DST results
	 */
	protected void mountMedicineList() {
		medicines = new ArrayList<String>();
		
		for (Object[] values: dstList) {
			String medName = (String)values[3];
			if (!medicines.contains(medName))
				medicines.add(medName);
		}
	}
	
	/**
	 * Export case results
	 */
	protected void exportCaseResults() {
		int caseid = tbcase.getId();
		boolean isRead = false;
		Date dt = null;
		excel.gotoMark("dst");

		for (Object[] values: dstList) {
			Integer id = (Integer)values[0];
			
			// is case being looked for ?
			if (id.equals(caseid)) {
				isRead = true;
				
				Date dtCollected = (Date)values[1];
				
				// first loop for the case or date collected has changed ?
				if ((dt == null) || (!dt.equals(dtCollected))) {
					if (dt != null)
						excel.lineBreak();
					excel.addDate(dtCollected);
					dt = dtCollected;
				}
				addResult(values);
			}
			else {
				if (isRead)
					break;
			}
		}
	}

	
	protected void addResult(Object[] values) {
		DstResult result = (DstResult)values[2];
		String medName = (String)values[3];
		
		int index = medicines.indexOf(medName);
		String txt = Messages.instance().get(result.getKey());
		
		excel.addText(txt, excel.getColumn() + index, excel.getRow(), null);
	}


	/**
	 * Add the titles of the excel file
	 */
	public void addTitles() {
		initialize();

		ExcelCreator excel = caseExport.getExcel();
		excel.addColumnMark("dst");
		excel.addGroupHeaderFromResource("cases.examdst", medicines.size() + 1, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		for (String medName: medicines) {
			excel.addText(medName, "title");
		}
	}
	
}
