package org.msh.tb.az;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.az.entities.enums.CaseFindingStrategy;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ClinicalEvolution;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.entities.enums.TbField;

@Name("globalLists_az")
@BypassInterceptors
public class GlobalLists {

	private final static PatientType[] patientTypes = {
		PatientType.NEW,
		PatientType.AFTER_DEFAULT,
		PatientType.RELAPSE,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.TRANSFER_IN,
		PatientType.OTHER
	};

	private static final TbField tbfields[] = {
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES,
		TbField.MARITAL_STATUS,
		TbField.EDUCATIONAL_DEGREE,
		TbField.SEVERITY_MARKS,
		TbField.XRAY_LOCALIZATION,
		TbField.ADJUSTMENT
	};

	private static final PrevTBTreatmentOutcome prevTBTreatmentOutcomes[] = {
		PrevTBTreatmentOutcome.CURED,
		PrevTBTreatmentOutcome.COMPLETED,
		PrevTBTreatmentOutcome.DEFAULTED,
		PrevTBTreatmentOutcome.FAILURE,
		PrevTBTreatmentOutcome.UNKNOWN
	};
	
	private static final CaseState caseStates[] = {
		CaseState.ONTREATMENT,
		CaseState.TRANSFERRING,
		CaseState.CURED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.FAILED,
		CaseState.DEFAULTED,
		CaseState.DIED,
		CaseState.DIED_NOTTB,
		CaseState.TRANSFERRED_OUT,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.OTHER,
		CaseState.REGIMEN_CHANGED,
		CaseState.MDR_CASE
	};
	

	private static final HIVResult hivResults[] = {
		HIVResult.ONGOING,
		HIVResult.NEGATIVE,
		HIVResult.POSITIVE
	};

	private static final DstResult dstResults[] = {
		DstResult.NOTDONE,
		DstResult.SUSCEPTIBLE,
		DstResult.RESISTANT,
		DstResult.CONTAMINATED,
		DstResult.BASELINE
	};
	
	private static final MicroscopyResult microscopyResults[] = {
		MicroscopyResult.NEGATIVE,
		MicroscopyResult.POSITIVE,
		MicroscopyResult.PLUS,
		MicroscopyResult.PLUS2,
		MicroscopyResult.PLUS3,
		MicroscopyResult.PLUS4
	};

	
	private static final ClinicalEvolution clinicalEvolution[] = {
		ClinicalEvolution.FAVORABLE,
		ClinicalEvolution.UNCHANGED,
		ClinicalEvolution.UNFAVORABLE
		
	};
	
	
/* never used, also specific to Brazil - AK
	public TipoResistencia[] getTiposResistencia() {
		return TipoResistencia.values();
	}
*/
	
	public CaseFindingStrategy[] getCaseFindingStrategies() {
		return CaseFindingStrategy.values();
	}

	/**
	 * Return list of HIV results specific for Brazilian version
	 * @return
	 */
	public HIVResult[] getHivResults() {
		return hivResults;
	}
	
	public PatientType[] getPatientTypes() {
		return patientTypes;
	}
	
	public TbField[] getTbFields() {
		return tbfields;
	}
	
	public PrevTBTreatmentOutcome[] getPrevTBTreatmentOutcomes() {
		return prevTBTreatmentOutcomes;
	}
	/* never used and also specific to Brazil - AK
	public FailureType[] getFailureTypes() {
		return FailureType.values();
	}	
	*/
	public ClinicalEvolution[] getClinicalEvolutions() {
		return clinicalEvolution;
	}
	
	public CaseState[] getCaseStates() {
		return caseStates;
	}
	
	public DstResult[] getDstResults() {
		return dstResults;
	}
	
	public MicroscopyResult[] getMicroscopyResults() {
		return microscopyResults;
	}

}
