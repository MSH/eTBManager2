package org.msh.tb.ua.entities.enums;

public enum MBTResult {

	CULTURE,
	MICROSCOPY,
	BOTH;

	public String getKey() {
		switch (this) {
		case CULTURE: return "ExtraOutcomeInfo.CULTURE";
		case MICROSCOPY: return "ExtraOutcomeInfo.SMEAR";
		case BOTH: return "ExtraOutcomeInfo.CULTURE_SMEAR";
		}
		return toString();
	}
}
