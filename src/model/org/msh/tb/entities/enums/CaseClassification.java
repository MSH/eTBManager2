package org.msh.tb.entities.enums;

/**
 * Classification of the cases
 * @author Ricardo Memoria
 *
 */
public enum CaseClassification {
	TB,
	DRTB,
	NMT;


	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
