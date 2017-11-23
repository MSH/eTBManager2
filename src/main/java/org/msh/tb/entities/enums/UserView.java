package org.msh.tb.entities.enums;

public enum UserView {
	COUNTRY,
	ADMINUNIT,
	TBUNIT;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
	
}
