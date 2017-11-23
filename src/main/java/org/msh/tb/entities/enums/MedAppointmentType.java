package org.msh.tb.entities.enums;

public enum MedAppointmentType {

	SCHEDULLED,
	EXTRA;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
