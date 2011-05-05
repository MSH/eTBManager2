package org.msh.tb.entities.enums;

public enum SampleType {

	SPUTUM,
	OTHER;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
