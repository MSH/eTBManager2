package org.msh.tb.ng.entities.enums;

public enum HealthFacility {
PUBLIC,
PRIVATE,
FBO;

	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
