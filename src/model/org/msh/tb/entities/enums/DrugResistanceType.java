package org.msh.tb.entities.enums;

public enum DrugResistanceType {

	MONO_RESISTANCE,
	POLY_RESISTANCE,
	MULTIDRUG_RESISTANCE,
	EXTENSIVEDRUG_RESISTANCE,
	RIFAMPICIN_MONO_RESISTANCE,
	ISONIAZID_MONO_RESISTANCE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
