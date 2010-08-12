package org.msh.mdrtb.entities;

/**
 * Interface all entity must implement to control its state (active or inactive)
 * @author Ricardo Memoria
 *
 */
public interface EntityState {

	/**
	 * Return true if entity is active, otherwise false if the entity is inactive
	 * @return
	 */
	boolean isActive();

	/**
	 * Change entity state
	 * @param newState
	 */
	void setActive(boolean newState);
}
