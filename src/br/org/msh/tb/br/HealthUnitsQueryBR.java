package org.msh.tb.br;

import java.math.BigInteger;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.HealthUnitInfo;
import org.msh.tb.cases.HealthUnitsQuery;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;

@Name("healthUnitsQueryBR")
public class HealthUnitsQueryBR extends HealthUnitsQuery {
	private static final long serialVersionUID = -5976513410629515587L;

	/* (non-Javadoc)
	 * @see org.msh.tb.cases.HealthUnitsQuery#createResultList()
	 */
	@Override
	public List<HealthUnitInfo> createResultList() {
		List<HealthUnitInfo> lst = super.createResultList();
		
		createMedExamMissing(lst);
		
		return lst;
	}
	
	
	public void createMedExamMissing(List<HealthUnitInfo> lst) {
		String casecond = generateSQLConditionByCase();
		
		Integer hsID = null;
		
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		Workspace defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");
		
		if (userWorkspace.getHealthSystem() != null)
			hsID = userWorkspace.getHealthSystem().getId();

		String hql = "select u.id, count(*) " +
			"from TbCase c " +
			"inner join Tbunit u on u.id = c.notification_unit_id " +
			"inner join AdministrativeUnit a on a.id = u.adminunit_id " +
			"inner join MedicalExamination med on med.case_id = c.id " +
			"where u.workspace_id = " + defaultWorkspace.getId().toString() + generateSQLConditionByUserView() +
			(hsID != null? " and u.healthsystem_id = " + hsID: "") + casecond +
			" and med.nextAppointment is not null and med.nextAppointment + 7 < current_date " +
			" and c.state < 3 " +
			" and med.EVENT_DATE = (select max(m.EVENT_DATE) from MedicalExamination m where m.case_id = c.id)" +
			"group by u.id";

		List<Object[]> res = getEntityManager()
			.createNativeQuery(hql)
			.getResultList();

		for (Object[] val: res) {
			bindMedExamResult(lst, val);
		}
	}

	
	/**
	 * @param vals
	 */
	protected void bindMedExamResult(List<HealthUnitInfo> lst, Object[] vals) {
		HealthUnitInfo info = null;
		
		Integer unitId = (Integer)vals[0];
		Long count = ((BigInteger)vals[1]).longValue();
		
		for (HealthUnitInfo aux: lst) {
			if (unitId.equals(aux.getUnitId())) {
				info = aux;
				break;
			}
		}
		
		if (info == null)
			return;
		
		Long n = info.getMedExamMissing();
		if (n == null)
			 info.setMedExamMissing(count);
		else info.setMedExamMissing(n + count);
	}
}
