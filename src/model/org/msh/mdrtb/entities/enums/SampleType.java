package org.msh.mdrtb.entities.enums;

public enum SampleType {

	SPUTUM,
	OTHER;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
