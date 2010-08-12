package org.msh.mdrtb.entities.enums;

public enum LocalityType {

	URBAN,
	RURAL;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
