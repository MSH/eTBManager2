package org.msh.tb.adminunits;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.log.LogInfo;
import org.msh.utils.EntityQuery;


@Name("countryStructureHome")
@LogInfo(roleName="ADMSTR")
public class CountryStructureHome extends EntityHomeEx<CountryStructure> {
	private static final long serialVersionUID = -3711659526254901707L;

	@In(create=true) CountryLevelInfo countryLevelInfo;
	
	@Factory("countryStructure")
	public CountryStructure getCountryStructure() {
		return getInstance();
	}

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#persist()
	 */
	@Override
	public String persist() {
		countryLevelInfo.refresh();
		return super.persist();
	}

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#remove()
	 */
	@Override
	public String remove() {
		countryLevelInfo.refresh();
		return super.remove();
	}

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#update()
	 */
	@Override
	public String update() {
		countryLevelInfo.refresh();
		return super.update();
	}

	public EntityQuery<CountryStructure> getEntityQuery() {
		return (CountryStructuresQuery)Component.getInstance("countryStructures");
	}
	
}
