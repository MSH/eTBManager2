package org.msh.tb.entities.enums;

public enum XRayBaseline {
	NORMAL,
	CAVITARY,
	INFILTRATE,
	OTHER,
	ONGOING;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
