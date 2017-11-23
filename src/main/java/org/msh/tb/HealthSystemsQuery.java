package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.HealthSystem;
import org.msh.utils.EntityQuery;

import java.util.Arrays;
import java.util.List;


@Name("healthSystems")
public class HealthSystemsQuery extends EntityQuery<HealthSystem> {
	private static final long serialVersionUID = -3299153474416894947L;

	private static final String[] restrictions = {"h.workspace.id = #{defaultWorkspace.id}"};
	
	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from HealthSystem h";
	}


	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
	
}
