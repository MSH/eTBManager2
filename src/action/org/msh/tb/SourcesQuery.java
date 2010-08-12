package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Source;
import org.msh.utils.EntityQuery;


@Name("sources")
public class SourcesQuery extends EntityQuery<Source>{
	private static final long serialVersionUID = 7087727728001563620L;

	@Override
	public String getEjbql() {
		return "from Source s where s.workspace.id = #{defaultWorkspace.id}";
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Source s where s.workspace.id = #{defaultWorkspace.id}";
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			 return "s.name";
		else return s;
	}

}
