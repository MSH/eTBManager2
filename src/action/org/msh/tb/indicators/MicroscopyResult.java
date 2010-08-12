package org.msh.tb.indicators;

public enum MicroscopyResult {

	NEGATIVE("SputumSmearResult.NEGATIVE"),
	POSITIVE("SputumSmearResult.POSITIVE");
	
	private String key;
	
	
	private MicroscopyResult(String key) {
		this.key = key;
	}



	public String getKey() {
		return key;
	}
}
