package org.msh.tb.tbunits;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;

/**
 * Provide the list of TB Units to be used by {@link TBUnitSelection} class
 * @author Ricardo Memoria
 *
 */
public class TbunitSelectionList {

	private AdministrativeUnit adminUnit;
	private List<Tbunit> units;
	private boolean applyHealthSystemRestrictions;

	private String restriction;

	/**
	 * Return the list of TB Units
	 * @return
	 */
	public List<Tbunit> getUnits() {
		if (units == null)
			createUnits();
		return units;
	}

	
	/**
	 * Create the list of TB units
	 */
	protected void createUnits() {
		if (adminUnit == null)
			return;
		
		// add dynamic condition by health system
		Integer healthSystemID = null;
		if (applyHealthSystemRestrictions) {
			UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
			if (userWorkspace.getHealthSystem() != null)
				healthSystemID = userWorkspace.getHealthSystem().getId();
		}
		String cond;
		if (healthSystemID != null)
			 cond = "and u.healthSystem.id = " + healthSystemID.toString();
		else cond = null;
		
		String hql = "select u from Tbunit u " +
				"where u.adminUnit.code like :code " +
				"and u.workspace.id = #{defaultWorkspace.id} " +
				"and u.active = :active " +
				(cond != null? cond: ""); 

		if ((restriction != null) && (restriction.length() > 0))
			hql = hql.concat(" and " + restriction);
		
		hql = hql + " order by u.name.name1";

		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		units = em.createQuery(hql)
				.setParameter("code", adminUnit.getCode() + "%")
				.setParameter("active", true)
				.getResultList();
	}


	/**
	 * @param adminUnit the adminUnit to set
	 */
	public void setAdminUnit(AdministrativeUnit adminUnit) {
		if (adminUnit == this.adminUnit)
			return;
		
		this.adminUnit = adminUnit;
		units = null;
	}


	/**
	 * @return the adminUnit
	 */
	public AdministrativeUnit getAdminUnit() {
		return adminUnit;
	}


	/**
	 * @return the restriction
	 */
	public String getRestriction() {
		return restriction;
	}


	/**
	 * @param restriction the restriction to set
	 */
	public void setRestriction(String restriction) {
		this.restriction = restriction;
		units = null;
	}


	/**
	 * @return the applyHealthSystemRestrictions
	 */
	public boolean isApplyHealthSystemRestrictions() {
		return applyHealthSystemRestrictions;
	}


	/**
	 * @param applyHealthSystemRestrictions the applyHealthSystemRestrictions to set
	 */
	public void setApplyHealthSystemRestrictions(
			boolean applyHealthSystemRestrictions) {
		this.applyHealthSystemRestrictions = applyHealthSystemRestrictions;
	}


}
