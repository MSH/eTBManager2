package org.msh.tb.cases;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.transactionlog.TransactionLogService;

@Name("caseLogService")
public class CaseLogService {

	@In(required=true) CaseHome caseHome;
	
	private TransactionLogService logService = new TransactionLogService();

	
	/**
	 * Register case validation 
	 */
	@Observer("case.validate")
	public void logValidation() {
		TbCase tbcase = caseHome.getInstance();
		
		logService.saveExecuteTransaction("CASE_VALIDATE", tbcase.toString(), tbcase.getId());
	}


	/**
	 * Register case transfer to another health unit
	 */
	@Observer("case.transfer")
	public void logCaseTransfer() {
		TbCase tbcase = caseHome.getInstance();
		CaseMoveHome caseMoveHome = (CaseMoveHome)Component.getInstance("caseMoveHome");
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB)
			 role = "TBTRANSFER";
		else role = "MDRTRANSFER";
		
		logService.addTableRow("cases.movdate", caseMoveHome.getMoveDate());
		logService.addTableRow("patients.desthu", caseMoveHome.getTbunitselection().getTbunit().toString());
		
		logService.saveExecuteTransaction(role, tbcase.toString(), tbcase.getId());
	}
	
	
	/**
	 * Register case outcome
	 */
	@Observer("case.close")
	public void logCaseClose() {
		TbCase tbcase = caseHome.getInstance();
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB)
			 role = "TBCLOSE";
		else role = "MDRCLOSE";

		logService.addTableRow("TbCase.outcomeDate", tbcase.getOutcomeDate());
		logService.addTableRow("cases.outcome", tbcase.getState());
		logService.saveExecuteTransaction(role, tbcase.toString(), tbcase.getId());
	}

	
	/**
	 * Register case reopen
	 */
	@Observer("case.reopen")
	public void logCaseReopen() {
		TbCase tbcase = caseHome.getInstance();
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB)
			 role = "TBREOPEN";
		else role = "MDRREOPEN";
		
		logService.saveExecuteTransaction(role, tbcase.toString(), tbcase.getId());
	}
}
