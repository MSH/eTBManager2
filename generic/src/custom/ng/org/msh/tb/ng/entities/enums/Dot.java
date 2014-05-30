package org.msh.tb.ng.entities.enums;

public enum Dot {
	N, 
	X,
	O;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
