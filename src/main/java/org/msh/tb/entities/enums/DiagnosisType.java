package org.msh.tb.entities.enums;

public enum DiagnosisType {

	SUSPECT,
	CONFIRMED;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
