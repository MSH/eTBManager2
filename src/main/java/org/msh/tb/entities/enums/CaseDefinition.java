package org.msh.tb.entities.enums;

public enum CaseDefinition {
    BACTERIOLOGICALLY_CONFIRMED,
    CLINICALLY_DIAGNOSED
	;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
