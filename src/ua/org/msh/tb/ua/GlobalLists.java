package org.msh.tb.ua;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.ua.entities.enums.DiagnosisSource;
import org.msh.tb.ua.entities.enums.HistologyResult;
import org.msh.tb.ua.entities.enums.MBTResult;

@Name("globalLists_ua")
public class GlobalLists {

	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.RELAPSE,
		PatientType.AFTER_DEFAULT,
		PatientType.RESISTANCE_PATTERN_CHANGED,
		PatientType.FAILURE,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.TRANSFER_IN,
		PatientType.OTHER
	};
	
	private static final TbField tbfields[] = {
		TbField.TBDETECTION,
		TbField.DIAG_CONFIRMATION,
		TbField.POSITION,
		TbField.SIDEEFFECT,
		TbField.COMORBIDITY,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES,
		TbField.REGISTRATION_CATEGORY,
		TbField.ADJUSTMENT,
		TbField.CAUSE_OF_CHANGE,
		TbField.MOLECULARBIOLOGY_METHOD,
		TbField.MANUFACTURER
	};
	
	public static final CaseState outcomes[] = {
		CaseState.CURED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.DIED,
		CaseState.FAILED,
		CaseState.TREATMENT_INTERRUPTION,
		CaseState.NOT_CONFIRMED,
		CaseState.TRANSFERRED_OUT
	};
	
	public static final ExtraOutcomeInfo ocCuredFailed[] = {
		ExtraOutcomeInfo.CULTURE_SMEAR,
		ExtraOutcomeInfo.CLINICAL_EXAM,
		ExtraOutcomeInfo.TRANSFER_CATIV
	};

	public static final ExtraOutcomeInfo ocDied[] = {
		ExtraOutcomeInfo.TB,
		ExtraOutcomeInfo.OTHER_CAUSES
	};


	/**
	 * Returns list of TB fields available for drop down menus
	 * @return array of TbField enum
	 */
	@Factory("tbFields.ua")
	public TbField[] getTbFields() {
		return tbfields;
	}


	/**
	 * Return options for histology results
	 * @return
	 */
	public HistologyResult[] getHistologyResults() {
		return HistologyResult.values();
	}
	
	public MBTResult[] getMbtResults() {
		return MBTResult.values();
	}
	
	public ExtraOutcomeInfo[] getCuredFailedExtraInfo() {
		return ocCuredFailed;
	}
	
	public ExtraOutcomeInfo[] getDeathExtraInfo() {
		return ocDied;
	}
	
	public DiagnosisSource[] getDiagnosisSources() {
		return DiagnosisSource.values();
	}

	/**
	 * @return the outcomes
	 */
	public CaseState[] getOutcomes() {
		return outcomes;
	}


	/**
	 * @return the patienttypes
	 */
	public PatientType[] getPatientTypes() {
		return patientTypes;
	}
	
}
