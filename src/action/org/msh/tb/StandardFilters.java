/**
 * 
 */
package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.tbunits.TBUnitSelection;

/**
 * Define a set of the most common filters used in the system. This
 * filter is intended to be user in a EVENT scope (NO CONVERSATION)
 * @author Ricardo Memoria
 *
 */
@Name("standardFilters")
@BypassInterceptors
public class StandardFilters {

	private TBUnitSelection tbunitSelection;
	
	private AdminUnitSelection auSelection;
	
	private LaboratorySelection labSelection;
	
	
	/**
	 * Return the selected unit in the filter, or null
	 * @return instance of {@link Tbunit}
	 */
	public Tbunit getSelectedUnit() {
		return tbunitSelection != null ? tbunitSelection.getSelected(): null;
	}

	
	/**
	 * Return the selected administrative unit of the filter
	 * @return instance of {@link AdministrativeUnit}
	 */
	public AdministrativeUnit getSelectedAdminUnit() {
		return auSelection != null ? auSelection.getSelectedUnit(): null;
	}

	
	/**
	 * Return the selected laboratory
	 * @return instance of {@link Laboratory}
	 */
	public Laboratory getSelectedLaboratory() {
		return labSelection != null? labSelection.getSelected(): null;
	}

	
	/**
	 * @return the tbunitSelection
	 */
	public TBUnitSelection getTbunitSelection() {
		if (tbunitSelection == null)
			tbunitSelection = new TBUnitSelection("unitid");
		return tbunitSelection;
	}

	/**
	 * @return the auSelection
	 */
	public AdminUnitSelection getAuSelection() {
		if (auSelection == null)
			auSelection = new AdminUnitSelection();
		return auSelection;
	}

	/**
	 * @return the labSelection
	 */
	public LaboratorySelection getLabSelection() {
		if (labSelection == null)
			labSelection = new LaboratorySelection("labid");
		return labSelection;
	}
}
