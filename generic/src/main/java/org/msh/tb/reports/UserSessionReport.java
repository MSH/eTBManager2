package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.utils.date.DateUtils;


/**
 * Generate user session history report
 * @author Ricardo Memoria
 *
 */
@Name("userSessionReport")
public class UserSessionReport {

	@In(create=true) EntityManager entityManager;
	@In(create=true) ReportSelection reportSelection;
	
	private UserLogin userLogin;
	private List<Item> items;
	private Date date;

	
	/**
	 * Generate the report
	 */
	protected void createReport() {
		if (date == null)
			return;
		
		String hql = "select log.id, u.login, u.name, log.loginDate, log.logoutDate, " +
				"log.IpAddress, w.name.name1 " + 
				"from UserLogin log join log.user u join log.workspace w " +
				"where log.loginDate >= :dtini and log.loginDate < :dtend";

		UserWorkspace userWorkspace = (reportSelection != null? reportSelection.getUserWorkspace(): null);
		
		if (userWorkspace != null)
			 hql += " and w.id = #{reportSelection.userWorkspace.workspace.id}";
		else hql += " and w.id in (select aux.workspace.id from UserWorkspace aux " +
					"where aux.user.id = #{userLogin.user.id})";
		
		hql += " order by log.id";
		
		Date dtEnd = DateUtils.incDays(date, 1);
		
		List<Object[]> lst = entityManager.createQuery(hql)
				.setParameter("dtini", date)
				.setParameter("dtend", dtEnd)
				.getResultList();
		
		items = new ArrayList<Item>();
		
		for (Object[] vals: lst) {
			Item item = new Item();
			item.setLoginId((Integer)vals[0]);
			item.setUserLogin((String)vals[1]);
			item.setUserName((String)vals[2]);
			item.setDateLogin((Date)vals[3]);
			item.setDateLogout((Date)vals[4]);
			item.setIpAddress((String)vals[5]);
			item.setWorkspace((String)vals[6]);
			
			items.add(item);
		}
	}


	/**
	 * Initialize report parameters
	 */
	public void initialize() {
		if (date == null)
			date = DateUtils.getDate();

		reportSelection.initializeWorkspace();
	}


	/**
	 * Called when the user workspace is changed in the report selection
	 */
	@Observer(value="report-workspace-changed", create=false)
	public void workspaceChangeListener() {
		items = null;
	}


	/**
	 * Return the user session list 
	 * @return List of {@link Item} instances
	 */
	public List<Item> getItems() {
		if (items == null)
			createReport();
		return items;
	}

	
	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		if ((date != null) && (!date.equals(this.date)))
			items = null;
		this.date = date;
	}


	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	
	public void setUserLoginId(Integer id) {
		userLogin = entityManager.find(UserLogin.class, id);
	}
	
	public Integer getUserLoginId() {
		return (userLogin == null? null: userLogin.getId());
	}
	
	public UserLogin getUserLogin() {
		return userLogin;
	}
	
	/**
	 * Keep information about a user session
	 * @author Ricardo Memoria
	 *
	 */
	public class Item {
		private Integer loginId;
		private String userName;
		private String userLogin;
		private Date dateLogin;
		private Date dateLogout;
		private String ipAddress;
		private String sessionTime;
		private String workspace;
		
		/**
		 * @return the loginId
		 */
		public Integer getLoginId() {
			return loginId;
		}
		/**
		 * @param loginId the loginId to set
		 */
		public void setLoginId(Integer loginId) {
			this.loginId = loginId;
		}
		/**
		 * @return the userName
		 */
		public String getUserName() {
			return userName;
		}
		/**
		 * @param userName the userName to set
		 */
		public void setUserName(String userName) {
			this.userName = userName;
		}
		/**
		 * @return the userLogin
		 */
		public String getUserLogin() {
			return userLogin;
		}
		/**
		 * @param userLogin the userLogin to set
		 */
		public void setUserLogin(String userLogin) {
			this.userLogin = userLogin;
		}
		/**
		 * @return the dateLogin
		 */
		public Date getDateLogin() {
			return dateLogin;
		}
		/**
		 * @param dateLogin the dateLogin to set
		 */
		public void setDateLogin(Date dateLogin) {
			this.dateLogin = dateLogin;
		}
		/**
		 * @return the dateLogout
		 */
		public Date getDateLogout() {
			return dateLogout;
		}
		/**
		 * @param dateLogout the dateLogout to set
		 */
		public void setDateLogout(Date dateLogout) {
			this.dateLogout = dateLogout;
		}
		/**
		 * @return the ipAddress
		 */
		public String getIpAddress() {
			return ipAddress;
		}
		/**
		 * @param ipAddress the ipAddress to set
		 */
		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}
		/**
		 * @return the sessionTime
		 */
		public String getSessionTime() {
			return sessionTime;
		}
		/**
		 * @param sessionTime the sessionTime to set
		 */
		public void setSessionTime(String sessionTime) {
			this.sessionTime = sessionTime;
		}
		/**
		 * @param workspace the workspace to set
		 */
		public void setWorkspace(String workspace) {
			this.workspace = workspace;
		}
		/**
		 * @return the workspace
		 */
		public String getWorkspace() {
			return workspace;
		}
	}
}
