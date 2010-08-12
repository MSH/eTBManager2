package org.msh.mdrtb.entities.enums;

public enum SusceptibilityResultTest {
	NOTDONE,
	RESISTANT,
	SUSCEPTIBLE,
	CONTAMINATED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
