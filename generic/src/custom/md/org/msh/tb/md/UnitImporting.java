package org.msh.tb.md;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.w3c.dom.Element;

public class UnitImporting extends ImportingBase {

	private EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");


	/* (non-Javadoc)
	 * @see org.msh.tb.md.ImportingBase#importRecord(org.w3c.dom.Element)
	 */
	@Override
	protected boolean importRecord(Element xmlLocData) {
		String id = getValue(xmlLocData, "second_id_in_table", true);
		String unitName = getValue(xmlLocData, "UNIT_NAME_RO", true);
		String unitNameRu = getValue(xmlLocData, "UNIT_NAME_RU", false);
		String regStr = getValue(xmlLocData, "REGION_ID", true);

		if (isErrorOnCurrentImport())
			return false;
		
		Tbunit unit = loadTBUnit(id, unitName);
		boolean result = unit == null;
		if (unit == null)
			unit = new Tbunit();
		
		unit.getName().setName1(unitName);
		unit.getName().setName2(unitNameRu);
		unit.setLegacyId(id);
		unit.setBatchControl(true);
		unit.setChangeEstimatedQuantity(true);
		unit.setMedicineStorage(true);
		unit.setNumDaysOrder(120);
		unit.setTreatmentHealthUnit(true);
		unit.setWorkspace(getWorkspace());
		unit.setAdminUnit(loadAdminUnit(regStr));
		unit.setHealthSystem(getHealthSystem());
		entityManager.persist(unit);
		entityManager.flush();
		
		return result;
	}


	/**
	 * Return the Health System in use in Moldova
	 * @return {@link HealthSystem} instance
	 */
	protected HealthSystem getHealthSystem() {
		return (HealthSystem)entityManager.merge(getConfig().getDefaultHealthSystem());
	}
	
	/**
	 * Load the administrative unit with the corresponding legacy ID. If the record is not found,
	 * the default administrative unit is loaded
	 * @param legacyId
	 * @return
	 */
	protected AdministrativeUnit loadAdminUnit(String legacyId) {
		try {
			return (AdministrativeUnit)entityManager
			.createQuery("from AdministrativeUnit adm where adm.legacyId = :id and adm.workspace.id = :wsid")
			.setParameter("id", legacyId)
			.setParameter("wsid", getWorkspace().getId())
			.getSingleResult();
			
		} catch (Exception e) {
//			System.out.println("Administrative Unit not found. ID = " + legacyId);
		}
		
		return entityManager.merge(getConfig().getDefaultAdminUnit());
	}

	
	/**
	 * Load the health system with the corresponding legacy ID. If the record is not found,
	 * the default health system is loaded
	 * @param legacyId
	 * @return
	 */
	protected HealthSystem loadHealthSystem(String legacyId) {
		try {
			return (HealthSystem)entityManager
			.createQuery("from HealthSystem hs where hs.legacyId = :id and hs.workspace.id=" + getWorkspace().getId().toString())
			.setParameter("id", legacyId)
			.getSingleResult();
			
		} catch (Exception e) {
//			System.out.println("Health System not found. ID = " + legacyId);
		}
		
		return entityManager.merge(getConfig().getDefaultHealthSystem());		
	}


	
	/**
	 * Load the TB Unit with the corresponding legacy ID. If the record is not found,
	 * the default health system is loaded
	 * @param legacyId
	 * @return
	 */
	protected Tbunit loadTBUnit(String legacyId, String name) {
		String hql = "from Tbunit unit where unit.workspace.id = " + getWorkspace().getId().toString(); 

		String restriction = "unit.legacyId = :id";
		if (name != null)
			restriction = "(" + restriction + " or unit.name.name1 = '" + name + "')";
		
		hql += " and " + restriction;
		
		List<Tbunit> lst = entityManager
			.createQuery(hql)
			.setParameter("id", legacyId)
			.getResultList();
			
		return (lst.size() > 0? lst.get(0): null);		
	}


}
