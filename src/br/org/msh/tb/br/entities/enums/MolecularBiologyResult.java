package org.msh.tb.br.entities.enums;

public enum MolecularBiologyResult {

	NOTDONE,
	MTUBERCULOSIS,
	MICROBAC_NONTB,
	NEGATIVE;
	
	public String getMessage() {
		switch (this) {
		case NOTDONE: return "N�o realizada";
		case MTUBERCULOSIS: return "M. Tuberculosis";
		case MICROBAC_NONTB: return "Micobacteria n�o tuberculosa";
		case NEGATIVE: return "Negativa";
		default: return "?";
		}
	}
}
