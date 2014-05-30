package org.msh.tb.kh.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.kh.entities.enums.SideEffectGrading;

@Entity
@DiscriminatorValue("kh")
public class CaseSideEffectKH extends CaseSideEffect{
	
	private SideEffectGrading grade;

	public SideEffectGrading getGrade() {
		return grade;
	}
	
	public void setGrade(SideEffectGrading grade) {
		this.grade = grade;
	}
	
}
