package org.msh.mdrtb.entities.enums;

public enum Nationality {

	NATIVE,
	FOREIGN;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
