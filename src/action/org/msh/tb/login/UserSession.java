package org.msh.tb.login;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.Identity;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserProfile;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;


/**
 * Hold information about a user session
 * @author Ricardo Memoria
 *
 */
@Name("userSession")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=20000)
public class UserSession {

	private Tbunit tbunit;
	private UserLogin userLogin;
	private UserWorkspace userWorkspace;
	private Integer workspaceId;
	private boolean displayMessagesKeys;
	private boolean initingList;


    /**
     * Return the workspace in use
     * @return {@link Workspace} instance
     */
    @Factory(value="defaultWorkspace", autoCreate=true, scope=ScopeType.EVENT)
    public Workspace getDefaultWorkspace() {
    	if (workspaceId == null)
    		return null;

		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		
		if (entityManager == null)
			return null;
		
		return (Workspace)entityManager.createQuery("from Workspace where id = :id")
			.setParameter("id", workspaceId)
			.getSingleResult();
    }


	/**
	 * @return the userLogin
	 */
	@Factory("userLogin")
	public UserLogin getUserLogin() {
		return userLogin;
	}


	/**
	 * @return the userWorkspace
	 */
	@Factory("userWorkspace")
	public UserWorkspace getUserWorkspace() {
		return userWorkspace;
	}


	/**
	 * Initialize the user session when the workspace is changed 
	 */
	@Create
	public void initialize() {
		if (userLogin == null)
			return;
		tbunit = getEntityManager().merge(userWorkspace.getTbunit());
		tbunit.getAdminUnit().getParents();
		tbunit.getHealthSystem().getName();
	}


    /**
     * Register the logout when the user session is finished by time-out
     */
    @Observer("org.jboss.seam.preDestroyContext.SESSION")
    @Transactional
    public void logout() {
    	if (userLogin == null) {
    		return;
    	}

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
    	if (userWorkspace == null)
    		return "error";

    	if (userLogin != null)
    		registerLogout();

    	userWorkspace.getUser().setDefaultWorkspace(userWorkspace);

    	registerLogin();
    	initialize();
    	updateUserRoleList();
    	
    	workspaceId = userLogin.getDefaultWorkspace().getId();
    	
    	Contexts.getSessionContext().set("userLogin", userLogin);
    	Contexts.getSessionContext().set("workspaceExtension", userWorkspace.getWorkspace().getExtension());
    	Contexts.getSessionContext().set("userWorkspace", userWorkspace);
    	
    	getEntityManager().flush();

    	Events.instance().raiseEvent("change-workspace");
    	
    	return "workspacechanged";
    }

    
    /**
     * Register the user login
     */
    protected void registerLogin() {
    	// get client information
    	FacesContext facesContext = FacesContext.getCurrentInstance();
       	HttpServletRequest req = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        String ipAddr = req.getRemoteAddr();
        String app = req.getHeader("User-Agent");

        // register new login        
        userLogin = new UserLogin();
        userLogin.setUser(userWorkspace.getUser());
        userLogin.setLoginDate(new java.util.Date());
        if (app.length() > 200)
        	app = app.substring(0, 200);
        userLogin.setApplication(app);
        userLogin.setWorkspace(userWorkspace.getWorkspace());
        userLogin.setIpAddress(ipAddr);

        OnlineUsersHome onlineUsers = (OnlineUsersHome)Component.getInstance("onlineUsers");
        onlineUsers.add(userLogin);
        
        getEntityManager().persist(userLogin);    	
    }


    /**
     * Register the logout of the current user
     */
    protected void registerLogout() {
    	EntityManager em = getEntityManager();
    	
    	userLogin = em.merge(userLogin);
    	userLogin.setLogoutDate(new Date());

        OnlineUsersHome onlineUsers = (OnlineUsersHome)Component.getInstance("onlineUsers");
        onlineUsers.remove(userLogin);

        em.persist(userLogin);
        em.flush();
    }

    
    /**
     * Mount the user permission list
     */
    public void updateUserRoleList() {
    	removePermissions();
    	
    	Identity identity = Identity.instance();
    	UserProfile prof = userWorkspace.getProfile();

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




	public Tbunit getWorkingTbunit() {
		UserWorkspace uw = userLogin.getUser().getDefaultWorkspace();
		if (uw.isPlayOtherUnits())
			 return tbunit;
		else return uw.getTbunit();
	}


	public Tbunit getTbunit() {
		return tbunit;
	}


	public void setTbunit(Tbunit tbunit) {
		tbunit.getAdminUnit().getParents();
		this.tbunit = tbunit;
	}

	
	/**
	 * Check if TB unit has started medicine management 
	 * @return
	 */
	public boolean isMedicineManagementStarted() {
		return (getTbunit().isMedicineManagementStarted());
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


	public boolean isCanAdjustStock() {
		return Identity.instance().hasRole("STOCKPOS_EDT") && (getTbunit().isMedicineStorage()) && (getWorkingTbunit().equals(tbunit));
	}


	public boolean isCanSetupUnit() {
		return Identity.instance().hasRole("UNITSETUP") && (getTbunit().isMedicineStorage()) && (getWorkingTbunit().equals(tbunit));
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
	 * @param userLogin the userLogin to set
	 */
	public void setUserLogin(UserLogin userLogin) {
		this.userLogin = userLogin;
	}

	/**
	 * @param userWorkspace the userWorkspace to set
	 */
	public void setUserWorkspace(UserWorkspace userWorkspace) {
		this.userWorkspace = userWorkspace;
	}


	public boolean isDisplayMessagesKeys() {
		return displayMessagesKeys;
	}


	public void setDisplayMessagesKeys(boolean displayMessagesKeys) {
		this.displayMessagesKeys = displayMessagesKeys;
	}
	
	
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	/**
	 * Set the current user workspace id
	 * @param id
	 */
	public void setUserWorkspaceId(Integer id) {
		if (id == null)
			 userWorkspace = null;
		else userWorkspace = getEntityManager().find(UserWorkspace.class, id);
	}


	/**
	 * Get the current user workspace id
	 * @return
	 */
	public Integer getUserWorkspaceId() {
		return (userWorkspace == null? null: userWorkspace.getId());
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


	/**
	 * @return the workspaceId
	 */
	public Integer getWorkspaceId() {
		return workspaceId;
	}


	/**
	 * @param workspaceId the workspaceId to set
	 */
	public void setWorkspaceId(Integer workspaceId) {
		this.workspaceId = workspaceId;
	}

	
	public static UserSession instance() {
		return (UserSession)Component.getInstance("userSession");
	}
}
