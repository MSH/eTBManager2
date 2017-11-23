package org.msh.tb.tbunits;

import org.msh.tb.entities.Tbunit;

/**
 * Interface all class must implement to receive notifications from {@link TBUnitSelection} component
 * when user selects a {@link Tbunit} entity 
 * @author Ricardo Memoria
 *
 */
public interface TbunitChangeListener {

	/**
	 * Called by AJAX when user selects a TB Unit
	 * @param tbunitSelection
	 */
	void notifyTbunitChange(TBUnitSelection tbunitSelection);
}
