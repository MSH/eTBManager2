package org.msh.tb.entities.enums;

public enum Gender {
	MALE,
	FEMALE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

	public String getAbbrev() {
		return this.equals(Gender.MALE) ? "M" : "F";
	}
}
