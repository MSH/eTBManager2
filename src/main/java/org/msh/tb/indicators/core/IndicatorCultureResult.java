package org.msh.tb.indicators.core;

/**
 * Possible results of culture exams used by indicators 
 * @author Ricardo Memoria
 *
 */
public enum IndicatorCultureResult {

	NEGATIVE("global.negative"),
	POSITIVE("global.positive");

	private String key;
	
	private IndicatorCultureResult(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
