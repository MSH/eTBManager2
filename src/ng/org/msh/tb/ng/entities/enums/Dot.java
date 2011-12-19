package org.msh.tb.ng.entities.enums;

public enum Dot {
	NN, 
	O,
	X;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
