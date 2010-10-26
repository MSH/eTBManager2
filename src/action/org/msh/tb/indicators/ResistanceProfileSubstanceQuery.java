package org.msh.tb.indicators;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Substance;
import org.msh.utils.EntityQuery;


/**
 * Generates a list of resistance pattern for the resistance profile report.
 * @author Utkarsh Srivastava
 *
 */
@Name("resistanceProfileSubstance")
public class ResistanceProfileSubstanceQuery extends EntityQuery<Substance> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -1858132444362207201L;

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from Substance r where r.workspace.id = #{defaultWorkspace.id} " +
				"	and r.abbrevName.name1 in ('H', 'R', 'E', 'S') " +
				"	order by r.abbrevName.name1 ";
	}
	
	@Factory(value = "substanceList")
	public List<Substance> getSubstanceList(){
		return getResultList();
	}

}
