package org.msh.tb.indicators.core;

/**
 * Return the output selection made by the user to generate an indicator
 * @author Ricardo Memoria
 *
 */
public enum OutputSelection {

	ADMINUNIT("AdministrativeUnit"),
	TBUNIT("TbCase.notificationUnit"),
	GENDER("Gender"),
	PATIENT_TYPE("PatientType"),
	INFECTION_SITE("InfectionSite"),
	NATIONALITY("Nationality"),
	AGERANGE("AgeRange"),
	PULMONARY("InfectionSite.PULMONARY"),
	EXTRAPULMONARY("InfectionSite.EXTRAPULMONARY"),
	REGIMENS("Regimen");

	private String key;
	
	private OutputSelection(String key) {
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
