package org.msh.tb.ua.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.ua.entities.enums.SideEffectGrading;
import org.msh.tb.ua.entities.enums.SideEffectOutcome;
import org.msh.utils.date.DateUtils;

@Entity
@DiscriminatorValue("ua")
@Name("caseSideEffectUA")
public class CaseSideEffectUA extends CaseSideEffect {
	
	private SideEffectGrading grade;
	private String symptomTherapy;
	@Temporal(TemporalType.DATE)
	private Date dateChangeReg;
	private SideEffectOutcome outcome;
	@Temporal(TemporalType.DATE)
	private Date effectSt;
	@Temporal(TemporalType.DATE)
	private Date effectEnd;
	
	public Integer getMonthOfTreatment(){
		int i = 0;
		if(getTbcase().getTreatmentPeriod() == null) return null;
		if(getTbcase().getTreatmentPeriod().getIniDate() == null) return null;
		if (effectSt == null)
			return super.getMonth();
		i = DateUtils.monthsBetween(getTbcase().getTreatmentPeriod().getIniDate(), effectSt)+1;
		return i;
		
	}
	
	public SideEffectGrading getGrade() {
		return grade;
	}
	public void setGrade(SideEffectGrading grade) {
		this.grade = grade;
	}
	public SideEffectOutcome getOutcome() {
		return outcome;
	}
	public void setOutcome(SideEffectOutcome outcome) {
		this.outcome = outcome;
	}
	public void setEffectEnd(Date effectEnd) {
		this.effectEnd = effectEnd;
	}
	public Date getEffectEnd() {
		return effectEnd;
	}
	public void setDateChangeReg(Date dateChangeReg) {
		this.dateChangeReg = dateChangeReg;
	}
	public Date getDateChangeReg() {
		return dateChangeReg;
	}
	public void setSymptomTherapy(String symptomTherapy) {
		this.symptomTherapy = symptomTherapy;
	}
	public String getSymptomTherapy() {
		return symptomTherapy;
	}
	public void setEffectSt(Date effectSt) {
		this.effectSt = effectSt;
	}
	public Date getEffectSt() {
		return effectSt;
	}



}
