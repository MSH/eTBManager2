package org.msh.tb.br;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.br.entities.enums.FailureType;
import org.msh.tb.br.entities.enums.TipoResistencia;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ClinicalEvolution;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.entities.enums.TbField;

@Name("globalLists_br")
@BypassInterceptors
public class GlobalLists {

	private final static PatientType[] patientTypes = {
		PatientType.NEW,
		PatientType.AFTER_DEFAULT,
		PatientType.RELAPSE,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.SCHEMA_CHANGED,
		PatientType.RESISTANCE_PATTERN_CHANGED,
		PatientType.OTHER
	};

	private static final TbField tbfields[] = {
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.CULTURE_METHOD,
		TbField.DST_METHOD,
		TbField.MOLECULARBIOLOGY_METHOD,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES,
		TbField.SKINCOLOR,
		TbField.PREGNANCE_PERIOD,
		TbField.EDUCATIONAL_DEGREE,
		TbField.CONTAG_PLACE, 
		TbField.SCHEMA_TYPES, 
		TbField.RESISTANCE_TYPES,
		TbField.POSITION,
		TbField.MICOBACTERIOSE,
		TbField.XRAY_CONTACT,
		TbField.ADJUSTMENT,
		TbField.TREATMENT_OUTCOME_ILTB
	};

	private static final PrevTBTreatmentOutcome prevTBTreatmentOutcomes[] = {
		PrevTBTreatmentOutcome.CURED,
		PrevTBTreatmentOutcome.COMPLETED,
		PrevTBTreatmentOutcome.DEFAULTED,
		PrevTBTreatmentOutcome.DIAGNOSTIC_CHANGED,
		PrevTBTreatmentOutcome.FAILURE,
		PrevTBTreatmentOutcome.SCHEME_CHANGED,
		PrevTBTreatmentOutcome.SHIFT_CATIV
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
		HIVResult.NOTDONE,
		HIVResult.ONGOING,
		HIVResult.NEGATIVE,
		HIVResult.POSITIVE
	};
	
	private static final DrugResistanceType drugResistanceTypes[] = {
		DrugResistanceType.MONO_RESISTANCE,
		DrugResistanceType.POLY_RESISTANCE,
		DrugResistanceType.MULTIDRUG_RESISTANCE,
		DrugResistanceType.EXTENSIVEDRUG_RESISTANCE
	
	};

	public TipoResistencia[] getTiposResistencia() {
		return TipoResistencia.values();
	}

	
	private static final ClinicalEvolution clinicalEvolution[] = {
		ClinicalEvolution.FAVORABLE,
		ClinicalEvolution.UNCHANGED,
		ClinicalEvolution.UNFAVORABLE,
		ClinicalEvolution.FAILED
		
	};	

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
	
	public FailureType[] getFailureTypes() {
		return FailureType.values();
	}	
	
	public ClinicalEvolution[] getClinicalEvolutions() {
		return clinicalEvolution;
	}
	
	public CaseState[] getCaseStates() {
		return caseStates;
	}
	
	public DrugResistanceType[] getDrugResistanceTypes() {
		return drugResistanceTypes;
	}
}
