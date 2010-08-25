package org.msh.tb.br.entities.enums;

/**
 * Type of resistance. Exclusive for Brazil
 * @author Ricardo Memoria
 *
 */
public enum TipoResistencia {

	PRIMARIA,
	ADQUIRIDA,
	NAO_SE_APLICA;
	
	public String getMessage() {
		switch (this) {
		case PRIMARIA: return "Prim�ria";
		case ADQUIRIDA: return "Adquirida";
		case NAO_SE_APLICA: return "N�o se aplica";
		default: return "?";
		}
	}
	
}
