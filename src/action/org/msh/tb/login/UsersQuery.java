package org.msh.tb.login;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserWorkspace;
import org.msh.utils.EntityQuery;


@Name("users")
public class UsersQuery extends EntityQuery<UserWorkspace>{
	private static final long serialVersionUID = -8293352124405808033L;

	private static final String[] restrictions = {
		"uw.workspace.id = #{defaultWorkspace.id}",
		"not exists(select perm.id from UserPermission perm " +
				"where perm.userProfile.id = uw.profile.id " +
				"and perm.userRole.id not in (select r.id from UserPermission aux " +
				"join aux.userRole r where aux.grantPermission = true " +
				"and aux.userProfile.id = #{userWorkspace.profile.id}))"
	};
	
	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from UserWorkspace uw join fetch uw.user left join fetch uw.profile join fetch uw.tbunit left join fetch uw.adminUnit";
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from UserWorkspace uw ";
	}

	
	
	/**
	 * Search for an instance of an {@link UserWorkspace} class by its user
	 * @param user
	 * @return {@link UserWorkspace} instance, or null if there is no instance assigned to the user
	 */
	public UserWorkspace getInstanceByUser(User user) {
		for (UserWorkspace uw: getResultList()) {
			if (uw.getUser().equals(user))
				return uw;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getMaxResults()
	 */
	@Override
	public Integer getMaxResults() {
		Integer maxresults = super.getMaxResults();
		if (maxresults == null)
			 return 25;
		else return super.getMaxResults();
	}
}
