package org.msh.tb.ng.entities.enums;

public enum IntakeAntiDrugsDuration {
	LESS_4WEEKS,
    GREATER_4WEEKS;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
