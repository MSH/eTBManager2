package org.msh.tb.ge;

import org.jboss.seam.annotations.Name;
import org.msh.tb.ge.entities.IntegrationHistory;
import org.msh.utils.EntityQuery;

@Name("integrationHistQuery")
public class IntegrationHistQuery extends EntityQuery<IntegrationHistory>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4002343991685756312L;
	
	@Override
	public String getEjbql() {
		return " from IntegrationHistory i order by i.lastIntegrationDate ";
	}

}
