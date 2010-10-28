package org.msh.mdrtb.entities.enums;

public enum PatientType {
	NEW,
	RELAPSE,
	AFTER_DEFAULT,
	FAILURE_FT,
	FAILURE_RT,
	OTHER,
// new types requested by Philippines
	FAILURE_CATI,
	FAILURE_CATII,
	FAILURE_CATIV, 
	RELAPSE_CATI,
	RELAPSE_CATII,
	RELAPSE_CATIV,
	TRANSFER_IN,
	OTHER_NONDOTS,
	OTHERPOS,
	OTHERNEG,
// new types used by Brazil
	SCHEMA_CHANGED,
// Ukraine
	FAILURE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}	
}
