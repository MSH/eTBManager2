package org.msh.tb.kh.entities.enums;

public enum SideEffectGrading {
	MILD,
	MODERATE, 
	SEVERE,
	VERYSEVERE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
