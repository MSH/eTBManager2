package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ResistancePattern;
import org.msh.utils.EntityQuery;


/**
 * Generates a list of resistance patterns
 * @author Ricardo Memoria
 *
 */
@Name("resistancePatterns")
public class ResistancePatternsQuery extends EntityQuery<ResistancePattern> {
	private static final long serialVersionUID = 8462763616579602856L;

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from ResistancePattern r where r.workspace.id = #{defaultWorkspace.id}";
	}

}
