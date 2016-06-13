package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.utils.date.Period;

import javax.persistence.EntityManager;
import java.util.Date;

@Name("caseCloseHomeBD")
public class CaseCloseHomeBD extends CaseCloseHome{
	protected static final CaseState[] outcomesMDR = {
		CaseState.CURED,
        CaseState.TREATMENT_COMPLETED,
        CaseState.FAILED,
        CaseState.DIED,
        CaseState.DEFAULTED,
        CaseState.NOT_EVALUATED
    };

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
