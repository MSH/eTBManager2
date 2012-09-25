package org.msh.tb.vi;

public enum MtbDetected {

	YES,
	NO,
	ERROR,
	INTERMEDIATE;
	
	public String getKey() {
		return "vi." + getClass().getSimpleName() + "." + toString();
	}
}
