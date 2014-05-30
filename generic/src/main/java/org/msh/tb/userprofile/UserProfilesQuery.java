package org.msh.tb.userprofile;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.UserProfile;
import org.msh.utils.EntityQuery;


@Name("profiles")
public class UserProfilesQuery extends EntityQuery<UserProfile>{
	private static final long serialVersionUID = -121818224036988157L;

	private static final String[] restrictions = {
		"p.workspace.id = #{defaultWorkspace.id}",
		"not exists(select perm.id from UserPermission perm " +
				"where perm.userProfile.id = p.id " +
				"and perm.userRole.id not in (select r.id from UserPermission aux " +
				"join aux.userRole r where aux.grantPermission = true " +
				"and aux.userProfile.id = #{userWorkspace.profile.id}))"
	};
	
	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from UserProfile p"; 
	}



	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}	
}
