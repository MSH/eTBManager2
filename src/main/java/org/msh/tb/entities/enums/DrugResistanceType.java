package org.msh.tb.entities.enums;

public enum DrugResistanceType {

	MONO_RESISTANCE,
	POLY_RESISTANCE,
	MULTIDRUG_RESISTANCE,
	EXTENSIVEDRUG_RESISTANCE,
	RIFAMPICIN_MONO_RESISTANCE,
	ISONIAZID_MONO_RESISTANCE,
	//Bangladesh
	UNKNOWN,
	//Cambodia
	RIF_RESISTANCE,
    // New WHO Recommendations 05-14-2015
    MONO_RESISTANCE_RIF,
    POLY_RESISTANCE_RIF,
	;
	

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
