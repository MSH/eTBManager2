package org.msh.tb.ua.entities.enums;

public enum SideEffectGrading {
	MILD,
	MODERATE, 
	SEVERE,
	UNKNOWN;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
