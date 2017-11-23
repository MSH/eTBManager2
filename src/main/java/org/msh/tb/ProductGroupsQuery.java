package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ProductGroup;
import org.msh.utils.EntityQuery;

import java.util.Arrays;
import java.util.List;


@Name("productGroups")
public class ProductGroupsQuery extends EntityQuery<ProductGroup> {
	private static final long serialVersionUID = 2970270375889729419L;

	private static final String[] restrictions = {"grp.workspace.id = #{defaultWorkspace.id}"};

	@Override
	public String getEjbql() {
		return "from ProductGroup grp";
	}


	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			return "grp.code";
		else return s;
	}


	@Override
	protected String getCountEjbql() {
		return "select count(*) from ProductGroup grp";
	}

	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

}
