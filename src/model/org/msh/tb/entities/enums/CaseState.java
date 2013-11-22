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
	REGIMEN_CHANGED,
	DIAGNOSTIC_CHANGED_TO_NOT_DRTB,
	DIAGNOSTIC_CHANGED_TO_MDR,
	DIAGNOSTIC_CHANGED_TO_MONO,
	DIAGNOSTIC_CHANGED_TO_POLY,
	DIAGNOSTIC_CHANGED_TO_XDR,
	
	//Cambodia
	NTM,
	SUSCEPTIBLE_TB,
	NOT_TB,
	
	//Vietnam
	TREATMENT_REFUSED;
	
	;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
