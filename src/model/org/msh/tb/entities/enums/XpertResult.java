package org.msh.tb.entities.enums;

public enum XpertResult {
	
	INVALID,
	ERROR,
	NO_RESULT,
	ONGOING,
	TB_NOT_DETECTED,
	TB_DETECTED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
