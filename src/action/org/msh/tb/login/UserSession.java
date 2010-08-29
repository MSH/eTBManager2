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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserProfile;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.UserView;


/**
 * Hold information about a user session
 * @author Ricardo Memoria
 *
 */
@Name("userSession")
@Scope(ScopeType.SESSION)
public class UserSession {

	private Tbunit tbunit;
	private UserLogin userLogin;
	private UserWorkspace userWorkspace;
	private boolean displayMessagesKeys;
	
	@In(create=true) EntityManager entityManager;
	@In(create=true) OnlineUsersHome onlineUsers;


    /**
     * Return the workspace in use
     * @return {@link Workspace} instance
     */
    @Factory("defaultWorkspace")
    @BypassInterceptors
    public Workspace getDefaultWorkspace() {
    	return (userWorkspace != null? userWorkspace.getWorkspace(): null);
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
		tbunit = entityManager.merge(userWorkspace.getTbunit());
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
     * Change the user workspace
     * @param userWorkspace
     */
    @Transactional
	@RaiseEvent("change-workspace")
    public String changeUserWorkspace() {
    	if (userWorkspace == null)
    		return "error";

    	if (userLogin != null)
    		registerLogout();

    	userWorkspace.getTbunit().getAdminUnit().getParentsTreeList(true);
    	if (userWorkspace.getAdminUnit() != null)
    		userWorkspace.getAdminUnit().getId();
    	userWorkspace.getUser().setDefaultWorkspace(userWorkspace);

    	registerLogin();
    	initialize();
    	mountRoleList();
    	
    	Contexts.getSessionContext().set("userLogin", userLogin);
    	Contexts.getSessionContext().set("defaultWorkspace", userWorkspace.getWorkspace());
    	Contexts.getSessionContext().set("userWorkspace", userWorkspace);
    	
    	entityManager.flush();
    	
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
        
        entityManager.persist(userLogin);    	
    }


    /**
     * Register the logout of the current user
     */
    protected void registerLogout() {
    	userLogin = entityManager.merge(userLogin);
    	userLogin.setLogoutDate(new Date());

        OnlineUsersHome onlineUsers = (OnlineUsersHome)Component.getInstance("onlineUsers");
        onlineUsers.remove(userLogin);

        entityManager.persist(userLogin);
        entityManager.flush();
    }

    
    /**
     * Monta a lista de permiss�es do usu�rio
     * @param usu
     */
    protected void mountRoleList() {
    	removePermissions();
    	
    	Identity identity = Identity.instance();
    	UserProfile prof = userWorkspace.getProfile();

    	// restriction to case manager. If user has a vision by TB Unit, and this TB Unit isn't a health facility, 
    	// so this user can't access the case management module
    	boolean useCaseMan = true;
    	if (userWorkspace.getView() == UserView.TBUNIT) 
    		useCaseMan = userWorkspace.getTbunit().isTreatmentHealthUnit(); 
    	
    	List<Object[]> lst = entityManager.createQuery("select u.userRole.name, u.canChange " +
    			"from UserPermission u where u.userProfile.id = :id and (u.canExecute = true or u.canOpen = true)")
    			.setParameter("id", prof.getId())
    			.getResultList();
    	
    	for (Object[] vals: lst) {
    		String roleName = (String)vals[0];

    		if (roleName.equals("CASEMAN")) {
    			if (useCaseMan)
    				identity.addRole(roleName);
    		}
    		else identity.addRole(roleName);
    	
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
	 * Return if user can play activities not only of his unit, but with other units
	 * @return true if user can play activities of other units
	 */
	public boolean isCanPlayOrderUnits() {
		return userLogin.getUser().getDefaultWorkspace().isPlayOtherUnits();
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


	public boolean isCanCheckReceiving() {
		return (Identity.instance().hasRole("RECEIV") && (getTbunit().isReceivingFromSource()));
	}


	public boolean isCanCheckStockPos() {
		return Identity.instance().hasRole("STOCKPOS") && (getTbunit().isMedicineStorage());
	}


	public boolean isCanCheckDispensing() {
		return Identity.instance().hasRole("DISP_PAC") && (getTbunit().isMedicineStorage()) && (getTbunit().isTreatmentHealthUnit());
	}


	public boolean isCanCheckOrders() {
		return Identity.instance().hasRole("ORDERS");
	}


	public boolean isCanCheckMovements() {
		return Identity.instance().hasRole("MOVS") && (getTbunit().isMedicineStorage());
	}


	public boolean isCanCheckTransfers() {
		return Identity.instance().hasRole("TRANSFER") && (getTbunit().isMedicineStorage());
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
		return Identity.instance().hasRole("ORDERS_EDT") && u.isMedicineStorage() && (getWorkingTbunit().equals(u)) && 
				((u.getFirstLineSupplier() != null) || (u.getSecondLineSupplier() != null));
	}


	/**
	 * Return true if the user can open TB or MDR cases
	 * @return
	 */
	public boolean isCanOpenCases() {
		return Identity.instance().hasRole("TBCASES") || Identity.instance().hasRole("MDRCASES");
	}

	
	/**
	 * Return true if the user can edit TB or MDR cases
	 * @return
	 */
	public boolean isCanEditCases() {
		return Identity.instance().hasRole("TBCASES_EDT") || Identity.instance().hasRole("MDRCASES_EDT");
	}


	/**
	 * Check if the user can open laboratory exam results
	 * @return true if the user can view results
	 */
	public boolean isCanOpenExams() {
		return Identity.instance().hasRole("TBEXAMS") || Identity.instance().hasRole("MDREXAMS");
	}


	/**
	 * Check if the user can edit laboratory exam results
	 * @return true if user can edit results
	 */
	public boolean isCanEditExams() {
		return Identity.instance().hasRole("TBEXAMS_EDT") || Identity.instance().hasRole("MDREXAMS_EDT");
	}


	/**
	 * Check if user can open treatment regimens of a case
	 * @return true if user can open treatment
	 */
	public boolean isCanOpenTreatment() {
		return Identity.instance().hasRole("TBTREAT") || Identity.instance().hasRole("MDRTREAT");
	}

	
	/**
	 * Check if user can edit treatment regimen of a case
	 * @return true if user can edit the treatment
	 */
	public boolean isCanEditTreatment() {
		return Identity.instance().hasRole("TBTREAT_EDT") || Identity.instance().hasRole("MDRTREAT_EDT");
	}


	/**
	 * Check if user can change data of more than one case classification
	 * @return true if so
	 */
	public boolean isCanEditSeveralClassifs() {
		Identity id = Identity.instance();
		int count = 0;
		for (CaseClassification classif: CaseClassification.values()) {
			if (id.hasRole(classif.getUserroleChange()))
				count++;
		}
		return count > 1;
	}

	
	/**
	 * Check if user can validate cases
	 * @return
	 */
	public boolean isCanValidateCases() {
		return Identity.instance().hasRole("MDRVALIDATE") || Identity.instance().hasRole("TBVALIDATE");
	}


	/**
	 * Check if user can notify more than one case classification, for instance, TB and DR-TB
	 * @param classif
	 * @return true if user can notify more than one classification
	 */
	public boolean isCanNotifyOnlyOneClassif(CaseClassification classif) {
		Identity id = Identity.instance();
		for (CaseClassification c: CaseClassification.values()) {
			if ((c != classif) && (id.hasRole(c.getUserroleChange())))
				return false;
		}
		return true;
	}
	

	/**
	 * Check if user can notify only TB cases
	 * @return true if so
	 */
	public boolean isCanNotifyOnlyTB() {
		return isCanNotifyOnlyOneClassif(CaseClassification.TB_DOCUMENTED);
	}

	
	/**
	 * Check if user can notify only MDR cases
	 * @return true if so
	 */
	public boolean isCanNotifyOnlyMDR() {
		return isCanNotifyOnlyOneClassif(CaseClassification.MDRTB_DOCUMENTED);
	}

	
	/**
	 * Check if user can notify only NMT cases
	 * @return
	 */
	public boolean isCanNotifyOnlyNMT() {
		return isCanNotifyOnlyOneClassif(CaseClassification.NMT);
	}

	/**
	 * Check if can open both TB and MDRTB cases
	 * @return
	 */
	public boolean isCanOpenTBMDRTB() { 
		return Identity.instance().hasRole("MDRCASES") && Identity.instance().hasRole("TBCASES");
	}

	
	/**
	 * Check if user can open TB cases
	 * @return
	 */
	public boolean isCanOpenTBCases() {
		return Identity.instance().hasRole("TBCASES");
	}

	
	/**
	 * Check if user can open MDR-TB cases
	 * @return
	 */
	public boolean isCanOpenMDRTBCases() {
		return Identity.instance().hasRole("MDRCASES");
	}

	
	/**
	 * Check if can transfer MDR or TB cases
	 * @return
	 */
	public boolean isCanTransferCases() {
		return Identity.instance().hasRole("MDRTRANSFER") || Identity.instance().hasRole("TBTRANSFER");
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

}
