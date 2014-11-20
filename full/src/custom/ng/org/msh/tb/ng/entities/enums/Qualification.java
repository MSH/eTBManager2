package org.msh.tb.ng.entities.enums;

public enum Qualification {
	DOC_CONSULTANT,
	DOC_MO,
	NURSE,
	OTHER;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
