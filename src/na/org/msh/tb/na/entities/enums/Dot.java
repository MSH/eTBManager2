package org.msh.tb.na.entities.enums;

public enum Dot {
	D, 
	S,
	N;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
