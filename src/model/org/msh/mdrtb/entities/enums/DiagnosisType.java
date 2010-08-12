package org.msh.mdrtb.entities.enums;

public enum DiagnosisType {

	SUSPECT,
	CONFIRMED;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
