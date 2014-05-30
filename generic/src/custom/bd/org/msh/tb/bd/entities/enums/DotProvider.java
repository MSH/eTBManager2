package org.msh.tb.bd.entities.enums;

public enum DotProvider {
	GOV_DOC, 
	GOV_FIELD_STAFF,
	PRIVATE_PRACT_NON_GRAD,
	PRIVATE_PRACT_GRAD,
	SHASTO,
	VLGE_DOC,
	COMM_VOLNTR,
	TEACHER,
	CURED_PATIENT,
	REL_PERSON,
	FAMILY_MEM,
	NURSE;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
