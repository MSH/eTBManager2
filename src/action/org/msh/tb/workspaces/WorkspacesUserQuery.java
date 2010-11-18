package org.msh.tb.workspaces;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.Workspace;
import org.msh.utils.EntityQuery;

@Name("workspacesUser")
@BypassInterceptors
public class WorkspacesUserQuery extends EntityQuery<Workspace> {
	private static final long serialVersionUID = 859882528182398990L;

	private static final String[] restrictions = {"w.id in (select aux.workspace.id from UserWorkspace aux where aux.user.id = #{userLogin.user.id})" };

	@Override
	public String getEjbql() {
		return "from Workspace w";
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Workspace w";
	}

	@Override
	public String getOrder() {
		return "w.name";
	}

	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
}
