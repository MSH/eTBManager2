package org.msh.mdrtb.entities.enums;

public enum Gender {
	MALE,
	FEMALE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
