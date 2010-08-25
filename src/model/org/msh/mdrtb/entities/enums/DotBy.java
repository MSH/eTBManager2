package org.msh.mdrtb.entities.enums;

public enum DotBy {
	HCW, 
	H,
	CHW,
	ND;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
