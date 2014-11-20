package org.msh.tb.indicators.core;

public enum IndicatorDate {

	REGISTRATION_DATE("TbCase.registrationDate"),
	DIAGNOSIS_DATE("TbCase.diagnosisDate"),
	INITREATMENT_DATE("TbCase.iniTreatmentDate");

	private String key;

	
	private IndicatorDate(String key) {
		this.key = key;
	}


	public String getKey() {
		return key;
	}
}
