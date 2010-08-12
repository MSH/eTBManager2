package org.msh.mdrtb.entities.enums;

public enum DrugResistanceType {

	MONO_RESISTANCE,
	POLY_RESISTANCE,
	MULTIDRUG_RESISTANCE,
	EXTENSIVEDRUG_RESISTANCE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
