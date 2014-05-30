package org.msh.tb.na.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.na.entities.enums.SideEffectAction;
import org.msh.tb.na.entities.enums.SideEffectGrading;
import org.msh.tb.na.entities.enums.SideEffectOutcome;
import org.msh.tb.na.entities.enums.SideEffectSeriousness;
import org.msh.tb.workspaces.customizable.WorkspaceCustomizationService;
import org.msh.utils.date.DateUtils;

/**
 * Implementation of the side effect entity data for Namibia
 * 
 * @author Ricardo Memoria
 *
 */
@Entity
@DiscriminatorValue("na")
public class CaseSideEffectNA extends CaseSideEffect{

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
		if(getTbcase().getTreatmentPeriod() != null && getTbcase().getTreatmentPeriod().getIniDate() != null)
			i = DateUtils.monthsBetween(getTbcase().getTreatmentPeriod().getIniDate(), effectSt)+1;
		return i;
		
	}
	
	/**
	 * Return the display message of the month of treatment of this side effect, following the 
	 * rules of month displaying for exams and other case data 
	 * @return String message containing the month of treatment
	 */
	public String getMonthDisplay() {
		WorkspaceCustomizationService wsservice = WorkspaceCustomizationService.instance();
		return wsservice.getExamControl().getMonthDisplay(getTbcase(), getEffectSt());
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
