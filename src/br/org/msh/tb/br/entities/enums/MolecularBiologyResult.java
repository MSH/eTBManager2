package org.msh.tb.br.entities.enums;

import org.msh.tb.entities.enums.MessageKey;

public enum MolecularBiologyResult implements MessageKey {

	NOTDONE,
	MTUBERCULOSIS,
	MICROBAC_NONTB,
	NEGATIVE;
	
	public String getMessageKey() {
		return "pt_BR." + getClass().getSimpleName() + "." + toString();
	}
}
