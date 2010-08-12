package org.msh.tb.login;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.utils.EntityQuery;


@Name("users")
public class UsersQuery extends EntityQuery<UsersQuery>{
	private static final long serialVersionUID = -8293352124405808033L;

	private static final String[] restrictions = {
		"uw.workspace.id = #{defaultWorkspace.id}",
		"not exists(select perm.id from UserPermission perm " +
				"where perm.userProfile.id = uw.profile.id " +
				"and (perm.canOpen = true or perm.canExecute = true) " +
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

}
