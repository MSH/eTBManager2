package org.msh.tb.kh;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;

@Name("caseCloseKHHome")
public class CaseCloseKHHome extends CaseCloseHome{


	/**
	 * 
	 */
	private static final long serialVersionUID = 4978288410994732240L;
	
	private static final CaseState[] suspectOutcomes = {
		CaseState.NOT_TB, 
		CaseState.DIED, 
		CaseState.DEFAULTED,
		CaseState.TB_NOT_DETECTED,
		CaseState.SUSCEPTIBLE_TB,
		CaseState.TB_RESISTANCE_ES,
		CaseState.OTHER};

	/**
	 * Return the available outcomes of a case, according to its classification (TB or DR-TB)
	 * @return Array of {@link CaseState} enumerations
	 */
	@Override
	public CaseState[] getOutcomes() {
		if (caseHome.getInstance().getDiagnosisType() == DiagnosisType.SUSPECT)
			return suspectOutcomes;

		if (caseHome.getInstance().getClassification() == CaseClassification.DRTB)
			 return outcomesMDR;
		else return outcomesTB;
	}
	
	
}
