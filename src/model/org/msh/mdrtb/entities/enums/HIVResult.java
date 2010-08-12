package org.msh.mdrtb.entities.enums;

public enum HIVResult {
	POSITIVE,
	NEGATIVE,
	ONGOING;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
