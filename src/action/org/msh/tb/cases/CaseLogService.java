package org.msh.tb.cases;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.tb.log.LogService;

@Name("caseLogService")
public class CaseLogService {

	@In(required=true) CaseHome caseHome;
	
	private LogService logService = new LogService();

	
	/**
	 * Register case validation 
	 */
	@Observer("case.validate")
	public void logValidation() {
		TbCase tbcase = caseHome.getInstance();
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB_DOCUMENTED)
			role = "TBVALIDATE";
		else role = "MDRVALIDATE";
		
		logService.saveExecuteTransaction(tbcase, role);
	}


	/**
	 * Register case transfer to another health unit
	 */
	@Observer("case.transfer")
	public void logCaseTransfer() {
		TbCase tbcase = caseHome.getInstance();
		CaseMoveHome caseMoveHome = (CaseMoveHome)Component.getInstance("caseMoveHome");
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB_DOCUMENTED)
			 role = "TBTRANSFER";
		else role = "MDRTRANSFER";
		
		logService.addValue("cases.movdate", caseMoveHome.getMoveDate());
		logService.addValue("patients.desthu", caseMoveHome.getTbunitselection().getTbunit().toString());
		
		logService.saveExecuteTransaction(tbcase, role);
	}
	
	
	/**
	 * Register case outcome
	 */
	@Observer("case.close")
	public void logCaseClose() {
		TbCase tbcase = caseHome.getInstance();
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB_DOCUMENTED)
			 role = "TBCLOSE";
		else role = "MDRCLOSE";

		logService.addValue("TbCase.outcomeDate", tbcase.getOutcomeDate());
		logService.addMessageValue("cases.outcome", tbcase.getState().getKey());
		logService.saveExecuteTransaction(tbcase, role);
	}

	
	/**
	 * Register case reopen
	 */
	@Observer("case.reopen")
	public void logCaseReopen() {
		TbCase tbcase = caseHome.getInstance();
		
		String role;
		if (tbcase.getClassification() == CaseClassification.TB_DOCUMENTED)
			 role = "TBREOPEN";
		else role = "MDRREOPEN";
		
		logService.saveExecuteTransaction("CaseState", role);
	}
}
