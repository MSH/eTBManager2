package org.msh.tb.br;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.date.Period;

@Name("caseCloseHomeBR")
public class CaseCloseHome extends Controller{
	private static final long serialVersionUID = 805003311511687697L;

	@In(create=true) CaseHome caseHome;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) TreatmentHome treatmentHome;
	@In(create=true) CaseDataBRHome caseDataBRHome;
	@In EntityManager entityManager;

	
	private Date date;
	private CaseState state;
	private String comment;
	
	private static final CaseState[] outcomes = {
		CaseState.CURED, 
		CaseState.TREATMENT_COMPLETED,
		CaseState.DEFAULTED, 
		CaseState.FAILED,
		CaseState.DIED, 
		CaseState.DIED_NOTTB, 
		CaseState.TRANSFERRED_OUT,
		CaseState.REGIMEN_CHANGED,
		CaseState.MDR_CASE,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.OTHER};

	
	/**
	 * Closes an opened case, setting its outcome
	 * @return
	 */
	@Transactional
	public String closeCase() {
		TbCase tbcase = caseHome.getInstance();

		if (date.before(tbcase.getRegistrationDate())) {
			facesMessages.addFromResourceBundle("cases.close.msg1");
			return "error";
		}
		
		tbcase.setOutcomeDate(date);
		if ((!tbcase.getTreatmentPeriod().isEmpty()))
			treatmentHome.cropTreatmentPeriod(new Period(tbcase.getTreatmentPeriod().getIniDate(), date));

		tbcase.setState(state);
		if (state.equals(CaseState.OTHER))
			 tbcase.setOtherOutcome(comment);
		else tbcase.setOtherOutcome(null);
		
		caseHome.persist();

		// register log
		TransactionLogService logsrv = new TransactionLogService();
		logsrv.addTableRow("TbCase.outcomeDate", tbcase.getOutcomeDate());
		logsrv.addTableRow("CaseState", state);
		logsrv.saveExecuteTransaction("CASE_CLOSE", tbcase);

		return "case-closed";
	}

	
	@Transactional
	public String reopenCase() {
		TbCase tbcase = caseHome.getInstance();
		
		if (tbcase.getTreatmentPeriod().isEmpty())
			 tbcase.setState(CaseState.WAITING_TREATMENT);
		else tbcase.setState(CaseState.ONTREATMENT);
		
		tbcase.setOtherOutcome(null);
		
		caseHome.persist();

		// register log
		TransactionLogService logsrv = new TransactionLogService();
		logsrv.addTableRow("CaseState", state);
		logsrv.saveExecuteTransaction("CASE_REOPEN", tbcase);
		
		return "case-reopened";
	}


	/**
	 * Return the available outcomes of a case, according to its classification (TB or DR-TB)
	 * @return Array of {@link CaseState} enumerations
	 */
	public CaseState[] getOutcomes() {
		return outcomes;
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
