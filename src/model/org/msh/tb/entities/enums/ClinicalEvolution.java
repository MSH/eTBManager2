package org.msh.tb.entities.enums;

public enum ClinicalEvolution {

	FAVORABLE,
	UNCHANGED,
	UNFAVORABLE;
	
	public String getKey() {
		return "pt_BR." + getClass().getSimpleName().concat("." + name());
	}
}
