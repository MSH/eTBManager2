package org.msh.tb.bd.entities.enums;

public enum BiopsyResult {
	TUBERCULUS,
	NON_TUBERCULUS,
	NOT_DONE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
