package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Regimen;
import org.msh.utils.EntityQuery;


@Name("regimens")
public class RegimensQuery extends EntityQuery<Regimen> {
	private static final long serialVersionUID = -3330592042727005660L;

	@Override
	public String getEjbql() {
		return "from Regimen r where r.workspace.id = #{defaultWorkspace.id}";
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Regimen r where r.workspace.id = #{defaultWorkspace.id}";
	}

	@Override
	public String getOrder() {
		return "r.name";
	}

}
