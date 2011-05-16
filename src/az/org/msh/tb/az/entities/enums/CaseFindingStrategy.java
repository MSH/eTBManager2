package org.msh.tb.az.entities.enums;

public enum CaseFindingStrategy {

	ACTIVE,
	PASSIVE;
	
	public String getKey() {
		return "az_AZ." + getClass().getSimpleName() + "." + toString();
	}
}
