package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.AdminUnitGroup;
import org.msh.tb.cases.HealthUnitInfo;
import org.msh.tb.cases.HealthUnitsQuery;
import org.msh.tb.login.UserSession;

@Name("healthUnitsQueryUA")
public class HealthUnitsQueryUA extends HealthUnitsQuery {
	private static final long serialVersionUID = 4887501919593423214L;

	@Override
	protected Query createQuery() {
		String casecond = generateSQLConditionByCase();
		
		Integer hsID = null;
		if (userWorkspace.getHealthSystem() != null)
			hsID = userWorkspace.getHealthSystem().getId();
		
		String sql = "select u.id, u.name1, a.code, " +
				//"(select count(*) from tbcase c where c.notification_unit_id = u.id and c.state <= 2 " + casecond + ") numcases, " +

				// on treatment here
				"(select count(*) from tbcase c " +
				"where c.state=1 and c.owner_unit_id = u.id " + casecond + ") as ontreat, " +
				// cured, but need closed
				"(select count(*) from treatmenthealthunit t inner join tbcase c on c.id = t.case_id " +
				"where c.state = 1 and c.owner_unit_id = u.id and c.endtreatmentdate < CURDATE() " + casecond + ") as needclosed, " +
				// notified by
				"(select count(*) from tbcase c where c.notification_unit_id = u.id " + casecond + ") numcases, " +

				// on treatment here, treatment and notify same
				"(select count(*) from treatmenthealthunit t inner join tbcase c on c.id = t.case_id " +
				"where c.state = 1 and t.unit_id = u.id and c.owner_unit_id = u.id "  + casecond + ") as transfering, " +
				// others
				"(select count(*) from tbcase c " +
				"where c.state <> 1 and c.notification_unit_id = u.id " + casecond + ") as others "  +
				
				"from tbunit u inner join administrativeunit a on a.id = u.adminunit_id " +
				"where u.workspace_id = " + UserSession.getWorkspace().getId().toString() + generateSQLConditionByUserView() +
				(hsID != null? " and u.healthsystem_id = " + hsID: "") + 
				" group by u.id, u.name1, a.code having numcases > 0 or ontreat > 0  order by a.code, u.name1";
		
		Query query = entityManager.createNativeQuery(sql);
		if ( getFirstResult()!=null) query.setFirstResult( getFirstResult() );
	    if ( getMaxResults()!=null) query.setMaxResults( getMaxResults()+1 ); //add one, so we can tell if there is another page
		
		return query;
	}
	
	/**
	 * Create the result list from the resultset of the query
	 * UA specific fields added AK
	 * @param lst
	 * @return
	 */
	@Override
	protected List<HealthUnitInfo> fillResultList(List<Object[]> lst) {
		List<HealthUnitInfo> res = new ArrayList<HealthUnitInfo>();
		
		for (Object[] vals: lst) {
			HealthUnitInfoUA info = new HealthUnitInfoUA();
			info.setUnitId((Integer)vals[0]);
			info.setUnitName((String)vals[1]);
			info.setAdminUnitCode((String)vals[2]);
			
			info.setCasesOnTreatment(readLongValue(vals[3]));
			info.setCasesNeedClosed(readLongValue(vals[4]));
			info.setCasesNotifs(readLongValue(vals[5]));
			info.setCasesHere(readLongValue(vals[6]));
			info.setCasesOther(readLongValue(vals[7]));

			res.add(info);
		}
		return res;
	}
	

	
	@Override
	public Integer getMaxResults() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<AdminUnitGroup<HealthUnitInfoUA>> getAdminUnitsUA() {
		return (List<AdminUnitGroup<HealthUnitInfoUA>>)(List<?>)getAdminUnits();
	}
}
