package org.msh.tb.bd.entities.enums;

public enum SideEffectSeriousness {
	NONE,
	HOSPITALIZED,
	DEAD,
	CONGENITAL_ANOMALY,
	DISABILITY,
	LIFE_THREATNING, 
	OTHER;


	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
