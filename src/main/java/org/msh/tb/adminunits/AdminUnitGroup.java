package org.msh.tb.adminunits;

import org.msh.tb.entities.AdministrativeUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintain a list of objects related to an {@link AdministrativeUnit} object
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class AdminUnitGroup<E> {

	private AdministrativeUnit adminUnit;
	private Long casesOnTreatment;
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
	/**
	 * @return the casesOnTreatment
	 */
	public Long getCasesOnTreatment() {
		return casesOnTreatment;
	}
	/**
	 * @param casesOnTreatment the casesOnTreatment to set
	 */
	public void setCasesOnTreatment(Long casesOnTreatment) {
		this.casesOnTreatment = casesOnTreatment;
	}
	
}
