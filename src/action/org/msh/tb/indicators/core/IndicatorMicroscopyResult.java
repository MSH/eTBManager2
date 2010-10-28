package org.msh.tb.indicators.core;

public enum IndicatorMicroscopyResult {

	NEGATIVE("global.negative"),
	POSITIVE("global.positive");
	
	private String key;
	
	
	private IndicatorMicroscopyResult(String key) {
		this.key = key;
	}



	public String getKey() {
		return key;
	}
}
