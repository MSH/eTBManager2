package org.msh.tb.entities.enums;

public enum InfectionSite {
	PULMONARY,
	EXTRAPULMONARY,
	BOTH;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
