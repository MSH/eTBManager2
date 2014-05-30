package org.msh.tb.entities.enums;

public enum RegimenPhase {
	INTENSIVE,
	CONTINUOUS;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}	
}
