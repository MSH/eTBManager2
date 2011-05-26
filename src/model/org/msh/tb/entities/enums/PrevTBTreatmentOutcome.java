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
}
