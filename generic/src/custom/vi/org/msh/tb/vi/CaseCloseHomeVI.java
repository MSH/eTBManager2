package org.msh.tb.vi;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;

@Name("caseCloseHomeVI")
public class CaseCloseHomeVI extends CaseCloseHome{
	
	protected static final CaseState[] outcomesMDR = {
		CaseState.CURED, 
		CaseState.DEFAULTED, 
		CaseState.DIED, 
		CaseState.FAILED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.TRANSFERRED_OUT,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.TREATMENT_REFUSED,
		CaseState.OTHER};

	protected static final CaseState[] outcomesTB = {
		CaseState.CURED, 
		CaseState.DEFAULTED, 
		CaseState.DIED, 
		CaseState.FAILED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.TRANSFERRED_OUT,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.MDR_CASE,
		CaseState.TREATMENT_REFUSED,
		CaseState.OTHER};

	/**
	 * Return the available outcomes of a case, according to its classification (TB or DR-TB)
	 * @return Array of {@link CaseState} enumerations
	 */
	public CaseState[] getOutcomes() {
		if (caseHome.getInstance().getDiagnosisType() == DiagnosisType.SUSPECT)
			return suspectOutcomes;

		if (caseHome.getInstance().getClassification() == CaseClassification.DRTB)
			 return outcomesMDR;
		else return outcomesTB;
	}

	
}
