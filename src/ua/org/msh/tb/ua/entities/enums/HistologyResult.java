package org.msh.tb.ua.entities.enums;

public enum HistologyResult {

	POSITIVE,
	NEGATIVE;
	
	public String getDescription() {
		if (this.equals(POSITIVE))
			 return "+";
		else return "-";
	}
}
