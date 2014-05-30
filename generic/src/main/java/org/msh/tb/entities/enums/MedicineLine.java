package org.msh.tb.entities.enums;

public enum MedicineLine {
	FIRST_LINE,
	SECOND_LINE,
	OTHER;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
