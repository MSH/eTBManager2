package org.msh.tb.entities.enums;

public enum ForecastNewCaseFreq {

	MONTH,
	QUARTER,
	SEMESTER,
	YEAR;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
