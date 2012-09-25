package org.msh.tb.entities.enums;

public enum DstResult {
	NOTDONE,
	RESISTANT,
	SUSCEPTIBLE,
	CONTAMINATED,
	BASELINE,
	INTERMEDIATE,
	ERROR;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
