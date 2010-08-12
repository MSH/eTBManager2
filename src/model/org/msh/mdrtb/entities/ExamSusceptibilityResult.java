package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.mdrtb.entities.enums.SusceptibilityResultTest;

@Entity
public class ExamSusceptibilityResult implements Serializable {
	private static final long serialVersionUID = -5594762900664251756L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="SUBSTANCE_ID")
	private Substance substance;
	
	@ManyToOne
	@JoinColumn(name="EXAM_ID")
	private ExamSusceptibilityTest exam;
	
	private SusceptibilityResultTest result;
	

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

	public SusceptibilityResultTest getResult() {
		return result;
	}

	public void setResult(SusceptibilityResultTest result) {
		this.result = result;
	}

	/**
	 * @return the exam
	 */
	public ExamSusceptibilityTest getExam() {
		return exam;
	}

	/**
	 * @param exam the exam to set
	 */
	public void setExam(ExamSusceptibilityTest exam) {
		this.exam = exam;
	}
}
