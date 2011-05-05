package org.msh.tb.ph.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.tb.entities.FieldValue;
import org.msh.tb.ph.entities.enums.PhysicalExamResult;

@Entity
public class PhysicalExam {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="CASEDATA_ID")
	private CaseDataPH caseDataPH;
	
	@ManyToOne
	@JoinColumn(name="EXAM_ID")
	private FieldValue exam;
	
	private PhysicalExamResult result;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the exam
	 */
	public FieldValue getExam() {
		return exam;
	}

	/**
	 * @param exam the exam to set
	 */
	public void setExam(FieldValue exam) {
		this.exam = exam;
	}

	/**
	 * @return the result
	 */
	public PhysicalExamResult getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(PhysicalExamResult result) {
		this.result = result;
	}

	/**
	 * @param caseDataPH the caseDataPH to set
	 */
	public void setCaseDataPH(CaseDataPH caseDataPH) {
		this.caseDataPH = caseDataPH;
	}

	/**
	 * @return the caseDataPH
	 */
	public CaseDataPH getCaseDataPH() {
		return caseDataPH;
	}
}
