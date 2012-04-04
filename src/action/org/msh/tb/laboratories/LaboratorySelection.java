package org.msh.tb.laboratories;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitChangeListener;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Laboratory;

public class LaboratorySelection {

	private Laboratory laboratory;
	private AdminUnitSelection auselection;
	private List<Laboratory> options;

	private boolean applyUserRestrictions;
	private HealthSystem healthSystem;


	/**
	 * Set laboratory selected
	 * @param laboratory
	 */
	public void setLaboratory(Laboratory laboratory) {
		if (laboratory != null)
			getAuselection().setSelectedUnit(laboratory.getAdminUnit());
		this.laboratory = laboratory;
	}
	
	
	/**
	 * Get laboratory selected
	 * @return
	 */
	public Laboratory getLaboratory() {
		return laboratory;
	}


	/**
	 * Return list of laboratories to be selected by the user
	 * @return List of {@link Laboratory} instances
	 */
	public List<Laboratory> getOptions() {
		if (options == null)
			createOptions();
		return options;
	}

	
	/**
	 * Create list of laboratories
	 */
	protected void createOptions() {
		AdministrativeUnit adminUnit = getAuselection().getSelectedUnit();
		
		if (adminUnit == null)
			return;

		options = new ArrayList<Laboratory>();
		
		String hql = "from Laboratory lab where lab.adminUnit.code like :admincode and lab.workspace.id = #{defaultWorkspace.id}";
		if (healthSystem != null)
			hql += " and lab.healthSystem.id = " + healthSystem.getId().toString();
		options = getEntityManager().createQuery(hql)
					.setParameter("admincode",  adminUnit.getCode() + "%")
					.getResultList();
	}
	

	/**
	 * Return the name of the structure of the administrative unit (region, city, oblast, etc)
	 * @return
	 */
	public String getLabelAdminUnit() {
		return getAuselection().getLabelLevel1();
	}


	
	/**
	 * Return the SEAM component entityManager
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}

	/**
	 * Return component to select the administrative unit
	 * @return
	 */
	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			createAuselection();
		return auselection;
	}

	
	/**
	 * Checks if administrative unit level 1 is read only
	 * @return true if is read only, otherwise return false
	 */
	public boolean isLevel1ReadOnly() {
		return getAuselection().isLevel1ReadOnly();
	}


	/**
	 * Return the list of user administrative units related to the user restrictions
	 * @return list of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getAdminUnits() {
		return getAuselection().getOptionsLevel1();
	}

	protected void createAuselection() {
		auselection = new AdminUnitSelection(applyUserRestrictions);

		auselection.addListener(new AdminUnitChangeListener() {
			public void notifyAdminUnitChange(AdminUnitSelection auselection) {
				setLaboratory(null);
				options = null;
			}
		});
	}

	
	public AdministrativeUnit getAdminUnit() {
		return getAuselection().getUnitLevel1();
	}

	
	public void setAdminUnit(AdministrativeUnit admin) {
		getAuselection().setUnitLevel1(admin);
		laboratory = null;
		options = null;
	}
	
}
