package org.msh.tb.ua.entities.enums;

public enum RiskClassification {
	LOW,
	MEDIUM,
	HIGH;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
