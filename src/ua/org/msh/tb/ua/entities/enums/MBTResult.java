package org.msh.tb.ua.entities.enums;

public enum MBTResult {

	CULTURE,
	MICROSCOPY,
	BOTH;

	public String getKey() {
		switch (this) {
		case CULTURE: return "uk_UA.ExtraOutcomeInfo.CULTURE";
		case MICROSCOPY: return "uk_UA.ExtraOutcomeInfo.SMEAR";
		case BOTH: return "uk_UA.ExtraOutcomeInfo.CULTURE_SMEAR";
		}
		return toString();
	}
}
