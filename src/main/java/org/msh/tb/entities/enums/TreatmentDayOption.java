/**
 * 
 */
package org.msh.tb.entities.enums;

/**
 * Options for the registration of the treatment monitoring for a day of treatment
 * 
 * @author Ricardo Memoria
 *
 */
public enum TreatmentDayOption {

	NOT_TAKEN,
	DOTS,
	SELF_ADMIN;
	
	public String getLabel() {
		switch (this) {
		case DOTS: return "D";
		case NOT_TAKEN: return "N";
		case SELF_ADMIN: return "S";
		}
		return null;
	}
}
