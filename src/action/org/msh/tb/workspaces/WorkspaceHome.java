package org.msh.tb.workspaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.CountryStructuresQuery;
import org.msh.tb.application.WorkspaceViewService;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserPermission;
import org.msh.tb.entities.UserProfile;
import org.msh.tb.entities.UserRole;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.WeeklyFrequency;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.WorkspaceView;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.NameComposition;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.TransactionLogService;
import org.msh.utils.EntityQuery;



@Name("workspaceHome")
@LogInfo(roleName="WORKSPACES")
public class WorkspaceHome extends EntityHomeEx<Workspace> {
	private static final long serialVersionUID = -5471044065603168728L;

	@In(required=true) UserLogin userLogin;
	@In(required=false) @Out(required=false, scope=ScopeType.EVENT) Workspace defaultWorkspace;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) EntityManager entityManager;
	@In(create=true) CountryStructuresQuery countryStructures;
	@In(create=true) WorkspaceViewService workspaceViewService;
	
	private UserWorkspace userWorkspace;
	private List<Tbunit> units;
	private List<UserProfile> profiles;
	private NameComposition nameComposition;
	private List<User> addedUsers;
	private List<UserWorkspace> removedUsers = new ArrayList<UserWorkspace>();
	private List<SelectItem> countryLevels;
	private WorkspaceView view;
	
	private byte[] picture;
	private String pictureFileName;
	private String pictureContentType;
	private int pictureSize;
	private boolean clearPicture;
	private boolean viewChanged;

	private String userName;
	private Integer userId;



	@Factory("workspace")
	public Workspace getWorkspaceInstance() {
		return getInstance();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#persist()
	 */
	@Override
	public String persist() {
		boolean bnew = !isManaged();

		if (!updateView())
			return "error";

		// instantiate default options, if no option is selected
		Workspace ws = getInstance();
		if (ws.getPatientNameComposition() == null)
			ws.setPatientNameComposition(NameComposition.FULLNAME);
		if (ws.getDisplayCaseNumber() == null)
			ws.setDisplayCaseNumber(DisplayCaseNumber.SYSTEM_GENERATED);
		if (ws.getPatientAddrRequiredLevels() == null)
			ws.setPatientAddrRequiredLevels(1);

		// initialize weekly frequencies
		ws.setWeekFreq1(new WeeklyFrequency(2));
		ws.setWeekFreq2(new WeeklyFrequency(18));
		ws.setWeekFreq3(new WeeklyFrequency(42));
		ws.setWeekFreq4(new WeeklyFrequency(30));
		ws.setWeekFreq5(new WeeklyFrequency(62));
		ws.setWeekFreq6(new WeeklyFrequency(126));
		ws.setWeekFreq7(new WeeklyFrequency(127));
		
		updateNameComposition();

		getInstance().setView(null);
		String ret = super.persist();

		if ((bnew) || (viewChanged)) {
			view.setId(ws.getId());
			view.setWorkspace(ws);
			getEntityManager().persist(view);
			getEntityManager().flush();
			viewChanged = true;
		}
		
		if (ret.equals("persisted")) {
			if (bnew)
				createDefaultEntities();

			if (viewChanged)
				workspaceViewService.updateView(view);			
		}
		
		return ret;
	}



	/**
	 * Create default entities for the workspace (for a minimum working area) 
	 */
	protected void createDefaultEntities() {
		Workspace workspace = getInstance();
		
		// create administrator profile
		UserProfile prof = new UserProfile();
		prof.setName("Administrator");
		prof.setWorkspace(workspace);
		List<UserRole> roles = getEntityManager().createQuery("from UserRole " +
				"where id in (select a.userRole.id from UserPermission a " +
				"where a.userProfile.id in (select b.profile.id from UserWorkspace b where b.user.id = #{userLogin.user.id}))")
				.getResultList();

		for (UserRole role: roles) {
			if (!role.isInternalUse()) {
				if (role.isByCaseClassification()) {
					for (CaseClassification cla: CaseClassification.values()) {
						UserPermission perm = createPermission(prof, role);
						perm.setCaseClassification(cla);
					}
				}
				else createPermission(prof, role);
			}
		}
		getEntityManager().persist(prof);
		
		// create temporary region
		CountryStructure cs = new CountryStructure();
		cs.setLevel(1);
		cs.getName().setName1("Region");
		cs.setWorkspace(workspace);
		getEntityManager().persist(cs);

		AdministrativeUnit reg = new AdministrativeUnit();
		reg.getName().setName1("REGION 1");
		reg.setCode("001");
		reg.setWorkspace(workspace);
		reg.setUnitsCount(1);
		reg.setCountryStructure(cs);
		getEntityManager().persist(reg);
		
		// create temporaty locality
		CountryStructure cs2 = new CountryStructure();
		cs2.setLevel(2);
		cs2.getName().setName1("Locality");
		cs2.setWorkspace(workspace);
		getEntityManager().persist(cs2);

		AdministrativeUnit loc = new AdministrativeUnit();
		loc.getName().setName1("LOCALITY 1");
		loc.setCode("001001");
		loc.setParent(reg);
		loc.setCountryStructure(cs2);
		loc.setWorkspace(workspace);
		getEntityManager().persist(loc);
		
		// create default health system
		HealthSystem healthSystem = new HealthSystem();
		healthSystem.getName().setName1("Ministry of Health");
		healthSystem.setWorkspace(workspace);
		getEntityManager().persist(healthSystem);
		
		// create temporary unit
		Tbunit unit = new Tbunit();
		unit.getName().setName1("TEMPORARY TB UNIT");
		unit.setAdminUnit(loc);
		unit.setWorkspace(workspace);
		unit.setHealthSystem(healthSystem);
		unit.setActive(true);
		getEntityManager().persist(unit);
	}

	/**
	 * Create user permission for the profile and based on the user role informed
	 * @param prof
	 * @param role
	 * @return
	 */
	protected UserPermission createPermission(UserProfile prof, UserRole role) {
		UserPermission p = new UserPermission();
		p.setGrantPermission(true);
		p.setUserProfile(prof);
		p.setUserRole(role);
		p.setCanExecute(true);
		if (role.isChangeable())
			p.setCanChange(true);
		prof.getPermissions().add(p);
		
		return p;
	}

	/**
	 * Returns the default workspace for editing
	 * @return
	 */
	public Workspace getDefaultWorkspace() {
		if (defaultWorkspace == null)
			return null;
		
		if (!defaultWorkspace.getId().equals(getId())) {
			setId(defaultWorkspace.getId());
		}
		
		return getInstance();
	}

	
	/**
	 * Save changes in the default workspace, i.e the workspace currently used
	 * @return
	 */
	public String saveDefaultworkspace() {
		if (!persist().equals("persisted"))
			return "error";
		
		TransactionLogService logService = new TransactionLogService();
		defaultWorkspace.setUsers(null);
		logService.recordEntityState(defaultWorkspace, Operation.EDIT);
		logService.save("SETUPWS", RoleAction.EDIT, getInstance());
//		logService.saveEntityDifferences(defaultWorkspace, getInstance(), "SETUPWS");
		
		defaultWorkspace = getInstance();
		
		return "defaultws-persisted";
	}
	
	
	@Override
	public EntityQuery<Workspace> getEntityQuery() {
		return (WorkspacesQuery)Component.getInstance("workspaces", false);
	}
	
	/**
	 * Update the picture options changed by the user
	 */
	protected boolean updateView() {
		boolean bHasPicture = (pictureFileName != null) && (!pictureFileName.isEmpty());  

		if (view != null) {
			if (clearPicture)
				view.setPicture(null);
			if (bHasPicture) { 
				int ind = pictureFileName.lastIndexOf('.');
				String ext = pictureFileName.substring(ind);
				if (!view.setPictureContentTypeByFileExtension(ext)) {
					facesMessages.add("Image type not supported: " + ext);
					return false;
				}
				view.setPicture(picture);
			}
		}
		
		viewChanged = (view != null) && ((bHasPicture) || (clearPicture)); 
		return true;
	}

	
	/**
	 * Check if user is into workspace
	 * @param user
	 * @return true if user is into workspace
	 */
	protected boolean isUserIntoWorkspace(User user) {
		for (UserWorkspace userWorkspace: getInstance().getUsers()) {
			if (userWorkspace.getUser().equals(user))
				return true;
		}
		return false;
	}


	/**
	 * Include a new user in the workspace
	 * @param usu
	 */
	public void addUser() {
		if (userId == null)
			return;

		User usu = getEntityManager().find(User.class, userId);
		
		UserWorkspace ws = getUserWorkspace();
		ws.setUser(usu);
		
		if (isUserIntoWorkspace(usu)) {
			facesMessages.addFromResourceBundle("admin.workspaces.userexists", usu.getName());
			return;
		}
		
		if (addedUsers == null)
			addedUsers = new ArrayList<User>();
		addedUsers.add(usu);
		
		if (ws.getAdminUnit() == null)
			ws.setAdminUnit(ws.getTbunit().getAdminUnit());
		
		ws.setWorkspace(getInstance());
		
		getInstance().getUsers().add(ws);
		usu.getWorkspaces().add(ws);
		userWorkspace = null;
		userName = null;
	}


	/**
	 * Remove a user from the workspace
	 * @param u
	 */
	public void remUser(UserWorkspace u) {
		getInstance().getUsers().remove(u);
		
		if (getEntityManager().contains(u))
			removedUsers.add(u);
		else u.getUser().getWorkspaces().remove(u);
	}


	/**
	 * Select users available for selection
	 */
	public List<User> listUsers(Object event) {
		String userName = event.toString();
		if ((userName == null) || (userName.isEmpty())) 
			return null;
		
		String nomeLike = userName.toUpperCase() + "%";
		
		return getEntityManager()
			.createQuery("from User u where (upper(u.login) like :p1) or (upper(u.name) like :p2) order by u.name")
			.setMaxResults(30)
			.setParameter("p1", nomeLike)
			.setParameter("p2", nomeLike)
			.getResultList();
	}


	public List<Tbunit> getUnits() {
		if (units == null)
			units = getEntityManager()
				.createQuery("from Tbunit u where u.workspace.id = #{workspace.id} order by u.name.name1")
				.getResultList();
		return units;
	}



	public List<UserProfile> getProfiles() {
		if (profiles == null)
			profiles = getEntityManager()
				.createQuery("from UserProfile u where u.workspace.id = #{workspace.id}")
				.getResultList();
		return profiles;
	}



	public UserWorkspace getUserWorkspace() {
		if (userWorkspace == null)
			userWorkspace = new UserWorkspace();
		return userWorkspace;
	}
	

	/* (non-Javadoc)
	 * @see org.msh.tb.home.EntityHomeEx#remove()
	 */
	@Override
	public String remove() {
		UserLogin userLogin = getUserLogin();
		
		if (userLogin.getDefaultWorkspace().equals(getInstance())) {
			facesMessages.addFromResourceBundle("admin.workspaces.delerror1");
			return "error";
		}
		
		return super.remove();
	}
	
	
	/**
	 * Save users changes (new users and removed users)
	 * @return
	 */
	@Transactional
	@End
	public String saveUsers() {
		removeUsers();
		
		// register in log the users changed
		TransactionLogService logService = new TransactionLogService();
		
		if (addedUsers != null) {
			for (User user: addedUsers)
				logService.addTableRow("admin.workspaces.addeduser", user.getLogin() + " - " + user.getName());
		}
		if (removedUsers != null) {
			for (UserWorkspace uw: removedUsers)
				logService.addTableRow("admin.workspaces.removeduser", uw.getUser().getLogin() + " - " + uw.getUser().getName());
		}
		logService.saveExecuteTransaction("WSADDREMUSER", getInstance().toString(), getInstance().getId(), getInstance().getClass().getSimpleName(), getInstance());
		
		return persist();
	}


	/**
	 * Remove the users selected to be removed
	 */
	protected void removeUsers() {
		for (UserWorkspace ws: removedUsers) {
			User u = ws.getUser();
			u.getWorkspaces().remove(ws);
			getEntityManager().remove(ws);
		}
	}

	
	/**
	 * Update the patient name composition when it's changed
	 */
	protected void updateNameComposition() {
		if ((nameComposition == null) || (nameComposition == getInstance().getPatientNameComposition()))
			return;
		
		// if the new number of fields is equals or bigger than the current composition, so there is nothing to do
		if (nameComposition.getNumFields() < getInstance().getPatientNameComposition().getNumFields()) {
			// update fields to avoid p.name as null when concatenating information
			entityManager.createQuery("update Patient p set p.middleName = '' where p.middleName is null and p.workspace.id = #{defaultWorkspace.id}").executeUpdate();
			entityManager.createQuery("update Patient p set p.lastName = '' where p.lastName is null and p.workspace.id = #{defaultWorkspace.id}").executeUpdate();
			
			String hql;
			if (nameComposition.getNumFields() == 1)
				hql = "p.name = p.name || ' ' || p.middleName || ' ' || p.lastName, p.middleName = null, p.lastName = null";
			else 
				hql = "p.middleName = p.middleName || ' ' || p.lastName, p.lastName = null";
			
			entityManager
				.createQuery("update Patient p set " + hql + " where p.workspace.id = #{defaultWorkspace.id}")
				.executeUpdate();
		}
		
		getInstance().setPatientNameComposition(nameComposition);
	}


	/**
	 * @param nameComposition the nameComposition to set
	 */
	public void setNameComposition(NameComposition nameComposition) {
		this.nameComposition = nameComposition;
	}


	/**
	 * @return the nameComposition
	 */
	public NameComposition getNameComposition() {
		if (nameComposition == null)
			nameComposition = getInstance().getPatientNameComposition();
		return nameComposition;
	}



	/**
	 * Return list of administrative unit leves
	 * @return
	 */
	public List<SelectItem> getCountryLevels() {
		if (countryLevels == null) {
			countryLevels = new ArrayList<SelectItem>();
			countryLevels.add(new SelectItem(null, "-"));
			
			String s = "";
			for (CountryStructure cs: countryStructures.getResultList()) {
				if (s.isEmpty())
					 s = cs.getName().toString();
				else s += ", " + cs.getName().toString();
				countryLevels.add(new SelectItem(cs.getLevel(), s));
			}
		}
		return countryLevels;
	}
	

	/**
	 * Return the view assigned to the default workspace
	 * @return
	 */
	public WorkspaceView getView() {
		if (view == null) {
			Workspace ws = getInstance();
			try {
				view = entityManager.find(WorkspaceView.class, ws.getId());
			} catch (Exception e) {
			}

			if (view == null) {
				view = new WorkspaceView();
			}
		}
		return view;
	}


	/**
	 * @return the pictureFileName
	 */
	public String getPictureFileName() {
		return pictureFileName;
	}


	/**
	 * @param pictureFileName the pictureFileName to set
	 */
	public void setPictureFileName(String pictureFileName) {
		this.pictureFileName = pictureFileName;
	}


	/**
	 * @return the pictureContentType
	 */
	public String getPictureContentType() {
		return pictureContentType;
	}


	/**
	 * @param pictureContentType the pictureContentType to set
	 */
	public void setPictureContentType(String pictureContentType) {
		this.pictureContentType = pictureContentType;
	}


	/**
	 * @return the pictureSize
	 */
	public int getPictureSize() {
		return pictureSize;
	}


	/**
	 * @param pictureSize the pictureSize to set
	 */
	public void setPictureSize(int pictureSize) {
		this.pictureSize = pictureSize;
	}


	/**
	 * @return the picture
	 */
	public byte[] getPicture() {
		return picture;
	}


	/**
	 * @param picture the picture to set
	 */
	public void setPicture(byte[] picture) {
		this.picture = picture;
	}


	/**
	 * @return the clearPicture
	 */
	public boolean isClearPicture() {
		return clearPicture;
	}


	/**
	 * @param clearPicture the clearPicture to set
	 */
	public void setClearPicture(boolean clearPicture) {
		this.clearPicture = clearPicture;
	}


	/**
	 * Process the auto complete list for the user name
	 * @param suggestion partial name of the user
	 * @return
	 */
	public List<UserInfo> autocompleteList(Object suggestion) {
		if (suggestion == null)
			return null;
		
		String param = '%' + suggestion.toString().toUpperCase() + '%';
		List<User> lst = getEntityManager().createQuery("from User u where upper(u.name) like :p1" +
					" or upper(u.login) like :p2 or upper(u.email) like :p3 order by u.name")
					.setParameter("p1", param)
					.setParameter("p2", param)
					.setParameter("p3", param)
					.getResultList();
		
		List<UserInfo> results = new ArrayList<UserInfo>();

		for (User user: lst) {
			if (!isUserIntoWorkspace(user)) {
				UserInfo info = new UserInfo();
				info.setId(user.getId());
				info.setName(user.getName());
				info.setLogin(user.getLogin());
				results.add(info);
			}
		}
		
		return results;
	}


	public String getUserName() {
		return userName;
	}



	public void setUserName(String userName) {
		this.userName = userName;
	}


	/**
	 * Provide information about a user to be serialized to the client side
	 * @author Ricardo Memoria
	 *
	 */
	public class UserInfo implements Serializable {
		private static final long serialVersionUID = -2422851080153786077L;
		
		private Integer id;
		private String name;
		private String login;

		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getLogin() {
			return login;
		}
		public void setLogin(String login) {
			this.login = login;
		}
	}


	public Integer getUserId() {
		return userId;
	}


	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
