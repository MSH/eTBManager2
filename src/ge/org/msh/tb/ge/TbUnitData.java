package org.msh.tb.ge;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;

public class TbUnitData extends ImportTableData{

	private Date lastIntDate;
	private HealthSystem healthSystem;
	
	public TbUnitData(Date lastIntDate, HealthSystem healthSystem) {
		super();
		this.lastIntDate = lastIntDate;
		this.healthSystem = healthSystem;
	}

	/**
	 * Import a case
	 * 
	 * @throws Exception
	 */
	protected void importData(ResultSet rsCases, Workspace workspace, int noOfRecord, EntityManager entityManager)
			throws Exception {
		System.out
				.println("-------------------- inside import case ---------------");
		ArrayList<Tbunit> tbUnitLst = new ArrayList<Tbunit>();
		Tbunit tbUnit;

		while (rsCases.next()) {
			tbUnit = new Tbunit();
			tbUnit.setLegacyId(rsCases.getString("tbfacility_id"));
			tbUnit.getName().setName2(rsCases.getString("tbfacility_name"));
			tbUnit.getName().setName1(
					rsCases.getString("tbfacility_englishname"));
			tbUnit.setActive(true);
			tbUnit.setChangeEstimatedQuantity(true);
			tbUnit.setHealthSystem(healthSystem);
			tbUnit.setMdrHealthUnit(true);
			tbUnit.setMedicineStorage(true);
			tbUnit.setNotifHealthUnit(true);
			tbUnit.setNumDaysOrder(90);
			tbUnit.setOrderOverMinimum(true);
			tbUnit.setPatientDispensing(true);
			tbUnit.setTbHealthUnit(true);
			tbUnit.setTreatmentHealthUnit(true);
			tbUnit.setWorkspace(workspace);
			tbUnit.setAdminUnit(getAdminUnit(rsCases, entityManager));
			tbUnit.setWorkspace(workspace);
			entityManager.persist(tbUnit);
			noOfRecord++;
		}

		System.out.println("no of records fetched == " + tbUnitLst.size());

	}

	private AdministrativeUnit getAdminUnit(ResultSet rsCases,
			EntityManager entityManager) throws SQLException {
		try {
			return (AdministrativeUnit) entityManager
					.createQuery(
							"from AdministrativeUnit a where a.legacyId = :legacyId")
					.setParameter("legacyId",
							 rsCases.getString("tbfacilityregion_id"))
					.getSingleResult();
			
		} catch (NoResultException no) {
			return null;
		}
	}

	@Override
	protected String getSql() {
		return "select * from tbfacility r where r.tbfacility_creationdate > '"
				+ lastIntDate + "' or r.tbfacility_lastmodificationdate > '"
				+ lastIntDate + "' ";
	}


}
