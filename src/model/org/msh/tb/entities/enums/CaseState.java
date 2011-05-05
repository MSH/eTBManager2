package org.msh.tb.entities.enums;

public enum CaseState {
	WAITING_TREATMENT,
	ONTREATMENT,
	TRANSFERRING,
	CURED,
	TREATMENT_COMPLETED,
	FAILED,
	DEFAULTED,
	DIED,
	TRANSFERRED_OUT,
	DIAGNOSTIC_CHANGED,
	OTHER,
	MDR_CASE,
	TREATMENT_INTERRUPTION,
	NOT_CONFIRMED, 
	DIED_NOTTB,
	REGIMEN_CHANGED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
