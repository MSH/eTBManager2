package org.msh.tb.adminunits;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.CountryStructure;
import org.msh.utils.EntityQuery;


/**
 * Query class to keep a list of the administrative unit structure
 * @author Ricardo Memoria
 *
 */
@Name("countryStructures")
public class CountryStructuresQuery extends EntityQuery<CountryStructure> {
	private static final long serialVersionUID = -2771218940643820853L;

	private static final String[] restrictions = {"a.workspace.id = #{defaultWorkspace.id}"};
	
	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from CountryStructure a";
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s != null)
			return s;
		
		return "a.level";
	}
	
	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from CountryStructure a";
	}

}
