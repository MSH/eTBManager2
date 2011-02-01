package org.msh.tb.userprofile;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.UserPermission;
import org.msh.mdrtb.entities.UserProfile;
import org.msh.mdrtb.entities.UserRole;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.log.LogInfo;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;


@Name("profileHome")
@LogInfo(roleName="PROFILES")
public class UserProfileHome extends EntityHomeEx<UserProfile>{
	private static final long serialVersionUID = 5248239769157758881L;
	
	@In(create=true) FacesMessages facesMessages;
	
//	private List<UserPermission> permissions;
	private UserProfile userProfile;
	
	private List<PermissionNode> nodes;

	@Factory("profile")
	public UserProfile getUserProfile() {
		return getInstance();
	}

	@Override
	public String persist() {
		userProfile = getInstance();
		mountPermissions(null, nodes);
		
		String s = super.persist();
		
		checkUserProfile();
		
		return s;
	}

	
	/**
	 * Mount list of permissions based on a list of nodes
	 * @param lst
	 */
	protected void mountPermissions(UserPermission parent, List<PermissionNode> lst) {
		EntityManager em = getEntityManager();
		
		for (PermissionNode node: lst) {
			UserPermission perm = node.getPermission();
			
			// node is checked ?
			if (node.isChecked() || (node.getPermission().isGrantPermission())) {
				// if parent can't be executed, then child won't be executed too 
				if ((parent != null) && (!parent.isCanExecute()))
					 perm.setCanExecute(false);
				else perm.setCanExecute(node.isChecked());

				if (!userProfile.getPermissions().contains(perm)) {
					userProfile.getPermissions().add(perm);
					perm.setUserProfile(userProfile);
				}
				mountPermissions(perm, node.getChildren());
			}
			else {
				// remove permission from user profile 
				if (userProfile.getPermissions().contains(perm)) {
					userProfile.getPermissions().remove(perm);
					if (em.contains(perm))
						em.remove(perm);
				}

				// remove children of the permission
				List<UserPermission> lsttemp = new ArrayList<UserPermission>();
				for (UserPermission aux: userProfile.getPermissions())
					if ((aux.getCaseClassification() == perm.getCaseClassification()) && (aux.getUserRole().isChildOf(perm.getUserRole()))) 
						lsttemp.add(aux);

				for (UserPermission aux: lsttemp) {
					userProfile.getPermissions().remove(aux);
					if (em.contains(aux))
						em.remove(aux);
				}
			}
		}
	}
	
	
	/**
	 * Return the list of nodes of permissions
	 * @return
	 */
	public List<PermissionNode> getNodes() {
		if (nodes == null)
			createNodes();
		return nodes;
	}

	
	/**
	 * Create the node tree
	 */
	public void createNodes() {
		List<UserPermission> lst = getRoles();
		
		nodes = new ArrayList<PermissionNode>();
		userProfile = getInstance();
		
		for (UserPermission perm: lst) {
			UserRole role = perm.getUserRole();
			if (role.getLevel() == 1) {
				PermissionNode node = newNode(null, role, perm.getCaseClassification());
				addChildren(node, perm.getCaseClassification(), lst);
			}
		}
	}

	
	/**
	 * Add a new permission node to the hierarchy of permissions
	 * @param parent
	 * @param role
	 * @param classif
	 * @return
	 */
	private PermissionNode newNode(PermissionNode parent, UserRole role, CaseClassification classif) {
		UserPermission per = userProfile.permissionByRole(role, classif);
		if (per == null) {
			per = new UserPermission();
			per.setUserRole(role);
			per.setCaseClassification(classif);
		}
		
		PermissionNode node = new PermissionNode(parent, per, classif);
		node.setChecked(per.isCanExecute());
		if (parent == null)
			 nodes.add(node);
		
		return node;
	}
	

	/**
	 * Add the children nodes of the given node
	 * @param node
	 * @param classif
	 * @param lst
	 */
	private void addChildren(PermissionNode node, CaseClassification classif, List<UserPermission> lst) {
		for (UserPermission item: lst) {
			String parentCode = item.getUserRole().getParentCode();
			String code = node.getPermission().getUserRole().getCode();
			if ((parentCode != null) && (parentCode.equals(code)) && 
				((classif == null) || ((classif != null)&&(classif == item.getCaseClassification())))) {
				PermissionNode aux = newNode(node, item.getUserRole(), item.getCaseClassification());
				addChildren(aux, item.getCaseClassification(), lst);
			}
		}
	}

	
	/**
	 * Check if profile changed is the current user profile. If so, the list of user roles is updated 
	 */
	private void checkUserProfile() {
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (userWorkspace == null)
			return;
		
		if (!userWorkspace.getProfile().getId().equals(getInstance().getId()))
			return;
		
		UserSession userSession = (UserSession)Component.getInstance("userSession");
		userSession.updateUserRoleList();
	}
	

	/**
	 * Return the roles that user can grant permission
	 * @return
	 */
	public List<UserPermission> getRoles() {
		return getEntityManager().createQuery("from UserPermission p " +
				"where p.userProfile.id = #{userWorkspace.profile.id} " +
				"and p.grantPermission = true and p.userRole.internalUse = false " +
				"order by p.userRole.code, p.caseClassification")
				.getResultList();
	}
	
	@Override
	public String remove() {
		if (!isManaged())
			return "error";
	
		// check if profile can be deleted
		UserWorkspace userWorkspace = getUserWorkspace();
		if (userWorkspace.getProfile().equals(getInstance())) {
			facesMessages.addFromResourceBundle("admin.profiles.delerror1");
			return "error";
		}
		
		return super.remove();
	}
	
	@Override
	public EntityQuery<UserProfile> getEntityQuery() {
		return (UserProfilesQuery)Component.getInstance("profiles", false);
	}
}
