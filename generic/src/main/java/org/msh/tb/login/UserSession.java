package org.msh.tb.login;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.security.Identity;
import org.jboss.seam.web.ServletContexts;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.Passwords;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.acl.Group;
import java.util.*;


/**
 * Handle common and generic operations of a user session. A user session is defined by
 * the moment the user logs into the system until the moment he logs out
 * @author Ricardo Memoria
 *
 */
@Name("userSession")
@AutoCreate
public class UserSession {

	private boolean initingList;
	
	private Integer userWorkspaceId;
	
	private String sessionId;
	
	private TBUnitSelection tbunitselection;
	
	/**
	 * Static method to return an instance of the {@link Workspace} in the current session
	 * @return
	 */
	public static Workspace getWorkspace() {
		return (Workspace)Component.getInstance("defaultWorkspace");
	}
	
	
	/**
	 * Static method to return an instance of the {@link User} in use in the current session
	 * @return
	 */
	public static User getUser() {
		return ((UserWorkspace)Component.getInstance("userWorkspace")).getUser();
	}
	
	
	/**
	 * Static method to return an instance of the {@link UserWorkspace} in the current session
	 * @return
	 */
	public static UserWorkspace getUserWorkspace() {
		return (UserWorkspace)Component.getInstance("userWorkspace");
	}
	
	/**
	 * Static method to return an instance of the {@link UserLogin} in the current session
	 * @return
	 */
	public static UserLogin getUserLogin() {
		return (UserLogin)Component.getInstance("userLogin");
	}


    /**
     * Register the logout when the user session is finished by time-out
     */
    @Observer("org.jboss.seam.preDestroyContext.SESSION")
    @Transactional
    public void logout() {
    	if (getUserLogin() == null)
    		return;

    	registerLogout();
    }

    
    /**
     * Sign the UI that the user wants to select a workspace among the list available
     */
    public void initWorkspaceList() {
    	Contexts.getEventContext().set("userWsInited", true);
    }
    

    /**
     * Change the user workspace
     * @param userWorkspace
     */
    @Transactional
    public String changeUserWorkspace() {
    	if (userWorkspaceId == null)
    		return "error";

  		UserWorkspace uw = getEntityManager().find(UserWorkspace.class, userWorkspaceId);
  		if (uw == null)
  			return "error";

  		// register the user is leaving the current workspace
  		registerLogout();

  		// register the user is entering a new workspace
    	registerLogin(uw, false);
    	
    	getEntityManager().flush();

    	Events.instance().raiseEvent("change-workspace");
    	
    	return "workspacechanged";
    }

    
    
    /**
     * Tries to authenticate and register login using a previous session id given from the client side
     * @param sessionId
     * @return
     */
    protected UserLogin registerLogin(String sessionId, boolean statelessLogin) {
    	List<UserLogin> lst = getEntityManager()
    		.createQuery("from UserLogin ul " +
    				"join fetch ul.user join fetch ul.workspace " +
    				"where ul.sessionId = :ses")
    		.setParameter("ses", Passwords.hashPassword(sessionId))
    		.getResultList();
    	
    	if (lst.size() == 0)
    		return null;
    	
    	UserLogin userLogin = lst.get(0);
    	
    	List<UserWorkspace> lst2 = getEntityManager().createQuery("from UserWorkspace us where us.user.id = :userid and us.workspace.id = :wsid")
    		.setParameter("userid", userLogin.getUser().getId())
    		.setParameter("wsid", userLogin.getWorkspace().getId())
    		.getResultList();
    	
    	if (lst2.size() == 0)
    		return null;
    	
    	UserWorkspace userWorkspace = lst2.get(0);

    	initializeSession(userWorkspace, userLogin, statelessLogin);
    	
    	return userLogin;
    }


    /**
     * Register the user login in the database as an instance of the {@link UserLogin} class.
     * It also stores in the session information about the workspace
     */
    @Transactional
    protected UserLogin registerLogin(UserWorkspace userWorkspace, boolean statelessLogin) {
    	UserLogin userLogin = null;
    	if (statelessLogin) {
    		userLogin = tryRestorePreviousSession(userWorkspace);
    	}

    	if (userLogin == null) {
        	// get client information
        	ServletContexts servletContexts = ServletContexts.instance();
        	String ipAddr = null;
        	String app = null;
        	if (servletContexts != null) {
               	HttpServletRequest req = (HttpServletRequest) servletContexts.getRequest();
                ipAddr = req.getRemoteAddr();
                app = req.getHeader("User-Agent");
        	}

            // register new login
            userLogin = new UserLogin();
            userLogin.setUser(userWorkspace.getUser());
            userLogin.setLoginDate(new java.util.Date());

            if (app == null)
            	app = "Undefined";
            else
            if (app.length() > 200)
            	app = app.substring(0, 200);
            userLogin.setApplication(app);
            userLogin.setWorkspace(userWorkspace.getWorkspace());
            userLogin.setIpAddress(ipAddr);
            // this session id must be available to be returned to the client
            createNewSessionId(userLogin);

            // change the user workspace
            userWorkspace.getUser().setDefaultWorkspace( userWorkspace );
            
            getEntityManager().persist(userLogin);
    	}

    	initializeSession(userWorkspace, userLogin, statelessLogin);
    	
    	return userLogin;
    }

    
    /**
     * Initialize the user session. This method is called after the login authentication and registration and
     * initialize some common used session objects and other procedures
     * @param userWorkspace
     * @param userLogin
     * @param statelessLogin
     */
    private void initializeSession(UserWorkspace userWorkspace, UserLogin userLogin, boolean statelessLogin) {
        // put data in the session scope
    	Contexts.getSessionContext().set(SessionFactory.workspaceId, userWorkspace.getWorkspace().getId());
    	Contexts.getSessionContext().set(SessionFactory.userWorkspaceId, userWorkspace.getId());
    	Contexts.getSessionContext().set("userLogin", userLogin);
    	Contexts.getSessionContext().set("workspaceExtension", userWorkspace.getWorkspace().getExtension());
//    	Contexts.getSessionContext().set("tbunitselection", getTbunitselection());
    	
    	setTbunit(userWorkspace.getTbunit());
    	
    	//AK 20131111 set the admin unit id on the session level
    	setDefaultAdminUnit(userWorkspace);
    	
    	if (!statelessLogin) {
            // register user in the list of on-line users
            OnlineUsersHome onlineUsers = (OnlineUsersHome)Component.getInstance("onlineUsers");
            onlineUsers.add(userLogin);

    		updateUserRoleList(userWorkspace);

    		// persist user selected language
    		UserSettings settings = (UserSettings)Component.getInstance("userSettings");
    		settings.setLocaleString(LocaleSelector.instance().getLocaleString());
    	}
    }

    /**
     * Set the user admin unit id on the session level
     * @param userWorkspace
     */
	public static void setDefaultAdminUnit(UserWorkspace userWorkspace) {
		if (UserView.ADMINUNIT.equals(userWorkspace.getView())){
    		SessionData.instance().setValue("uaid", (userWorkspace.getAdminUnit() != null? userWorkspace.getAdminUnit().getId(): null));
    		SessionData.instance().setValue("unitid", (userWorkspace.getAdminUnit() != null? userWorkspace.getAdminUnit().getId(): null));
        }
		else 
			if (UserView.TBUNIT.equals(userWorkspace.getView())){
				SessionData.instance().setValue("uaid",(userWorkspace.getTbunit().getAdminUnit().getId()));
				SessionData.instance().setValue("unitid",(userWorkspace.getTbunit().getAdminUnit().getId()));
			}
	}

	/**
	 * Create a unique new session id to be used by clients application in reference to this session (even when the session is finished)
	 * @return unique identification 
	 */
	private String createNewSessionId(UserLogin userLogin) {
		UUID uid = UUID.randomUUID();
		sessionId = uid.toString();
		sessionId = sessionId.replace("-", "");
        userLogin.setSessionId( Passwords.hashPassword(sessionId) );

        return uid.toString();
	}


	/**
     * Try to restore the previous user session in order to not create a new {@link UserLogin} registration
     * based on established criterias like: Last login in than 12h, same user and workspace  
     * @param userWorkspace
     * @return
     */
    private UserLogin tryRestorePreviousSession(UserWorkspace userWorkspace) {
    	UserLogin userLogin = null; 
    	try {
        	userLogin = (UserLogin)getEntityManager()
            		.createQuery("from UserLogin ul join fetch ul.user u join fetch ul.workspace u " +
            		"where ul.id = (select max(aux.id) from UserLogin aux " +
            		"where aux.user.user.id = :userid " +
            		"and aux.workspace.id = :wsid " +
            		"and aux.logoutDate >= :dt and aux.sessionId is not null)")
            		.setParameter("userid", userWorkspace.getUser().getId())
            		.setParameter("wsid", userWorkspace.getWorkspace().getId())
            		.setParameter("dt", DateUtils.incHours(new Date(), -12))
            		.getResultList();
		} catch (Exception e) {
			return null;
		}

    	userLogin.setLogoutDate(new Date());
    	createNewSessionId(userLogin);

    	return userLogin;
	}


	/**
     * Register the logout of the current user
     */
    protected void registerLogout() {
    	EntityManager em = getEntityManager();
    	if (getUserLogin().getId() !=null){
	    	UserLogin userLogin = em.find(UserLogin.class, getUserLogin().getId() );
	    	userLogin.setLogoutDate(new Date());
	
	        OnlineUsersHome onlineUsers = (OnlineUsersHome)Component.getInstance("onlineUsers");
	        onlineUsers.remove(userLogin);
	
	        em.persist(userLogin);
	        em.flush();
    	}
    }

    
    /**
     * Mount the user permission list
     */
    public void updateUserRoleList(UserWorkspace uw) {
    	removePermissions();
    	
    	Identity identity = Identity.instance();
    	UserProfile prof = uw.getProfile();

    	List<Object[]> lst = getEntityManager().createQuery("select u.userRole.name, u.canChange, u.caseClassification " +
    			"from UserPermission u where u.userProfile.id = :id and u.canExecute = true")
    			.setParameter("id", prof.getId())
    			.getResultList();
    	
    	for (Object[] vals: lst) {
    		String roleName = (String)vals[0];
    		
    		CaseClassification classification = (CaseClassification)vals[2];

    		if (classification != null)
    			roleName = classification.toString() + "_" + roleName;
    		identity.addRole(roleName);
    		
    		boolean change = (Boolean)vals[1];
    		if (change) {
    			identity.addRole(roleName + "_EDT");
    		}
    	}
    }


    /**
     * Remove all user permissions for the current session (in memory operation)
     */
    protected void removePermissions() {
    	Identity identity = Identity.instance();
    	for (Group g: identity.getSubject().getPrincipals(Group.class)) {
    		if (g.getName().equals("Roles")) {
    			Enumeration e = g.members();
    			
    			List<Principal> members = new ArrayList<Principal>();
    			while (e.hasMoreElements()) {
    				Principal member = (Principal) e.nextElement();
    				members.add(member);
    			}

    			for (Principal p: members) {
    				g.removeMember(p);
    			}
    		}
    	}    	
    }



    /**
     * Return the id of the selected TB unit made by the user
     * @return
     */
    public Integer getTbunitId() {
    	return (Integer)Contexts.getSessionContext().get("tbunitId");
    }


	/**
	 * Return the selected workspace of the user
	 * @return
	 */
	public Tbunit getWorkingTbunit() {
		UserWorkspace uw = (UserWorkspace)Component.getInstance("userWorkspace");
		if (uw.isPlayOtherUnits())
			 return (Tbunit)Component.getInstance("selectedUnit");
		else return uw.getTbunit();
	}

	
	/**
	 * Check if the user can make transactions in the selected unit
	 * @return
	 */
	public boolean isSelectedUnitTransactional() {
		UserWorkspace uw = (UserWorkspace)Component.getInstance("userWorkspace");

		if (uw.isPlayOtherUnits())
			return true;
		else {
			Integer selid = (Integer)Contexts.getSessionContext().get(SessionFactory.selectedUnitId);
			if (selid == null)
				throw new RuntimeException("There is no information about the selected unit");
			return uw.getTbunit().getId().equals(selid);
		}
	}


	/**
	 * Return the selected unit of the current user
	 * @return
	 */
	public Tbunit getTbunit() {
		return (Tbunit)Component.getInstance("selectedUnit");
	}


	/**
	 * Change the selected unit of the current user
	 * @param tbunit
	 */
	public void setTbunit(Tbunit tbunit) {
		Contexts.getSessionContext().set(SessionFactory.selectedUnitId, tbunit.getId());
		Contexts.getConversationContext().set("selectedUnit", tbunit);
	}

	
	/**
	 * Check if TB unit has started medicine management 
	 * @return
	 */
	public boolean isMedicineManagementStarted() {
		Tbunit unit = getTbunit();
		return (unit.isMedicineManagementStarted());
	}

	public boolean isCanCheckReceiving() {
		return (Identity.instance().hasRole("RECEIV") && (getTbunit().isReceivingFromSource()));
	}


	public boolean isCanCheckDispensing() {
		return Identity.instance().hasRole("DISP_PAC") && (getTbunit().isMedicineStorage()) && (getTbunit().isTreatmentHealthUnit());
	}


	public boolean isCanCheckOrders() {
		return (Identity.instance().hasRole("ORDERS")) && (getTbunit().isMedicineManagementStarted());
	}


	/**
	 * Check if user can view the estimated position report
	 * @return
	 */
	public boolean isCanViewEstPositionReport() {
		return (Identity.instance().hasRole("REP_ESTPOS")) && (getTbunit().isMedicineManagementStarted());		
	}

	
	/**
	 * Check if user can view the stock evolution report
	 * @return
	 */
	public boolean  isCanViewStockEvolutionReport() {
		return (Identity.instance().hasRole("REP_STOCKEVOL")) && (getTbunit().isMedicineManagementStarted());				
	}
	
	/**
	 * Check if user can view the cost of treatment per patient report
	 * @return
	 */
	public boolean  isCanViewCostPatientReport() {
		return (Identity.instance().hasRole("REP_COSTPAT")) && (getTbunit().isMedicineManagementStarted());				
	}

	public boolean isCanCheckMovements() {
		return (Identity.instance().hasRole("MOVS") && (getTbunit().isMedicineStorage())) && (getTbunit().isMedicineManagementStarted());
	}


	public boolean isCanCheckTransfers() {
		return ((Identity.instance().hasRole("TRANSFER")) && (getTbunit().isMedicineManagementStarted()));
	}


	public boolean isCanCheckForecasting() {
		return true;
	}


	/**
	 * Return true if user can adjust the stock
	 * @return
	 */
	public boolean isCanAdjustStock() {
		return Identity.instance().hasRole("STOCKPOS") && (getTbunit().isMedicineStorage()) && (isSelectedUnitTransactional());
	}


	/**
	 * Return true if user can include TB unit in medicine management control
	 * @return
	 */
	public boolean isCanSetupUnit() {
		return Identity.instance().hasRole("UNITSETUP") && (getTbunit().isMedicineStorage()) && (isSelectedUnitTransactional());
	}


	public boolean isCanCreateOrder() {
		Tbunit u = getTbunit(); 
		return Identity.instance().hasRole("NEW_ORDER") && u.isMedicineStorage() && (getWorkingTbunit().equals(u)) && 
				((u.getFirstLineSupplier() != null) || (u.getSecondLineSupplier() != null));
	}


	/**
	 * Return true if the user can open TB or MDR cases
	 * @return
	 */
	public boolean isCanOpenCases() {
		return checkRoleAnyClassification("CASE_VIEW");
	}


	/**
	 * Check if a specific role name is allowed to the current user for any classification
	 * @param roleName
	 * @return
	 */
	public boolean checkRoleAnyClassification(String roleName) {
		for (CaseClassification cla: CaseClassification.values()) {
			if (Identity.instance().hasRole(cla.toString() + "_" + roleName))
				return true;
		}
		return false;
	}


	/**
	 * Return true if the user can edit TB or MDR cases
	 * @return
	 */
	public boolean isCanEditCases() {
		return checkRoleAnyClassification("CASE_DATA_EDT");
	}


	/**
	 * Check if user can notify or change data of more than one case classification
	 * @return true if so
	 */
	public boolean isCanNotifySeveralClassifs() {
		Identity id = Identity.instance();
		int count = 0;
		for (CaseClassification classif: CaseClassification.values()) {
			String roleName = classif.toString() + "_CASE_DATA_EDT";
			if (id.hasRole(roleName))
				count++;
		}
		return count > 1;
	}




	/**
	 * Check if user can notify or change data of a specific classification
	 * @param classif
	 * @return true if user can notify more than one classification
	 */
	public boolean isCanEditCaseByClassification(CaseClassification classif) {
		return Identity.instance().hasRole(classif.toString() + "_CASE_DATA_EDT");
	}

	
	/**
	 * Check if a specific case classification can be opened by the user 
	 * @param classif
	 * @return
	 */
	public boolean isCanOpenCaseByClassification(CaseClassification classif) {
		return Identity.instance().hasRole(classif.toString() + "_CASE_DATA");
	}
	
	/**
	 * Check if a user can generate movements in the selected unit according to the limit date to generate movements 
	 * The movement has to be at the same date of limitDateMedicineMovementor or after, or the user has to have
	 * permission to generate movements before the limit date.
	 * based on the date passed as parameter.
	 * @param movementDate 
	 * @return
	 */
	public boolean isCanGenerateMovements(Date movementDate) {
		return isCanGenerateMovements(movementDate, getTbunit());
	}
	
	/**
	 * Check if a user can generate movements in the unit passed as param according to the limit date to generate movements 
	 * The movement has to be at the same date of limitDateMedicineMovementor or after, or the user has to have
	 * permission to generate movements before the limit date.
	 * based on the date passed as parameter.
	 * @param movementDate 
	 * @param unit 
	 * @return
	 */
	public static boolean isCanGenerateMovements(Date movementDate, Tbunit unit) {
		if(unit == null || movementDate == null)
			return false;
		
		//no limit specified
		if(unit.getLimitDateMedicineMovement() == null)
			return true;
		
		//Movement date is equals or after the limit date
		if(movementDate.compareTo(unit.getLimitDateMedicineMovement()) >= 0)
			return true;
						
		//Movement date is not equals or after the limit date so the user has to have permission
		return Identity.instance().hasRole("MED_MOV_EDIT_OUT_PERIOD");
	}
	
	
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	/**
	 * Set the current user workspace id
	 * @param id
	 */
	public void setUserWorkspaceId(Integer id) {
		userWorkspaceId = id;
	}


	/**
	 * Get the current user workspace id
	 * @return
	 */
	public Integer getUserWorkspaceId() {
		return userWorkspaceId;
	}


	/**
	 * @return the initingList
	 */
	public boolean isInitingList() {
		return initingList;
	}


	/**
	 * @param initingList the initingList to set
	 */
	public void setInitingList(boolean initingList) {
		this.initingList = initingList;
	}

	
	public static UserSession instance() {
		return (UserSession)Component.getInstance("userSession");
	}


	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null) {
			tbunitselection = new TBUnitSelection("unitid");
		}
		return tbunitselection;
	}

	public String getAdminUnitCodeLike() {
		AdministrativeUnit adm = tbunitselection.getAdminUnit();
		if (adm == null)
			 return null;
		else return adm.getCode() + "%";
	}

	public String acceptULA(){
		UserLogin userLogin = getUserLogin();
		User user = userLogin.getUser();
		user.setUlaAccepted(true);
		getEntityManager().createQuery("update User set ulaAccepted = :val where id = :id")
		.setParameter("val", true)
		.setParameter("id", user.getId())
		.executeUpdate();
		getEntityManager().flush();
		return "ulaaccepted";
	}
}
