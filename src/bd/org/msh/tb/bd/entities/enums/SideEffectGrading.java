package org.msh.tb.bd.entities.enums;

public enum SideEffectGrading {
	MILD,
	MODERATE, 
	SEVERE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
