package org.msh.tb.bd.entities.enums;

public enum SideEffectOutcome {
	UNKNOWN, 
	RESOLVED,
	RESOLVING,
	SEQUEALE,
	NOT_RESOLVED,
	DEATH;

	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
