package org.msh.mdrtb.entities.enums;

public enum XRayBaseline {
	NORMAL,
	CAVITARY,
	INFILTRATE,
	OTHER;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
