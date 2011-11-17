package org.msh.tb.entities.enums;

public enum PrevTBTreatmentOutcome {

	CURED,
	COMPLETED,
	FAILURE,
	DEFAULTED,
	SCHEME_CHANGED,
	TRANSFERRED_OUT,
	SHIFT_CATIV,
	UNKNOWN,
	ONGOING,
	DIAGNOSTIC_CHANGED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
	
	/**
	 * Convert from case outcome to previous TB case outcome
	 * @param state
	 * @return
	 */
	public static PrevTBTreatmentOutcome convertFromCaseState(CaseState state) {
		switch (state) {
		case CURED: 
			return PrevTBTreatmentOutcome.CURED;
		case DEFAULTED: 
			return DEFAULTED;
		case DIAGNOSTIC_CHANGED:
			return DIAGNOSTIC_CHANGED;
		case FAILED:
			return FAILURE;
		case MDR_CASE:
			return SHIFT_CATIV;
		case NOT_CONFIRMED:
			return UNKNOWN;
		case TREATMENT_COMPLETED:
			return COMPLETED;
		case TRANSFERRED_OUT:
			return TRANSFERRED_OUT;
		default:
			return PrevTBTreatmentOutcome.UNKNOWN;
		}
	}
}
