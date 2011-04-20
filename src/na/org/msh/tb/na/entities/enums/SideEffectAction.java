package org.msh.tb.na.entities.enums;

public enum SideEffectAction {
	NONE,
	DISCONTINUED, 
	REDUCED,
	SWITCH,
	RE_CHALLENGE,
	OTHER;

	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
