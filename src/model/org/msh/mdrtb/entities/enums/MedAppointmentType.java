package org.msh.mdrtb.entities.enums;

public enum MedAppointmentType {

	SCHEDULLED,
	EXTRA;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
