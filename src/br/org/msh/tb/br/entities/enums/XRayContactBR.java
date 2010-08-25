package org.msh.tb.br.entities.enums;

/**
 * XRay options used in the Brazilian version
 * @author Ricardo Memoria
 *
 */
public enum XRayContactBR {

	NOT_DONE,
	NORMAL,
	SUGGESTIVE,
	SEQUEL,
	OTHER_DISEASES;
	
	public String getKey() {
		return "br." + getClass().getSimpleName() + "." + toString();
	}
}
