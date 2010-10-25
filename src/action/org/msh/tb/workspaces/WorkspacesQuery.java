package org.msh.tb.workspaces;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Workspace;
import org.msh.utils.EntityQuery;

@Name("workspaces")
public class WorkspacesQuery extends EntityQuery<Workspace> {
	private static final long serialVersionUID = 859882528182398990L;

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

}
