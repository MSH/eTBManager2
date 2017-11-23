package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Tag;
import org.msh.utils.EntityQuery;

@Name("tagsQuery")
public class TagsQuery extends EntityQuery<Tag>{
	private static final long serialVersionUID = 5745910901093575339L;


	@Override
	public String getEjbql() {
		return "from Tag where workspace.id = #{defaultWorkspace.id}";
	}


	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			 return "name";
		else return s;
	}


	@Override
	protected String getCountEjbql() {
		return "select count(*) from Tag where workspace.id = #{defaultWorkspace.id}";
	}

}
