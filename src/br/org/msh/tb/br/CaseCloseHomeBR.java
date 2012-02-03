package org.msh.tb.br;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.br.entities.TbCaseBR;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.transactionlog.TransactionLogService;

@Name("caseCloseHomeBR")
public class CaseCloseHomeBR extends CaseCloseHome {
	private static final long serialVersionUID = 805003311511687697L;

	@In(create=true) CaseHome caseHome;

	
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
		if (!validateClose()) {
			return "error";
		}

		TbCaseBR tbcase = (TbCaseBR)caseHome.getInstance();
		
		TransactionLogService logService = (TransactionLogService)Component.getInstance("transactionLogService", true);

		switch (getState()) {
		case MDR_CASE:
			logService.addTableRow("TbField.SCHEMA_TYPES", tbcase.getOutcomeResistanceType().toString());
			break;
		case REGIMEN_CHANGED:
			logService.addTableRow("TbField.RESISTANCE_TYPES", tbcase.getOutcomeRegimenChanged().toString());
			break;
		}
		
		return super.closeCase();
	}


	/**
	 * Return the available outcomes of a case, according to its classification (TB or DR-TB)
	 * @return Array of {@link CaseState} enumerations
	 */
	public CaseState[] getOutcomes() {
		return outcomes;
	}


}
