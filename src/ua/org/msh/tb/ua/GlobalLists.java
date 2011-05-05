package org.msh.tb.ua;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.ua.entities.enums.DiagnosisSource;
import org.msh.tb.ua.entities.enums.HistologyResult;
import org.msh.tb.ua.entities.enums.MBTResult;

@Name("globalLists_ua")
public class GlobalLists {

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
		TbField.REGISTRATION_CATEGORY
	};
	
	private static final CaseState outcomes[] = {
		CaseState.CURED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.DIED,
		CaseState.FAILED,
		CaseState.TREATMENT_INTERRUPTION,
		CaseState.NOT_CONFIRMED,
		CaseState.TRANSFERRED_OUT
	};
	
	private static final ExtraOutcomeInfo ocCuredFailed[] = {
		ExtraOutcomeInfo.CULTURE_SMEAR,
		ExtraOutcomeInfo.CLINICAL_EXAM,
		ExtraOutcomeInfo.TRANSFER_CATIV
	};

	private static final ExtraOutcomeInfo ocDied[] = {
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
}
