package org.msh.tb.kh;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.utils.date.Period;

@Name("caseCloseKHHome")
public class CaseCloseKHHome extends CaseCloseHome{


	/**
	 * 
	 */
	private static final long serialVersionUID = 4978288410994732240L;
	@In(create=true) CaseHome caseHome;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) TreatmentHome treatmentHome;
	@In EntityManager entityManager;

	
	private Date date;
	private CaseState state;
	private String comment;
	
	private static final CaseState[] outcomesMDR = {
		CaseState.CURED, 
		CaseState.DEFAULTED, 
		CaseState.DIED, 
		CaseState.FAILED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.TRANSFERRED_OUT,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.NTM,
		CaseState.SUSCEPTIBLE_TB,
		CaseState.OTHER};

	private static final CaseState[] outcomesTB = {
		CaseState.CURED, 
		CaseState.DEFAULTED, 
		CaseState.DIED, 
		CaseState.FAILED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.TRANSFERRED_OUT,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.MDR_CASE,
		CaseState.NTM,
		CaseState.SUSCEPTIBLE_TB,
		CaseState.OTHER};


	/**
	 * Closes an opened case, setting its outcome
	 * @return
	 */
	@Transactional
	public String closeCase() {
		TbCase tbcase = caseHome.getInstance();

		if (!validateClose()) 
			return "error";

		if ((tbcase.getTreatmentPeriod() != null) && (!tbcase.getTreatmentPeriod().isEmpty()))
			treatmentHome.cropTreatmentPeriod(new Period(tbcase.getTreatmentPeriod().getIniDate(), date));
		
		tbcase.setOutcomeDate(date);

		tbcase.setState(state);
		if (state.equals(CaseState.OTHER))
			 tbcase.setOtherOutcome(comment);
		else tbcase.setOtherOutcome(null);

		// save case changes
		caseHome.setTransactionLogActive(false);
		caseHome.persist();
		caseHome.updateCaseTags();

		Events.instance().raiseEvent("case.close");
		
		return "case-closed";
	}


	/**
	 * Check if case can be closed
	 * @return
	 */
	public boolean validateClose() {
		TbCase tbcase = caseHome.getInstance();

		Date dt = tbcase.getDiagnosisDate();
		if ((dt != null) && (date.before(dt))) {
			facesMessages.addFromResourceBundle("cases.close.msg1");
			return false;
		}
		
		return true;
	}


	/**
	 * Reopen a closed case
	 * @return
	 */
	@Transactional
	@RaiseEvent("case.reopen")
	public String reopenCase() {
		TbCase tbcase = caseHome.getInstance();
		
		if ((tbcase.getTreatmentPeriod() == null) || (tbcase.getTreatmentPeriod().isEmpty()))
			 tbcase.setState(CaseState.WAITING_TREATMENT);
		else tbcase.setState(CaseState.ONTREATMENT);
		
		tbcase.setOtherOutcome(null);

		caseHome.setTransactionLogActive(false);
		caseHome.persist();
		caseHome.updateCaseTags();
		
		return "case-reopened";
	}


	/**
	 * Return the available outcomes of a case, according to its classification (TB or DR-TB)
	 * @return Array of {@link CaseState} enumerations
	 */
	public CaseState[] getOutcomes() {
		if (caseHome.getInstance().getClassification() == CaseClassification.DRTB)
			 return outcomesMDR;
		else return outcomesTB;
	}



	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public CaseState getState() {
		return state;
	}

	public void setState(CaseState state) {
		this.state = state;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	
}
