package org.msh.mdrtb.entities.enums;

/**
 * X-Ray evolution options
 * @author Ricardo Memoria
 *
 */
public enum XRayEvolution {
	IMPROVED,
	PROGRESSED,
	STABLE;

	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
}
