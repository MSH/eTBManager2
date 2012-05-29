package org.msh.tb.kh.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.kh.entities.enums.SideEffectGrading;

@Entity
@DiscriminatorValue("kh")
public class CaseSideEffectKH extends CaseSideEffect{
	
	@ManyToOne
	@JoinColumn(name="CASE_DATA_ID")
	@NotNull
	private TbCaseKH tbcasekh;
	
	private SideEffectGrading grade;

	public SideEffectGrading getGrade() {
		return grade;
	}
	
	public void setGrade(SideEffectGrading grade) {
		this.grade = grade;
	}
	
	public TbCaseKH getTbcasekh() {
		return tbcasekh;
	}

	public void setTbcasekh(TbCaseKH tbcasekh) {
		this.tbcasekh = tbcasekh;
	}
}
