package org.msh.tb.entities.enums;

public enum SecDrugsReceived {
	YES,
	NO,
    UNKNOWN;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
