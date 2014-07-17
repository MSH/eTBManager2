package org.msh.tb.bd.entities;

import org.jboss.seam.annotations.Name;
import org.msh.tb.bd.entities.enums.SideEffectAction;
import org.msh.tb.bd.entities.enums.SideEffectGrading;
import org.msh.tb.bd.entities.enums.SideEffectOutcome;
import org.msh.tb.bd.entities.enums.SideEffectSeriousness;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.workspaces.customizable.WorkspaceCustomizationService;
import org.msh.utils.date.DateUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@DiscriminatorValue("bd")
@Name("caseSideEffectBD")
public class CaseSideEffectBD extends CaseSideEffect{

	private SideEffectGrading grade;
	
	private SideEffectAction actionTaken; 
	
	private SideEffectOutcome outcome;
	
	private SideEffectSeriousness seriousness;
	
	@Temporal(TemporalType.DATE)
	private Date effectSt;

	@Temporal(TemporalType.DATE)
	private Date effectEnd;
	
	//private String comments;	
	
	public Integer getMonthOfTreatment(){
		//for registers of before the changing of month of treatment to iniDate 
		if(getMonth() != 0)
			return getMonth();
		
		if(getTbcase().getTreatmentPeriod() == null || effectSt == null)
			return null;

		int i = 0;
		i = DateUtils.monthsBetween(getTbcase().getTreatmentPeriod().getIniDate(), effectSt)+1;
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

//	public String getComments() {
//		return comments;
//	}
//
//	public void setComments(String comments) {
//		this.comments = comments;
//	}

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

	/**
	 * Return month of treatment based on the start date of side effect
	 * @return
	 */
	public String getIniMonthTreatmentDisplay() {
		if (getEffectSt() == null)
			return null;
		WorkspaceCustomizationService wsservice = WorkspaceCustomizationService.instance();
		return wsservice.getExamControl().getMonthDisplay(getTbcase(), getEffectSt());
	}
	
	/**
	 * Return month of treatment based on the start treatment date and the collected date
	 * @return
	 */
	public String getEndMonthTreatmentDisplay() {
		if (getEffectEnd() == null)
			return null;
		WorkspaceCustomizationService wsservice = WorkspaceCustomizationService.instance();
		return wsservice.getExamControl().getMonthDisplay(getTbcase(), getEffectEnd());
	}
	
	public boolean hasAditionalinfo(){
		return ((getMedicines() != null && !getMedicines().trim().equals("")) 
				|| getGrade() != null || getSeriousness() != null || getActionTaken() != null || 
				getOutcome() != null ||	getEffectEnd() != null || (getComment() != null && !getComment().trim().equals("")));
	}
	
}
