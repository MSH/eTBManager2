package org.msh.tb.entities.enums;

public enum ValidationState {

	WAITING_VALIDATION,
	VALIDATED,
	PENDING,
	PENDING_ANSWERED;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
