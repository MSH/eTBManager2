package org.msh.tb.cases;

/**
 * Filter types for health units in case searching 
 * @author Ricardo Memoria
 *
 */
public enum FilterHealthUnit {

	NOTIFICATION_UNIT,
	TREATMENT_UNIT,
	BOTH;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
