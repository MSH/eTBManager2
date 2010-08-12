package org.msh.mdrtb.entities.enums;

public enum CultureResult {
	NEGATIVE,
	POSITIVE,
	PLUS,
	PLUS2,
	PLUS3,
	PLUS4,
	CONTAMINATED,
	NOTDONE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
