package org.msh.mdrtb.entities.enums;

public enum HIVResultKe {
	POSITIVE,
	NEGATIVE,
	ONGOING,
	DECLINED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
