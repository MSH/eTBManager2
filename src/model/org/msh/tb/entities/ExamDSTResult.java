package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.tb.entities.enums.DstResult;

@Entity
public class ExamDSTResult implements Serializable {
	private static final long serialVersionUID = -5594762900664251756L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="SUBSTANCE_ID")
	private Substance substance;
	
	@ManyToOne
	@JoinColumn(name="EXAM_ID")
	private ExamDST exam;
	
	private DstResult result;
	

	public Substance getSubstance() {
		return substance;
	}

	public void setSubstance(Substance substante) {
		this.substance = substante;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DstResult getResult() {
		return result;
	}

	public void setResult(DstResult result) {
		this.result = result;
	}

	/**
	 * @return the exam
	 */
	public ExamDST getExam() {
		return exam;
	}

	/**
	 * @param exam the exam to set
	 */
	public void setExam(ExamDST exam) {
		this.exam = exam;
	}
}
