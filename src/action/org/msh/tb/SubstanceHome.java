package org.msh.tb;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.msh.mdrtb.entities.Substance;
import org.msh.tb.log.LogInfo;


@Name("substanceHome")
@LogInfo(roleName="SUBSTANCES")
public class SubstanceHome extends EntityHomeEx<Substance> {
	private static final long serialVersionUID = 8718366857062580485L;

	@In(create=true) EntityQuery<Substance> substances;
	
	@Factory("substance")
	public Substance getSubstance() {
		return getInstance();
	}
	
	@Override
	public String remove() {
		if (substances != null)
			substances.refresh();
		return super.remove();
	}
}
