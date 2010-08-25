package org.msh.tb.ph;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.mdrtb.entities.enums.TbField;
import org.msh.tb.ph.entities.enums.PhysicalExamResult;

@Name("globalLists_ph")
public class GlobalLists {
	
	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.FAILURE_CATI,
		PatientType.FAILURE_CATII,
		PatientType.FAILURE_CATIV,
		PatientType.AFTER_DEFAULT,
		PatientType.RELAPSE_CATI,
		PatientType.RELAPSE_CATII,
		PatientType.RELAPSE_CATIV,
		PatientType.TRANSFER_IN,
		PatientType.OTHER_NONDOTS,
		PatientType.OTHERPOS,
		PatientType.OTHERNEG
	};

	private static final TbField tbfields[] = {
		TbField.PHYSICAL_EXAMS,
		TbField.DIAG_CONFIRMATION,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.SMEAR_METHOD,
		TbField.SYMPTOMS,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES
	};
	
	private static final PrevTBTreatmentOutcome prevTBTreatmentOutcomes[] = {
		PrevTBTreatmentOutcome.CURED,
		PrevTBTreatmentOutcome.COMPLETED,
		PrevTBTreatmentOutcome.FAILURE,
		PrevTBTreatmentOutcome.DEFAULTED,
		PrevTBTreatmentOutcome.SCHEME_CHANGED,
		PrevTBTreatmentOutcome.SHIFT_CATIV,
		PrevTBTreatmentOutcome.UNKNOWN
	};
	
	private static final CaseState outcomes[] = {
		CaseState.CURED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.FAILED,
		CaseState.DEFAULTED,
		CaseState.DIED
	};

	@Factory("phPatientTypes")
	public PatientType[] getPatientTypes() {
		return patientTypes;
	}
	
	@Factory("phPrevTBTreatmentOutcomes")
	public PrevTBTreatmentOutcome[] getPrevTBTreatmentOutcomes() {
		return prevTBTreatmentOutcomes;
	}
	
	@Factory("phOutcomes")
	public CaseState[] getOutcomes() {
		return outcomes;
	}
	
	@Factory("physicalExamResults")
	public PhysicalExamResult[] getPhysicalExamResults() {
		return PhysicalExamResult.values();
	}
	
	/**
	 * Returns list of TB fields available for drop down menus
	 * @return array of TbField enum
	 */
	@Factory("tbFields.ph")
	public TbField[] getTbFields() {
		return tbfields;
	}
}
