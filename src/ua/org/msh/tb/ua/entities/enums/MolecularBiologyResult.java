package org.msh.tb.ua.entities.enums;

import org.msh.tb.entities.enums.MessageKey;

public enum MolecularBiologyResult implements MessageKey {

	GeneXpert,
	GenoTypeMTBDRplus,
	GenoTypeMTBDRsl;
	
	public String getMessageKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
