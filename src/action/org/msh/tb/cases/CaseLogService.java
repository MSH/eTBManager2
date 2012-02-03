package org.msh.tb.cases;

import java.util.Date;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.transactionlog.TransactionLogService;

@Name("caseLogService")
public class CaseLogService {

	@In(required=true) CaseHome caseHome;
	
	private TransactionLogService logService = (TransactionLogService)Component.getInstance("transactionLogService", true);

	
	/**
	 * Helper function to save some typing 
	 * @param roleName
	 * @param tbcase
	 */
	protected void saveExecuteTransaction(String roleName, TbCase tbcase) {
		logService.saveExecuteTransaction(roleName, tbcase.toString(), tbcase.getId(), TbCase.class.getSimpleName());
	}
	
	/**
	 * Register case validation 
	 */
	@Observer("case.validate")
	public void logValidation() {
		TbCase tbcase = caseHome.getInstance();
		
		saveExecuteTransaction("CASE_VALIDATE", tbcase);
	}


	/**
	 * Register case transfer out to another health unit
	 */
	@Observer("case.transferout")
	public void logCaseTransferOut() {
		TbCase tbcase = caseHome.getInstance();
		CaseMoveHome caseMoveHome = (CaseMoveHome)Component.getInstance("caseMoveHome");
		
		logService.addTableRow("cases.movdate", caseMoveHome.getMoveDate());
		logService.addTableRow("patients.desthu", caseMoveHome.getTbunitselection().getTbunit().toString());
		logService.addTableRow("AdministrativeUnit", caseMoveHome.getTbunitselection().getTbunit().getAdminUnit().getFullDisplayName());
		
		saveExecuteTransaction("CASE_TRANSFEROUT", tbcase);
	}
	

	/**
	 * Register case transfer in to another health unit
	 */
	@Observer("case.transferin")
	public void logCaseTransferIn() {
		TbCase tbcase = caseHome.getInstance();
		CaseMoveHome caseMoveHome = (CaseMoveHome)Component.getInstance("caseMoveHome");
		
		logService.addTableRow("cases.movdate", caseMoveHome.getMoveDate());
		logService.addTableRow("patients.desthu", tbcase.getTreatmentUnit().getName().toString());
		logService.addTableRow("AdministrativeUnit", caseMoveHome.getTbunitselection().getTbunit().getAdminUnit().getFullDisplayName());
		
		saveExecuteTransaction("CASE_TRANSFERIN", tbcase);
	}


	/**
	 * Register case rolling back of case transferring out
	 */
	@Observer("case.transferout.rollback")
	public void logCaseTransferOutRoolback() {
		TbCase tbcase = caseHome.getInstance();
		
		Date transferDate = (Date)Component.getInstance("transferdate");
		Tbunit unit = (Tbunit)Component.getInstance("transferunit");
		
		logService.addTableRow("cases.movdate", transferDate);
		logService.addTableRow("patients.desthu", unit.getName().toString());
		logService.addTableRow("AdministrativeUnit", unit.getAdminUnit().getFullDisplayName());
		
		saveExecuteTransaction("CASE_TRANSFERCANCEL", tbcase);
	}
	
	
	/**
	 * Register case outcome
	 */
	@Observer("case.close")
	public void logCaseClose() {
		TbCase tbcase = caseHome.getInstance();
		
		logService.addTableRow("TbCase.outcomeDate", tbcase.getOutcomeDate());
		logService.addTableRow("cases.outcome", tbcase.getState());

		if (tbcase.getState().equals(CaseState.OTHER))
			logService.addTableRow("TbCase.otherOutcome", tbcase.getOtherOutcome());
		
		saveExecuteTransaction("CASE_CLOSE", tbcase);
	}

	
	/**
	 * Register case reopen
	 */
	@Observer("case.reopen")
	public void logCaseReopen() {
		TbCase tbcase = caseHome.getInstance();
		
		saveExecuteTransaction("CASE_REOPEN", tbcase);
	}
}
