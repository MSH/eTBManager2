package org.msh.tb.adminunits;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.AdministrativeUnit;

/**
 * Maintain a list of objects related to an {@link AdministrativeUnit} object
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class AdminUnitGroup<E> {

	private AdministrativeUnit adminUnit;
	private List<E> items = new ArrayList<E>();

	/**
	 * @return the adminUnit
	 */
	public AdministrativeUnit getAdminUnit() {
		return adminUnit;
	}
	/**
	 * @param adminUnit the adminUnit to set
	 */
	public void setAdminUnit(AdministrativeUnit adminUnit) {
		this.adminUnit = adminUnit;
	}
	/**
	 * @return the items
	 */
	public List<E> getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(List<E> items) {
		this.items = items;
	}
	
}
