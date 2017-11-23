package org.msh.tb.reports;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.UserLog;
import org.msh.tb.entities.UserRole;
import org.msh.tb.entities.enums.RoleAction;

import javax.persistence.EntityManager;
import java.util.List;


@Name("reportOptions")
public class ReportOptions {

	@In EntityManager entityManager;
	
	@Factory("usersLog")
	public List<UserLog> getUsersLogList() {
		return entityManager.createQuery("from UserLog where id in (select a.user.id from UserWorkspace a " +
				"where a.workspace.id = #{defaultWorkspace.id}) order by name").getResultList();
	}
	
	@Factory("userRoles")
	public List<UserRole> getEvents() {
		return entityManager.createQuery("from UserRole order by code").getResultList();
	}
	
	public RoleAction[] getRoleActions() {
		return RoleAction.values();
	}
}
