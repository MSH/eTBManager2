package org.msh.tb.cases;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.tb.adminunits.AdminUnitGroup;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;


@Name("healthUnitsQuery")
public class HealthUnitsQuery extends EntityQuery<HealthUnitInfo> {
	private static final long serialVersionUID = -1091248707546402475L;
	
	@In EntityManager entityManager;
	@In Workspace defaultWorkspace;
	@In(create=true) UserSession userSession;
	@In(create=true) UserWorkspace userWorkspace;
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
				"(select count(*) from TbCase c where c.notification_unit_id = u.id and c.state <= 2 " + casecond + ") numcases, " +
				"(select count(*) from TreatmentHealthUnit t inner join TbCase c on c.id = t.case_id " +
				"where t.enddate = c.endtreatmentdate and c.state=1 and t.unit_id = u.id " + casecond + ") as ontreat, " +
				"(select count(*) from TreatmentHealthUnit t inner join TbCase c on c.id = t.case_id " +
				"where t.inidate > c.initreatmentdate and t.enddate = c.endtreatmentdate and c.state=1 " +
				"and t.transferring=false and t.unit_id = u.id" + casecond + ") as transferin, " +
				"(select count(*) from TreatmentHealthUnit t inner join TbCase c on c.id = t.case_id " +
				"where t.enddate < c.endtreatmentdate and c.state in (1,2) and t.unit_id = u.id" + casecond + ") as transferout " +
				"from Tbunit u inner join AdministrativeUnit a on a.id = u.adminunit_id " +
				"where u.workspace_id = " + defaultWorkspace.getId().toString() + generateSQLConditionByUserView() +
				(hsID != null? " and u.healthsystem_id = " + hsID: "") + 
				" group by u.id, u.name1, a.code having numcases > 0 or ontreat > 0  order by a.code, u.name1";
		
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
		List<CaseClassification> classifs = caseFilters.getClassifications().getSelectedItems();

		if (classifs.size() == 0)
			return "";
		
		String caseCondition = "";
		for (CaseClassification cla: classifs) {
			if (!caseCondition.isEmpty())
				caseCondition += ", ";
			caseCondition += cla.ordinal();
		}

		return " and (c.classification in (" + caseCondition + "))";
	}

	
	@Override
	public String getCountEjbql() {
		return "select count(*) from Tbunit u where u.workspace.id = #{defaultWorkspace.id}";
	}
	
	@Override
	public String getEjbql() {
		// just to avoid "validate" exception
		return "from Tbunit u where u.workspace.id = #{defaultWorkspace.id}";
	}
	
	
	@Override
	public List<HealthUnitInfo> getResultList() {
		if (resultList == null)
		{
	         javax.persistence.Query query = createQuery();
	         List<Object[]> lst = query==null ? null : query.getResultList();
	         fillResultList(lst);
	    }
		return resultList;
	}

	
	@Override
	public Integer getMaxResults() {
		return 50;
	}


	protected void fillResultList(List<Object[]> lst) {
		resultList = new ArrayList<HealthUnitInfo>();
		
		for (Object[] vals: lst) {
			HealthUnitInfo info = new HealthUnitInfo();
			info.setUnitId((Integer)vals[0]);
			info.setUnitName((String)vals[1]);
			info.setAdminUnitCode((String)vals[2]);
			info.setCasesNotifs(readLongValue(vals[3]));
			info.setCasesOnTreatment(readLongValue(vals[4]));
			info.setCasesTransferIn(readLongValue(vals[5]));
			info.setCasesTransferOut(readLongValue(vals[6]));

			resultList.add(info);
		}
	}


	private Long readLongValue(Object val) {
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
		}
	}
	
	
	private AdminUnitGroup<HealthUnitInfo> findAdminUnitGroup(String adminUnitCode) {
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
