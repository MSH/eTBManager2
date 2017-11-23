package org.msh.tb.adminunits;

/**
 * Interface to receive notification about administrative unit changing
 * @author Ricardo Memoria
 *
 */
public interface AdminUnitChangeListener {

	public void notifyAdminUnitChange(AdminUnitSelection auselection);
}
