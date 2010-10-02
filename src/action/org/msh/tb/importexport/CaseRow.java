package org.msh.tb.importexport;

import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamMicroscopy;
import org.msh.mdrtb.entities.ExamXRay;
import org.msh.mdrtb.entities.TbCase;

public class CaseRow {

	private TbCase tbcase;
	
	private Object data;
	
	private ExamMicroscopy examMicroscopy;
	
	private ExamCulture examCulture;
	
	private ExamHIV examHIV;
	
	private ExamXRay examXRay;

	private CaseRowGroup group;
	
	
	public CaseRow(CaseRowGroup group) {
		super();
		this.group = group;
	}

	
	/**
	 * Check if row is the same patient as the row before
	 * @return true if row is the same case as before
	 */
	public boolean isDuplicated() {
		CaseExportTable tbl = group.getTable();
		int index = tbl.getRows().indexOf(this);
		
		if (index <= 0)
			return false;
		
		return (tbl.getRows().get(index - 1).getGroup() == tbl.getRows().get(index).getGroup());
	}
	
	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	public ExamMicroscopy getExamMicroscopy() {
		return examMicroscopy;
	}

	public void setExamMicroscopy(ExamMicroscopy examMicroscopy) {
		this.examMicroscopy = examMicroscopy;
	}

	public ExamCulture getExamCulture() {
		return examCulture;
	}

	public void setExamCulture(ExamCulture examCulture) {
		this.examCulture = examCulture;
	}

	public ExamHIV getExamHIV() {
		return examHIV;
	}

	public void setExamHIV(ExamHIV examHIV) {
		this.examHIV = examHIV;
	}

	public ExamXRay getExamXRay() {
		return examXRay;
	}

	public void setExamXRay(ExamXRay examXRay) {
		this.examXRay = examXRay;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public CaseRowGroup getGroup() {
		return group;
	}
}
