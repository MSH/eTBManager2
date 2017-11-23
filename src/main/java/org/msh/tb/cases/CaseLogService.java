package org.msh.tb.cases;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.etbm.commons.transactionlog.ActionTX;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.RoleAction;

import java.util.Date;

@Name("caseLogService")
public class CaseLogService {

	@In(required=true) CaseHome caseHome;
	
//	private TransactionLogService logService = (TransactionLogService)Component.getInstance("transactionLogService", true);


	/**
	 * Begin a transaction
	 * @param roleName
	 * @param tbcase
	 * @return
	 */
	protected ActionTX beginTX(String roleName, TbCase tbcase) {
		return ActionTX
				.begin(roleName, tbcase, RoleAction.EXEC)
				.setEntityClass( TbCase.class.getSimpleName() );
	}


	/**
	 * Helper function to save some typing 
	 * @param roleName
	 * @param tbcase
	 */
/*
	protected void saveExecuteTransaction(String roleName, TbCase tbcase) {
		logService.saveExecuteTransaction(roleName, tbcase.toString(), tbcase.getId(), TbCase.class.getSimpleName(), tbcase);
	}
*/

	/**
	 * Register case validation 
	 */
	@Observer("case.validate")
	public void logValidation() {
		TbCase tbcase = caseHome.getInstance();
		
		beginTX("CASE_VALIDATE", tbcase)
                .addRow("DisplayCaseNumber.VALIDATION_NUMBER", tbcase.getDisplayValidationNumber())
				.end();
	}


	/**
	 * Register case transfer out to another health unit
	 */
	@Observer("case.transferout")
	public void logCaseTransferOut() {
		TbCase tbcase = caseHome.getInstance();
		CaseMoveHome caseMoveHome = (CaseMoveHome)Component.getInstance("caseMoveHome");

		ActionTX atx = beginTX("CASE_TRANSFEROUT", tbcase);

		atx.getDetailWriter().addTableRow("cases.movdate", caseMoveHome.getMoveDate());
		atx.getDetailWriter().addTableRow("patients.desthu", caseMoveHome.getTbunitselection2().getSelected().toString());
		atx.getDetailWriter().addTableRow("AdministrativeUnit", caseMoveHome.getTbunitselection2().getSelected().getAdminUnit().getFullDisplayName());

		atx.end();
	}
	

	/**
	 * Register case transfer in to another health unit
	 */
	@Observer("case.transferin")
	public void logCaseTransferIn() {
		TbCase tbcase = caseHome.getInstance();
		CaseMoveHome caseMoveHome = (CaseMoveHome)Component.getInstance("caseMoveHome");

		ActionTX atx = beginTX("CASE_TRANSFERIN", tbcase);

		atx.getDetailWriter().addTableRow("cases.movdate", caseMoveHome.getMoveDate());
		atx.getDetailWriter().addTableRow("patients.desthu", tbcase.getOwnerUnit().getName().toString());
//		logService.addTableRow("AdministrativeUnit", caseMoveHome.getTbunitselection().getTbunit().getAdminUnit().getFullDisplayName());

		atx.end();
		//saveExecuteTransaction("CASE_TRANSFERIN", tbcase);
	}


	/**
	 * Register case rolling back of case transferring out
	 */
	@Observer("case.transferout.rollback")
	public void logCaseTransferOutRoolback() {
		TbCase tbcase = caseHome.getInstance();
		
		Date transferDate = (Date)Component.getInstance("transferdate");
		Tbunit unit = (Tbunit)Component.getInstance("transferunit");

		ActionTX atx = beginTX("CASE_TRANSFERCANCEL", tbcase);

		atx.getDetailWriter().addTableRow("cases.movdate", transferDate);
		atx.getDetailWriter().addTableRow("patients.desthu", unit.getName().toString());
		atx.getDetailWriter().addTableRow("AdministrativeUnit", unit.getAdminUnit().getFullDisplayName());

		atx.end();

		//saveExecuteTransaction("CASE_TRANSFERCANCEL", tbcase);
	}
	
	
	/**
	 * Register case outcome
	 */
	@Observer("case.close")
	public void logCaseClose() {
		TbCase tbcase = caseHome.getInstance();

		ActionTX atx = beginTX("CASE_CLOSE", tbcase);
		
		atx.getDetailWriter().addTableRow("TbCase.outcomeDate", tbcase.getOutcomeDate());
		atx.getDetailWriter().addTableRow("cases.outcome", tbcase.getState());

		if (tbcase.getState().equals(CaseState.OTHER)) {
			atx.getDetailWriter().addTableRow("TbCase.otherOutcome", tbcase.getOtherOutcome());
		}

		atx.end();
		//saveExecuteTransaction("CASE_CLOSE", tbcase);
	}

	
	/**
	 * Register case reopen
	 */
	@Observer("case.reopen")
	public void logCaseReopen() {
		TbCase tbcase = caseHome.getInstance();

		beginTX("CASE_REOPEN", tbcase).end();
//		saveExecuteTransaction("CASE_REOPEN", tbcase);
	}
	
	/**
	 * Register case number changed
	 */
	@Observer("case.casenumbermodified")
	public void logCaseNumberModified() {
		TbCase tbcase = caseHome.getInstance();

		beginTX("CASE_CHANGENUMBER", tbcase).end();
//		saveExecuteTransaction("CASE_CHANGENUMBER", tbcase);
	}
	
	@Observer("case.starttreatment")
	public void logStartTreatment() {
		
	}
}
