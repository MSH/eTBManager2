package org.msh.tb.bd.entities.enums;

public enum SmearStatus {
	SMEAR_POSITIVE,
	SMEAR_NEGATIVE,
	NOT_EVALUATED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
