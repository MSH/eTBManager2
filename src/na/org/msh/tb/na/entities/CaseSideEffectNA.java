package org.msh.tb.na.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.na.entities.enums.SideEffectAction;
import org.msh.tb.na.entities.enums.SideEffectGrading;
import org.msh.tb.na.entities.enums.SideEffectOutcome;
import org.msh.tb.na.entities.enums.SideEffectSeriousness;

@Entity
public class CaseSideEffectNA extends CaseSideEffect{

	@ManyToOne
	@JoinColumn(name="CASE_DATA_ID")
	@NotNull
	private CaseDataNA caseData;	

	private SideEffectGrading grade;
	
	private SideEffectAction actionTaken; 
	
	private SideEffectOutcome outcome;
	
	private SideEffectSeriousness seriousness;
	
	@Lob
	private String comments;	
		
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

	public CaseDataNA getCaseData() {
		return caseData;
	}

	public void setCaseData(CaseDataNA caseData) {
		this.caseData = caseData;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
}
