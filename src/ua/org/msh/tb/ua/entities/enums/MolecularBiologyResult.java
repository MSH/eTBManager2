package org.msh.tb.ua.entities.enums;

import org.msh.tb.entities.enums.MessageKey;

public enum MolecularBiologyResult implements MessageKey {

	NOTDONE,
	MTUBERCULOSIS,
	MICROBAC_NONTB,
	NEGATIVE;
	
	public String getMessageKey() {
		return "uk_UA." + getClass().getSimpleName() + "." + toString();
	}
}
