package org.msh.tb.reportgen;

import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.UserRole;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

import javax.persistence.EntityManager;
import java.util.List;

public class RoleVariable implements Variable {

	private static final String fields[] = {"role_id"};
	
	private List<UserRole> roles;


	@Override
	public String getTitle() {
		return Messages.instance().get("UserRole");
	}

	@Override
	public String[] createSQLSelectFields(ReportQuery reportQuery) {
//		reportQuery.addJoinMasterTable("userrole", "id", "role_id");
		return fields;
	}

	@Override
	public String[] createSQLGroupByFields(ReportQuery reportQuery) {
		return fields;
	}

	@Override
	public Object translateValues(Object[] value) {
		return null;
	}

	@Override
	public Object translateSingleValue(Object value) {
		Integer id = (Integer)value;
		return roleById(id);
	}

	@Override
	public String getValueDisplayText(Object value) {
		return (value != null? ((UserRole)value).getDisplayName() : "-");
	}

	@Override
	public Integer compareValues(Object val1, Object val2) {
		if ((val1 == null) && (val2 == null))
			return 0;
		
		if (val1 == null)
			return 1;
		
		if (val2 == null)
			return -1;
		
		UserRole role1 = (UserRole)val1;
		UserRole role2 = (UserRole)val2;
		
		return role1.getDisplayName().compareToIgnoreCase(role2.getDisplayName());
	}

	@Override
	public boolean setGrouping(boolean value) {
		return true;
	}

	@Override
	public Object getGroupData(Object val1) {
		UserRole role = (UserRole)val1;
		String code = role.getCode().substring(0,2) + "0000";
		return (code != null? roleByCode(code): role);
	}


	protected UserRole roleByCode(String code) {
		for (UserRole role: getRoles()) {
			if (role.getCode().equals(code))
				return role;
		}
		return null;
	}
	
	protected UserRole roleById(Integer id) {
		for (UserRole role: getRoles()) {
			if (role.getId().equals(id))
				return role;
		}
		return null;
	}


	/**
	 * Return list of roles
	 * @return
	 */
	public List<UserRole> getRoles() {
		if (roles == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			roles = em.createQuery("from UserRole order by code").getResultList();
		}
		return roles;
	}
}
