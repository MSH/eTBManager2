package org.msh.tb.entities.enums;

public enum GenexpertResult {
	
	INVALID,
	ERROR,
	NO_RESULT,
	ONGOING,
	TB_NOT_DETECTED,
	TB_DETECTED,
	RIF_DETECTED,
	RIF_NOT_DETECTED,
	RIF_INDETERMINATE
	;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
