package org.msh.mdrtb.entities.enums;

public enum ValidationState {

	WAITING_VALIDATION,
	VALIDATED,
	PENDING,
	PENDING_ANSWERED;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
