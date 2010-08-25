package org.msh.tb.br.entities.enums;

public enum FailureType {

	FIRST_TREATMENT,
	RETREATMENT;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
