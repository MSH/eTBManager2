package org.msh.entities.enums;

public enum FailureType {

	FIRST_TREATMENT,
	RETREATMENT;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
