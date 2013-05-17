package org.msh.tb.na.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.na.entities.enums.SideEffectAction;
import org.msh.tb.na.entities.enums.SideEffectGrading;
import org.msh.tb.na.entities.enums.SideEffectOutcome;
import org.msh.tb.na.entities.enums.SideEffectSeriousness;
import org.msh.utils.date.DateUtils;

@Entity
@DiscriminatorValue("na")
public class CaseSideEffectNA extends CaseSideEffect{

	@ManyToOne
	@JoinColumn(name="CASE_DATA_ID")
	@NotNull
	private TbCaseNA tbcasena;	

	private SideEffectGrading grade;
	
	private SideEffectAction actionTaken; 
	
	private SideEffectOutcome outcome;
	
	private SideEffectSeriousness seriousness;
	
	@Temporal(TemporalType.DATE)
	private Date effectSt;

	@Temporal(TemporalType.DATE)
	private Date effectEnd;	
	
	public Integer getMonthOfTreatment(){
		int i = 0;
		if(tbcasena.getTreatmentPeriod() != null && tbcasena.getTreatmentPeriod().getIniDate() != null)
			i = DateUtils.monthsBetween(tbcasena.getTreatmentPeriod().getIniDate(), effectSt)+1;
		return i;
		
	}
		
	public SideEffectGrading getGrade() {
		return grade;
	}

	public void setGrade(SideEffectGrading grade) {
		this.grade = grade;
	}

	public SideEffectAction getActionTaken() {
		return actionTaken;
	}

	public void setActionTaken(SideEffectAction actionTaken) {
		this.actionTaken = actionTaken;
	}

	public SideEffectOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(SideEffectOutcome outcome) {
		this.outcome = outcome;
	}

	public SideEffectSeriousness getSeriousness() {
		return seriousness;
	}

	public void setSeriousness(SideEffectSeriousness seriousness) {
		this.seriousness = seriousness;
	}

	public TbCaseNA getTbcasena() {
		return tbcasena;
	}

	public void setTbcasena(TbCaseNA tbcasena) {
		this.tbcasena = tbcasena;
	}

	public Date getEffectSt() {
		return effectSt;
	}

	public void setEffectSt(Date effectSt) {
		this.effectSt = effectSt;
	}

	public Date getEffectEnd() {
		return effectEnd;
	}

	public void setEffectEnd(Date effectEnd) {
		this.effectEnd = effectEnd;
	}

	
}
