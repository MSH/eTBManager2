package org.msh.tb.entities.enums;

public enum TreatmentCategory {
    INITIAL_REGIMEN_FIRST_LINE_DRUGS,
	RETREATMENT_FIRST_LINE_DRUGS,
	SECOND_LINE_TREATMENT_REGIMEN
	;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
