package org.msh.tb.cases;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.adminunits.AdminUnitGroup;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;


@Name("healthUnitsQuery")
public class HealthUnitsQuery extends EntityQuery<HealthUnitInfo> {
	private static final long serialVersionUID = -1091248707546402475L;
	
	@In
	protected EntityManager entityManager;
	@In(create=true) Workspace defaultWorkspace;
	@In(create=true) UserSession userSession;
	@In(create=true)
	protected UserWorkspace userWorkspace;
	@In(create=true) CaseFilters caseFilters;
	
	private List<HealthUnitInfo> resultList;
	private List<AdminUnitGroup<HealthUnitInfo>> adminUnits;
	private List<AdministrativeUnit> adminsLevel1;


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.EntityQuery#createQuery()
	 */
	@Override
	protected Query createQuery() {
		String casecond = generateSQLConditionByCase();
		
		Integer hsID = null;
		if (userWorkspace.getHealthSystem() != null)
			hsID = userWorkspace.getHealthSystem().getId();
		
		String sql = "select u.id, u.name1, a.code, " +
				"(select count(*) from tbcase c where c.notification_unit_id = u.id and c.state <= 2 " + casecond + ") numcases, " +
				
				"(select count(*) from tbcase c " +
				"where c.state=1 and c.owner_unit_id = u.id " + casecond + ") as ontreat, " +
				
				"(select count(*) from treatmenthealthunit t inner join tbcase c on c.id = t.case_id " +
				"where t.inidate > c.initreatmentdate and t.enddate = c.endtreatmentdate and c.state=1 " +
				"and t.transferring=false and t.unit_id = u.id" + casecond + ") as transferin, " +
				
				"(select count(*) from treatmenthealthunit t inner join tbcase c on c.id = t.case_id " +
				"where t.enddate < c.endtreatmentdate and c.state in (1,2) and t.unit_id = u.id" + casecond + ") as transferout, " +
				
				"(select count(*) from tbcase c " +
				"where c.state=0 and c.owner_unit_id = u.id " + casecond + ") as notontreat " +

				"from tbunit u inner join administrativeunit a on a.id = u.adminunit_id " +
				"where u.workspace_id = " + defaultWorkspace.getId().toString() + generateSQLConditionByUserView() +
				(hsID != null? " and u.healthsystem_id = " + hsID: "") + 
				" group by u.id, u.name1, a.code having numcases > 0 or ontreat > 0 or transferin > 0 or transferout > 0 order by a.code, u.name1";
		
		Query query = entityManager.createNativeQuery(sql);
		if ( getFirstResult()!=null) query.setFirstResult( getFirstResult() );
	    if ( getMaxResults()!=null) query.setMaxResults( getMaxResults()+1 ); //add one, so we can tell if there is another page
		
		return query;
	}
	

	/**
	 * Generate SQL condition by user view
	 * @return SQL condition to be used in a WHERE clause
	 */
	public String generateSQLConditionByUserView() {
		if (userWorkspace.getView() == null)
			return "";
		
		String cond;
		
		switch (userWorkspace.getView()) {
		case ADMINUNIT: 
				String code = userWorkspace.getAdminUnit().getCode();
				cond = " and (a.code like '" + code + "%')"; 
			break;
		case TBUNIT: cond = " and u.id = " + userWorkspace.getTbunit().getId();
			break;
		default: cond = "";
		}
		
		return cond;
	}


	/**
	 * Generate SQL condition to filter cases
	 * @return SQL condition to be used in a where clause
	 */
	protected String generateSQLConditionByCase() {
		//Classifications selected
		List<CaseClassification> classifs = caseFilters.getClassifications().getSelectedItems();
		String caseCondition = "";
		String clasSel = "";
		String diagTypesSel = "";
		
		if (classifs.size() == 0)
			return "";
		else{
			for (CaseClassification cla: classifs) {
				if (!clasSel.isEmpty())
					clasSel += ", ";
				clasSel += cla.ordinal();
			}
		}
		
		//diagnosis types selected
		List<DiagnosisType> diagTypes = caseFilters.getDiagnosisTypes().getSelectedItems();

		if (diagTypes.size() == 0)
			return "";
		else{
			for (DiagnosisType dt: diagTypes) {
				if (!diagTypesSel.isEmpty())
					diagTypesSel += ", ";
				diagTypesSel += dt.ordinal();
			}
		}

		if(!clasSel.isEmpty())
			caseCondition = " and (c.classification in (" + clasSel + "))";
		
		if(!diagTypesSel.isEmpty())
			caseCondition = caseCondition + " and (c.diagnosisType in (" + diagTypesSel + "))";
		
		return caseCondition;
	}

	
	@Override
	public String getCountEjbql() {
		return "select count(*) from tbunit u where u.workspace.id = #{defaultWorkspace.id}";
	}
	
	@Override
	public String getEjbql() {
		// just to avoid "validate" exception
		return "from tbunit u where u.workspace.id = #{defaultWorkspace.id}";
	}
	
	
	@Override
	public List<HealthUnitInfo> getResultList() {
		if (resultList == null)
		{
			resultList = createResultList();
	    }
		return resultList;
	}

	
	@Override
	public Integer getMaxResults() {
		return null;
	}

	
	/**
	 * Create the result to be exposed by the component
	 * @return
	 */
	public List<HealthUnitInfo> createResultList() {
        javax.persistence.Query query = createQuery();
        List<Object[]> lst = query==null ? null : query.getResultList();
        return fillResultList(lst);
	}


	/**
	 * Create the result list from the resultset of the query
	 * @param lst
	 * @return
	 */
	protected List<HealthUnitInfo> fillResultList(List<Object[]> lst) {
		List<HealthUnitInfo> res = new ArrayList<HealthUnitInfo>();
		
		for (Object[] vals: lst) {
			HealthUnitInfo info = new HealthUnitInfo();
			info.setUnitId((Integer)vals[0]);
			info.setUnitName((String)vals[1]);
			info.setAdminUnitCode((String)vals[2]);
			
			info.setCasesNotifs(readLongValue(vals[3]));
			info.setCasesOnTreatment(readLongValue(vals[4]));
			info.setCasesTransferIn(readLongValue(vals[5]));
			info.setCasesTransferOut(readLongValue(vals[6]));
			info.setCasesNotOnTreatment(readLongValue(vals[7]));

			res.add(info);
		}
		return res;
	}


	protected Long readLongValue(Object val) {
		if (val == null)
			return null;
		
		Long longval = ((BigInteger)val).longValue();
		if (longval == 0)
			 return null;
		else return longval;
	}


	@Override
	@Transactional
	public boolean isNextExists()
	{
		boolean b = (resultList!=null) && (resultList.size() > getMaxResults());
		return b;
	}
	
	@Override
	public void refresh() {
		resultList = null;
		
		super.refresh();
	}


	protected void createAdminUnits() {
		adminUnits = new ArrayList<AdminUnitGroup<HealthUnitInfo>>();
		for (HealthUnitInfo info: getResultList()) {
			AdminUnitGroup<HealthUnitInfo> adminUnitGroup = findAdminUnitGroup(info.getAdminUnitCode());
			adminUnitGroup.getItems().add(info);
			
			if(adminUnitGroup.getCasesOnTreatment() == null)
				adminUnitGroup.setCasesOnTreatment(new Long(0));
			
			if(info.getCasesOnTreatment() != null)
				adminUnitGroup.setCasesOnTreatment(adminUnitGroup.getCasesOnTreatment() + info.getCasesOnTreatment());
		}
	}
	
	
	protected AdminUnitGroup<HealthUnitInfo> findAdminUnitGroup(String adminUnitCode) {
		for (AdminUnitGroup<HealthUnitInfo> adm: adminUnits) {
			if (adm.getAdminUnit().isSameOrChildCode(adminUnitCode)) {
				return adm;
			}
		}
		
		AdminUnitGroup<HealthUnitInfo> adm = new AdminUnitGroup<HealthUnitInfo>();
		AdministrativeUnit aux = findAdminUnitByCode(adminUnitCode);
		if (aux == null)
			throw new RuntimeException("Admin unit parent of code " + adminUnitCode + " not found");
		adm.setAdminUnit( aux );
		adminUnits.add(adm);
		
		return adm;
	}
	
	/**
	 * Search for an administrative level 1 unit by its code or a code of a child 
	 * @param code
	 * @return
	 */
	private AdministrativeUnit findAdminUnitByCode(String code) {
		// if user just "see" an administrative unit, just return it
		if (userWorkspace.getView() == UserView.ADMINUNIT)
			return userWorkspace.getAdminUnit();
		
		// get list of administrative units of first level
		if (adminsLevel1 == null)
			adminsLevel1 = caseFilters.getAuselection().getOptionsLevel1();
		
		for (AdministrativeUnit adm: adminsLevel1) {
			if (adm.isSameOrChildCode(code)) {
				return adm;
			}
		}
		return null;
	}
	
	/**
	 * @return the adminUnits
	 */
	public List<AdminUnitGroup<HealthUnitInfo>> getAdminUnits() {
		if (adminUnits == null)
			createAdminUnits();
		return adminUnits;
	}
	
}
