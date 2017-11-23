package org.msh.tb;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.msh.tb.application.mail.MailService;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.enums.UserView;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MsgDispatcher {

	@In EntityManager entityManager;
	private Map<String, Object> components;
	
	/**
	 * Add a component to be available in the context of the mail sender
	 * @param compName
	 * @param component
	 */
	public void addComponent(String compName, Object component) {
		if (components == null)
			components = new HashMap<String, Object>();
		components.put(compName, component);
	}
	
	/**
	 * Add a component to be available in the context of the mail sender
	 * @param compName
	 * @param component
	 */
	public void remComponent(String compName) {
		if (components != null)
			components.remove(compName);
	}
	
	/**
	 * Return the current user logged into the system
	 * @return
	 */
	public User getCurrentUser() {
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin");
		return (userLogin != null? userLogin.getUser(): null);
	}
	
	/**
	 * Return the list of users that have permission to a particular role in a unit
	 * @param rolename
	 * @param unit
	 * @return
	 */
	protected List<User> getUsersByRoleAndUnit(String roleName, Tbunit unit) {
		String code = unit.getAdminUnit().getCode();
		String s = "";
		while (code != null) {
			if (!s.isEmpty())
				s += ",";
			s += "'" + code + "'";
			code = AdministrativeUnit.getParentCode(code);
		}
		
		String hql = "select u.user from UserWorkspace u where u.profile.id in " +
						"(select distinct p.id " +
							"from UserProfile p join p.permissions perm " +
							"where perm.userRole.name like :rolename " +
							"and p.workspace.id = #{defaultWorkspace.id} " +
							"and ((perm.userRole.changeable = :true and perm.canChange = :true) or (perm.userRole.changeable = :false and perm.canExecute = :true))" +
							"and (u.view = :viewcountry or (u.view = :viewunit and u.tbunit.id = :unitid) " +
							"or (u.view = :viewadm and u.tbunit.adminUnit.code in (" + s + "))))";

		
		List<User> lst = (List<User>) entityManager.createQuery(hql)
			.setParameter("true", true)
			.setParameter("false", false)
			.setParameter("viewcountry", UserView.COUNTRY)
			.setParameter("viewunit", UserView.TBUNIT)
			.setParameter("unitid", unit.getId())
			.setParameter("viewadm", UserView.ADMINUNIT)
			.setParameter("rolename", roleName)
			.getResultList();
		
		return lst;
	}
	
	/**
	 * Return the list of users that have permission to a particular role in a unit
	 * @param rolename
	 * @param unit
	 * @return
	 */
	public List<User> getUsersByRoleAndUnit(String roleName, Tbunit unit, boolean checkPlayOtherUnits) {
		if(!checkPlayOtherUnits){
			return getUsersByRoleAndUnit(roleName, unit);
		}
		
		String code = unit.getAdminUnit().getCode();
		String s = "";
		while (code != null) {
			if (!s.isEmpty())
				s += ",";
			s += "'" + code + "'";
			code = AdministrativeUnit.getParentCode(code);
		}
		
		String hql = "select u.user from UserWorkspace u where u.profile.id in " +
						"(select distinct p.id " +
							"from UserProfile p join p.permissions perm " +
							"where perm.userRole.name like :rolename " +
							"and p.workspace.id = #{defaultWorkspace.id} " +
							"and ((perm.userRole.changeable = :true and perm.canChange = :true) or (perm.userRole.changeable = :false and perm.canExecute = :true))" +
							"and (u.view = :viewcountry or ( u.playOtherUnits = :true and ((u.view = :viewunit and u.tbunit.id = :unitid) " +
							"or (u.view = :viewadm and u.tbunit.adminUnit.code in (" + s + "))))))";

		
		List<User> lst = (List<User>) entityManager.createQuery(hql)
			.setParameter("true", true)
			.setParameter("false", false)
			.setParameter("viewcountry", UserView.COUNTRY)
			.setParameter("viewunit", UserView.TBUNIT)
			.setParameter("unitid", unit.getId())
			.setParameter("viewadm", UserView.ADMINUNIT)
			.setParameter("rolename", roleName)
			.getResultList();
		
		return lst;
	}
	
	/**
	 * Return the list of users that have permission to a particular role in a unit
	 * @param unit
	 * @return
	 */
	protected List<User> getUsersByUnitAndView(Tbunit unit) {
		String code = unit.getAdminUnit().getCode();
		String s = "";
		while (code != null) {
			if (!s.isEmpty())
				s += ",";
			s += "'" + code + "'";
			code = AdministrativeUnit.getParentCode(code);
		}
		
		String hql = "select u.user from UserWorkspace u " +
							"where u.workspace.id = #{defaultWorkspace.id} " + 
							"and (u.view = :viewcountry or (u.view = :viewunit and u.tbunit.id = :unitid) " +
							"or (u.view = :viewadm and u.tbunit.adminUnit.code in (" + s + ")))";

		
		List<User> lst = (List<User>) entityManager.createQuery(hql)
			.setParameter("viewcountry", UserView.COUNTRY)
			.setParameter("viewunit", UserView.TBUNIT)
			.setParameter("unitid", unit.getId())
			.setParameter("viewadm", UserView.ADMINUNIT)
			.getResultList();
		
		return lst;
	}
	
	/**
	 * Return the list of users that have permission to a particular role in a unit
	 * @param unit
	 * @return
	 */
	protected List<User> getUsersByUnit(Tbunit unit) {
		String code = unit.getAdminUnit().getCode();
		String s = "";
		while (code != null) {
			if (!s.isEmpty())
				s += ",";
			s += "'" + code + "'";
			code = AdministrativeUnit.getParentCode(code);
		}
		
		String hql = "select u.user from UserWorkspace u " +
						"where p.workspace.id = #{defaultWorkspace.id} " + 
						"and u.tbunit.id = :unitid" ;

		
		List<User> lst = (List<User>) entityManager.createQuery(hql)
			.setParameter("unitid", unit.getId())
			.getResultList();
		
		return lst;
	}
	
	/**
	 * Send a message to a list of users using its default language and time zone
	 * @param users
	 * @param mailpage
	 */
	public void sendMessage(List<User> users, String mailpage) {
		MailService srv = MailService.instance();
		Set<String> keys = components.keySet();
		
		for (User user: users) {
			for(String key : keys){
				srv.addComponent(key, components.get(key));
			}
			srv.addComponent("user", user);
			srv.addMessageToQueue(mailpage, user.getTimeZone(), user.getLanguage(), user, true);
		}
		
		srv.dispatchQueue();
		components = null;
	}
	
	/**
	 * Send a message to a list of users using its default language and time zone
	 * @param userRole
	 * @param tbUnit
	 * @param mailpage
	 */
	public void sendMessage(String userRole, Tbunit unit, String mailpage) {
		List<User> users = getUsersByRoleAndUnit(userRole, unit);
		
		sendMessage(users, mailpage);
	}
}
