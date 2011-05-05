package org.msh.tb.entities.enums;

public enum DisplayCaseNumber {

	SYSTEM_GENERATED,
	REGISTRATION_CODE;

	public String getKey() {
		if (this == REGISTRATION_CODE) 
			 return "TbCase.registrationCode";
		else return getClass().getSimpleName().concat("." + name());
	}
}
