/**
 * 
 */
package org.msh.tb.importexport;

import java.util.ArrayList;
import java.util.List;

import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamSputumSmear;
import org.msh.mdrtb.entities.TbCase;

/**
 * Group rows by case
 * @author Ricardo Memoria
 *
 */
public class CaseRowGroup {
	
	public CaseRowGroup(CaseExportTable table) {
		super();
		this.table = table;
	}


	private CaseExportTable table;
	
	private TbCase tbcase;
	
	private Object data;
	
	private List<CaseRow> rows = new ArrayList<CaseRow>();

	
	/**
	 * @param examHIV
	 * @return
	 */
	public CaseRow addExamHIV(ExamHIV examHIV) {
		for (CaseRow row: rows) {
			if (row.getExamHIV() == null) {
				row.setExamHIV(examHIV);
				return row;
			}
		}

		CaseRow row = new CaseRow(this);
		row.setExamHIV(examHIV);
		row.setTbcase(tbcase);
		rows.add(row);
		table.getRows().add(row);
		return row;
	}


	/**
	 * @return
	 */
	public int getIniRow() {
		int index = table.getGroups().indexOf(this);
		if (index <= 0)
			 return 3;

		CaseRowGroup prevGrp = table.getGroups().get(index - 1);
		return prevGrp.getIniRow() + prevGrp.getRows().size();
	}
	
	/**
	 * @param examCulture
	 * @return
	 */
	public CaseRow addExamCulture(ExamCulture examCulture) {
		for (CaseRow row: rows) {
			if (row.getExamCulture() == null) {
				row.setExamCulture(examCulture);
				return row;
			}
		}
		
		CaseRow row = new CaseRow(this);
		row.setExamCulture(examCulture);
		row.setTbcase(tbcase);
		rows.add(row);
		table.getRows().add(row);
		return row;
	}


	/**
	 * @param examSputumSmear
	 * @return
	 */
	public CaseRow addExamSputumSmear(ExamSputumSmear examSputumSmear) {
		for (CaseRow row: rows) {
			if (row.getExamSputumSmear() == null) {
				row.setExamSputumSmear(examSputumSmear);
				return row;
			}
		}
		
		CaseRow row = new CaseRow(this);
		row.setExamSputumSmear(examSputumSmear);
		row.setTbcase(tbcase);
		rows.add(row);
		table.getRows().add(row);
		return row;
	}

	
	public TbCase getTbcase() {
		return tbcase;
	}
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}
	public List<CaseRow> getRows() {
		return rows;
	}


	public CaseExportTable getTable() {
		return table;
	}


	public void setTable(CaseExportTable table) {
		this.table = table;
	}


	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}
}