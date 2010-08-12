package org.msh.mdrtb.entities.enums;

public enum DiagnosisSource {
	CLINICAL,
	XRAY,
	LABTEST,
	OTHER;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
