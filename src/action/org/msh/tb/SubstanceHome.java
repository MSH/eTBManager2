package org.msh.tb;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Substance;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.utils.EntityQuery;



@Name("substanceHome")
@LogInfo(roleName="SUBSTANCES")
public class SubstanceHome extends EntityHomeEx<Substance> {
	private static final long serialVersionUID = 8718366857062580485L;

	@Factory("substance")
	public Substance getSubstance() {
		return getInstance();
	}
	
	@Override
	public EntityQuery<Substance> getEntityQuery() {
		return (SubstancesQuery)Component.getInstance("substances", false);
	}
}
