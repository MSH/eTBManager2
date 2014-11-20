package org.msh.tb.export;

import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.indicators.core.CaseHQLBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Export DST results to an Excel file. All results of all cases will be loaded at once, but exported case by case
 * @author Ricardo Memoria
 *
 */
public class DstExport extends CaseHQLBase {
	private static final long serialVersionUID = -4038073908982284863L;

	private String hqlSelect;
	private String hqlFrom;

	private List<Object[]> dstList;
	private List<String> medicines;
	private TbCase tbcase;
	private ExcelCreator excel;


	public DstExport(ExcelCreator excelCreator) {
		super();
		excel = excelCreator;
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
		
		loadData();
		mountMedicineList();
	}
	
	/**
	 * Load DST results into memory
	 */
	protected void loadData() {
		hqlFrom = "from ExamDSTResult res join res.exam exam join exam.tbcase c";
		hqlSelect = "select c.id, exam.dateCollected, res.result, res.substance.abbrevName.name1, exam.id";
		setOrderByFields("exam.tbcase.id, exam.dateCollected");
		dstList = createQuery().getResultList();			
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
		Integer prevExamId = null; 
		excel.gotoMark("dst");
		excel.setColIdent(excel.getColumn());

		for (Object[] values: dstList) {
			Integer id = (Integer)values[0];
			
			// is case being looked for ?
			if (id.equals(caseid)) {
				isRead = true;
				
				Date dtCollected = (Date)values[1];
				Integer examId = (Integer)values[4];
				
				// first loop for the case or date collected has changed ?
				if ((prevExamId == null) || (!prevExamId.equals(examId))) {
					if (prevExamId != null)
						excel.lineBreak();
					excel.addDate(dtCollected);
					prevExamId = examId;
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

		excel.addColumnMark("dst");
		excel.addGroupHeaderFromResource("cases.examdst", medicines.size() + 1, "title");
		excel.addTextFromResource("PatientSample.dateCollected", "title");
		for (String medName: medicines) {
			excel.addText(medName, "title");
		}
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
