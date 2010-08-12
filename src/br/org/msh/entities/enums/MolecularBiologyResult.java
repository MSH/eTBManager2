package org.msh.entities.enums;

public enum MolecularBiologyResult {

	NOTDONE,
	MTUBERCULOSIS,
	MICROBAC_NONTB,
	NEGATIVE;
	
	public String getMessage() {
		switch (this) {
		case NOTDONE: return "Não realizada";
		case MTUBERCULOSIS: return "M. Tuberculosis";
		case MICROBAC_NONTB: return "Micobacteria não tuberculosa";
		case NEGATIVE: return "Negativa";
		default: return "?";
		}
	}
}
