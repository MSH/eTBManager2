package org.msh.tb.ge;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.Workspace;

public class RegionData extends ImportTableData{

	private Date lastIntDate;
	private CountryStructure structure;
	
	public RegionData(Date lastIntDate, CountryStructure structure) {
		super();
		this.lastIntDate = lastIntDate;
		this.structure = structure;
	}


	@Override
	public String getSql() {
		return "select * from region r where r.region_creationdate > '"+lastIntDate+"' or r.region_lastmodificationdate > '"+lastIntDate+"' " ;
	}



	/**
	 * Import a case
	 * @throws Exception
	 */
	@Override
	protected void importData(ResultSet rsCases, Workspace workspace, int noOfRecord, EntityManager entityManager) throws Exception {
		System.out.println("-------------------- inside import case ---------------");
		ArrayList<AdministrativeUnit> adminUnitLst = new ArrayList<AdministrativeUnit>();
		AdministrativeUnit adminUnit;
		
		while(rsCases.next()){
			adminUnit = new AdministrativeUnit();
			adminUnit.setLegacyId(rsCases.getString("region_id"));
			adminUnit.setCode(rsCases.getString("region_code"));
			adminUnit.getName().setName2(rsCases.getString("region_name"));
			adminUnit.getName().setName1(rsCases.getString("region_englishname"));
			adminUnit.setCountryStructure(structure);
			adminUnit.setWorkspace(workspace);
			adminUnitLst.add(adminUnit);
		}
		
		System.out.println("no of records fetched == "+adminUnitLst.size());
		noOfRecord = adminUnitLst.size();
		
		AdministrativeUnit adminUnitFromDb;
		for (AdministrativeUnit administrativeUnit : adminUnitLst) {
			try{
				adminUnitFromDb = (AdministrativeUnit) entityManager.createQuery("from AdministrativeUnit a where a.legacyId = :legacyId")
										.setParameter("legacyId", administrativeUnit.getLegacyId())
										.getSingleResult();
				administrativeUnit.setId(adminUnitFromDb.getId());
			}catch (NoResultException no){
				adminUnitFromDb = null;
			}
			entityManager.persist(administrativeUnit);
		}
		
	}

	
	
}
