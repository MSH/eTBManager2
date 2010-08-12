package org.msh.tb.indicators.core;

/**
 * Return the output selection made by the user to generate an indicator
 * @author Ricardo Memoria
 *
 */
public enum OutputSelection {

	ADMINUNIT("AdministrativeUnit"),
	TBUNIT("cases.details.notifhu"),
	GENDER("Gender"),
	PATIENT_TYPE("PatientType"),
	INFECTION_SITE("InfectionSite"),
	NATIONALITY("Nationality"),
	AGERANGE("AgeRange"),
	PULMONARY("InfectionSite.PULMONARY"),
	EXTRAPULMONARY("InfectionSite.EXTRAPULMONARY");

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
	
	/**
	 * Return the field associated to the output selection
	 * @return String containing the HQL field associated to the output selection filter
	 */
	public String getField() {
		switch (this) {
		case GENDER: return "c.patient.gender";
		case INFECTION_SITE: return "c.infectionSite";
		case PATIENT_TYPE: return "c.patientType";
		case ADMINUNIT: return "c.notificationUnit.adminUnit.code";
		case TBUNIT: return "c.notificationUnit.name.name1";
		case NATIONALITY: return "c.nationality";
		case AGERANGE: return "c.age";
		case PULMONARY: return "c.pulmonaryType.name";
		case EXTRAPULMONARY: return "c.extrapulmonaryType.name";
		}
		return null;
	}
}
