package org.msh.tb.adminunits;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.utils.EntityQuery;


/**
 * Return the root elements of the administrative unit of the country
 * @author Ricardo Memoria
 *
 */
@Name("adminUnits")
public class AdminUnitsQuery extends EntityQuery<AdministrativeUnit> {
	private static final long serialVersionUID = 6428637361635215953L;

	private static final String[] restrictions = {"a.workspace.id = #{defaultWorkspace.id}",
			"a.parent.id = #{adminUnitHome.parentId}"};
	
	@In(required=false) AdminUnitHome adminUnitHome;


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from AdministrativeUnit a left join fetch a.countryStructure " + getStaticCondition();
	}
		

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from AdministrativeUnit a " + getStaticCondition();
	}


	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getOrder()
	 */
	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			s = "a.name.name1";
		return s;
	}


	/**
	 * Generate HQL static condition to be included in the query
	 * @return HQL condition
	 */
	protected String getStaticCondition() {
		String s = "";
		if ((adminUnitHome == null) || (adminUnitHome.getParentId() == null))
			s = " where a.parent.id is null";
		return s;
	}



}
