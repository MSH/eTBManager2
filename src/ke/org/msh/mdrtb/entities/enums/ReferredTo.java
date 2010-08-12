package org.msh.mdrtb.entities.enums;

public enum ReferredTo {
	NUTRITION_SUPPORT,
	VCT_CENTER,
	HIV_COMP_CARE_UNIT,
	STI_CLINIC,
	HOME_BASED_CARE, 
	ANTENATAL_CLINIC,
	PRIVATE_SECTOR,
	NOT_REFERRED;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
