package org.msh.mdrtb.entities.enums;

public enum MicroscopyResult {

	NEGATIVE,
	POSITIVE,
	PLUS,
	PLUS2,
	PLUS3,
	NOTDONE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
