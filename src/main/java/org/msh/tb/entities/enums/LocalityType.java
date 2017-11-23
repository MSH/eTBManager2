package org.msh.tb.entities.enums;

public enum LocalityType {

	URBAN,
	RURAL;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
