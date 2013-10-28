package org.msh.tb.ua.entities.enums;

public enum TreatmentType {
	HOSPITALISATION,
	OUTPATIENT;

	public String getKey() {
		return "uk_UA."+getClass().getSimpleName().concat("." + name());
	}
}
