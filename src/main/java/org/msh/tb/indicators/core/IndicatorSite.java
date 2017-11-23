package org.msh.tb.indicators.core;

public enum IndicatorSite {

	TREATMENTSITE,
	PATIENTADDRESS;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
