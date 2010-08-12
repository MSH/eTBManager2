package org.msh.mdrtb.entities.enums;

public enum PrevTBTreatmentOutcome {

	CURED,
	COMPLETED,
	FAILURE,
	DEFAULTED,
	SCHEME_CHANGED,
	TRANSFERRED_OUT,
	SHIFT_CATIV,
	UNKNOWN;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
