package org.msh.mdrtb.entities.enums;

public enum HistologyResult {

	POSITIVE,
	NEGATIVE;
	
	public String getDescription() {
		if (this.equals(POSITIVE))
			 return "+";
		else return "-";
	}
}
