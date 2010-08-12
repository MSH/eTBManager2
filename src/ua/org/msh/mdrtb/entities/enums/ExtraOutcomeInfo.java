package org.msh.mdrtb.entities.enums;

public enum ExtraOutcomeInfo {
	CULTURE_SMEAR,
	CULTURE,
	CLINICAL_EXAM,
	TB,
	OTHER_CAUSES,
	TRANSFER_CATIV;
	
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}

}
