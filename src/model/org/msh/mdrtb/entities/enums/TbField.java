package org.msh.mdrtb.entities.enums;


/**
 * Field 
 * @author Ricardo
 *
 */
public enum TbField {

	TBDETECTION,
	DIAG_CONFIRMATION,
	POSITION,
	SIDEEFFECT,
	COMORBIDITY,
	PHYSICAL_EXAMS,
	DST_METHOD,
	CULTURE_METHOD,
	SMEAR_METHOD,
	SYMPTOMS,
	CONTACTTYPE,
	CONTACTCONDUCT,
	XRAYPRESENTATION,
	PULMONARY_TYPES,
	EXTRAPULMONARY_TYPES,
	// Brazilian custom variables
	SKINCOLOR,PREGNANCE_PERIOD,EDUCATIONAL_DEGREE,CONTAG_PLACE, SCHEMA_TYPES, 
	RESISTANCE_TYPES, MICROBACTERIOSE, MOLECULARBIOLOGY_METHOD,
	// ukraine
	REGISTRATION_CATEGORY,
	// Bangladesh
	BIOPSY_METHOD
	;
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
