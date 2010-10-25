package org.msh.tb.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.UserPermission;
import org.msh.mdrtb.entities.UserProfile;
import org.msh.mdrtb.entities.UserRole;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.log.LogInfo;
import org.msh.utils.EntityQuery;


@Name("profileHome")
@LogInfo(roleName="PROFILES")
public class UserProfileHome extends EntityHomeEx<UserProfile>{
	private static final long serialVersionUID = 5248239769157758881L;
	
	@In(create=true) FacesMessages facesMessages;
	
	private List<UserPermission> permissions;

	@Factory("profile")
	public UserProfile getUserProfile() {
		return getInstance();
	}

	@Override
	public String persist() {
		getInstance().setPermissions(getItems());
		return super.persist();
	}
	
	public List<UserPermission> getItems() {
		UserProfile userProfile = getInstance();
		
		if (permissions == null) {
			permissions = new ArrayList<UserPermission>();

			List<UserRole> userRoles = getRoles();
			
			for (UserRole ur: userRoles) {
				UserPermission up = userProfile.permissionByRole(ur);
				if (up == null) {
					up = new UserPermission();
					up.setUserRole(ur);
				}
				up.setUserProfile(userProfile);

				permissions.add(up);
			}
		}

		Collections.sort(permissions);
		
		return permissions;
	}


	/**
	 * Return the roles that user can grant permission
	 * @return
	 */
	public List<UserRole> getRoles() {
		return getEntityManager().createQuery("select p.userRole from UserPermission p " +
				"where p.userProfile.id = #{userWorkspace.profile.id} " +
				"and p.grantPermission = true and p.userRole.internalUse = false")
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
