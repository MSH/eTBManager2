package org.msh.tb.entities.enums;

public enum DotBy {
	HCW, 
	H,
	CHW,
	ND;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
