package org.msh.tb.ua.cases;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;

@Name("duplicatePatients")
public class DuplicatePatients {
	
	//private Patient selectPatient;
	
	/**
	 * Return other records from table Patient with same full name and birthday
	 * @param p - patient, with which data compare
	 * @param applyUserRestriction - consider or not user view and user health system
	 * @return
	 */
	public static List<Patient> getOtherPatients(Patient p, boolean applyUserRestriction) {
		List<Patient> lst = new ArrayList<Patient>();
		String sql;
		try{
			sql = "select p.id from Patient p" +
			" where p.lastName like '%" + p.getLastName().replaceAll("'", "''").trim() +
			"%' and p.patient_name like '%" + p.getName().replaceAll("'", "''").trim() +
			"%' and p.middleName like '%" + p.getMiddleName().replaceAll("'", "''").trim() + "%'" +
			(p.getBirthDate()!=null?" and p.birthDate = '" +DateUtils.formatDate(p.getBirthDate(),"yyyy-MM-dd") + "'":"")+
			" and p.id != "+p.getId().intValue()+
			" and exists(select * " +
				"from tbcase c "+
				(applyUserRestriction?getSqlJoinForCases():"")+
				" where c.patient_id=p.id "+
				(applyUserRestriction?generateSQLConditionByUserView():"")+")";
			List<Integer> lst2 = App.getEntityManager().createNativeQuery(sql).getResultList();
			for (Integer id:lst2)
				lst.add(App.getEntityManager().find(Patient.class, id));
		}
		catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return lst;
	}
	
	/**
	 * Generate SQL condition to filter cases by user view
	 * @return
	 */
	public static String generateSQLConditionByUserView() {
		UserWorkspace userWorkspace = UserSession.getUserWorkspace();
		UserView view = userWorkspace.getView();

		String s;
		if (userWorkspace.getHealthSystem() != null)
			s = " and (u.healthSystem_id = " + userWorkspace.getHealthSystem().getId()+" or nu.healthSystem_id = " + userWorkspace.getHealthSystem().getId()+")";
		else s = "";

		if (view == null)
			return "";

		switch (view) {
			case ADMINUNIT: {
				return " and (a.code like '" + userWorkspace.getAdminUnit().getCode() + "%'"+
				" or na.code like '" + userWorkspace.getAdminUnit().getCode() + "%'"+
				" or loc.code like '"+ userWorkspace.getAdminUnit().getCode() + "%')"+s; 
			}
			case TBUNIT: 
			return " and (u.id = " + userWorkspace.getTbunit().getId()+
					" or nu.id = " + userWorkspace.getTbunit().getId()+")"+s;
		default: return s;
		}
	}
	
	/**
	 * Get SQL instruction to be included in the join declaration for cases subquery
	 * @return
	 */
	public static String getSqlJoinForCases() {
		String sql = " left outer join tbunit u on u.id = c.owner_unit_id ";
		sql += " left join administrativeunit a on a.id = u.adminunit_id ";
		sql += "left outer join tbunit nu on nu.id = c.notification_unit_id "+
		"left outer join administrativeunit loc on c.notif_adminunit_id = loc.id "+
		"left outer join administrativeunit na on na.id = nu.adminunit_id ";
		return sql;
	}

/*	public void unionPatients() {
		if (selectPatient == null)
			return;
		CaseHome caseHome = (CaseHome)App.getComponent(CaseHome.class);
		Patient curPat = caseHome.getTbCase().getPatient();
		for (TbCase tc: curPat.getCases()){
			tc.setPatient(selectPatient);
			App.getEntityManager().persist(tc);
		}
		App.getEntityManager().remove(curPat);
		App.getEntityManager().flush();
	}
	
	public void setSelectPatient(Patient selectPatient) {
		this.selectPatient = selectPatient;
	}

	public Patient getSelectPatient() {
		return selectPatient;
	}*/
}
