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
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.utils.date.Period;

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
