package org.msh.tb.tbunits;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.tb.MedicineUnitHome;
import org.msh.tb.RegimenUnitHome;
import org.msh.tb.login.UserSession;

@Name("unitSetup")
@Scope(ScopeType.CONVERSATION)
public class UnitSetup {

	@In(required=false) MedicineUnitHome medicineUnitHome;
	@In(required=false) RegimenUnitHome regimenUnitHome;
	@In(create=true) UserSession userSession;
	@In(create=true) EntityManager entityManager;
	
	private Tbunit unit;
	
	/**
	 * Get unit for editing
	 * @return
	 */
	public Tbunit getUnit() {
		if (unit == null)
			initialize();
		return unit;
	}
	
	/**
	 * Initialize unit stock setup
	 */
	public void initialize() {
		// check if it was already initialized
		if (unit != null)
			return;

		unit = entityManager.merge(userSession.getTbunit());
		userSession.setTbunit(unit);
	}


	/**
	 * Saves the information about stock unit settings
	 * @return
	 */
	public String persist() {
		entityManager.persist(unit);

		if (medicineUnitHome != null)
			medicineUnitHome.persist();
		
		if (regimenUnitHome != null)
			regimenUnitHome.persist();
		
		return "persisted";
	}
}
