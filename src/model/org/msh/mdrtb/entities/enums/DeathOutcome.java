package org.msh.mdrtb.entities.enums;

public enum DeathOutcome {
	TB,
	OTHER_CAUSES;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
