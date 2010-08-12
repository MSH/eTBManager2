package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.utils.EntityQuery;


@Name("allunits")
public class AllUnitsQuery extends EntityQuery<Tbunit> {
	private static final long serialVersionUID = -7926445472130250300L;


	@Override
	public String getEjbql() {
		return "from Tbunit u where u.workspace.id = #{defaultWorkspace.id}";
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Tbunit u where u.workspace.id = #{defaultWorkspace.id}";
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			 return "u.name.name1";
		else return s;
	}
}
