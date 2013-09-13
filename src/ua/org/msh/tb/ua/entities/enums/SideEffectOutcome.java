package org.msh.tb.ua.entities.enums;

public enum SideEffectOutcome {
	RESOLVED,
	RESOLVING,
	NOT_RESOLVED,
	IMPAIRMENT;

	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
