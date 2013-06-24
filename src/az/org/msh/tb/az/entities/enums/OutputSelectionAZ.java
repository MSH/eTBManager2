package org.msh.tb.az.entities.enums;

/**
 * Return the output selection made by the user to generate an indicator
 * @author Ricardo Memoria
 *
 */
public enum OutputSelectionAZ {

	CLASSIFICATION("CaseClassification"),
	ADMINUNIT("AdministrativeUnit"),
	TBUNIT("az_AZ.custonInd.notifUnit"),
	OWNER_UNIT("az_AZ.custonInd.ownerUnit"),
	REAL_UNIT("TbCase.realUnit"),
	GENDER("Gender"),
	PATIENT_TYPE("PatientType"),
	INFECTION_SITE("InfectionSite"),
	NATIONALITY("Nationality"),
	AGERANGE("AgeRange"),
	PULMONARY("InfectionSite.PULMONARY"),
	EXTRAPULMONARY("InfectionSite.EXTRAPULMONARY"),
	REGIMENS("Regimen"),
	VALIDATION_STATE("ValidationState"),
	MICROSCOPY_RESULT("MicroscopyResult"),
	CULTURE_RESULT("CultureResult"),
	DIAGNOSIS_TYPE("DiagnosisType"),
	HIV_RESULT("HIVResult"),
	//SOURCE_MED("Source"),
	DRUG_RESIST_TYPE("DrugResistanceType"),
	YEAR("global.yearBig"),
	YEAR_MONTH("global.monthyear"),
	YEAR_WEEK("global.weekyear"),
	HEALTH_SYSTEM("HealthSystem"),
	CREATOR_USER("User");

	private String key;
	
	private OutputSelectionAZ(String key) {
		this.key = key;
	}


	/**
	 * Return the key in the message file to display the enumeration in the user language
	 * @return String containing the message key
	 */
	public String getKey() {
		return key;
	}
	
}
