package org.msh.tb.bd.entities.enums;

public enum BiopsyResult {
	POSITIVE,
	NEGATIVE,
	NOT_DONE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
