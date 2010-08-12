package org.msh.tb.tbunits;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.UserWorkspace;

/**
 * Provide the list of TB Units to be used by {@link TBUnitSelection} class
 * @author Ricardo Memoria
 *
 */
@Name("tbunitSelectionList")
@Scope(ScopeType.EVENT)
public class TbunitSelectionList {

	@In EntityManager entityManager;
	
	private AdministrativeUnit adminUnit;
	private List<Tbunit> units;
	private HealthSystem healthSystem;

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
		if (healthSystem != null)
			healthSystemID = healthSystem.getId();
		else {
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
		
		units = entityManager.createQuery(hql)
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
	 * @return the healthSystem
	 */
	public HealthSystem getHealthSystem() {
		return healthSystem;
	}


	/**
	 * @param healthSystem the healthSystem to set
	 */
	public void setHealthSystem(HealthSystem healthSystem) {
		this.healthSystem = healthSystem;
	}


}
