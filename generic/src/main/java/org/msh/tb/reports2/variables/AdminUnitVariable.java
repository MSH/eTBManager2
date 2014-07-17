package org.msh.tb.reports2.variables;

import org.jboss.seam.Component;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Variable to handle administrative unit fields
 * @author Ricardo Memoria
 *
 */
public class AdminUnitVariable extends VariableImpl {

	private List<AdministrativeUnit> adminUnits;
	
	public AdminUnitVariable(String id, String label, String field) {
		super(id, label, field);
	}

	
	/**
	 * Return the first level list of administrative units
	 * @return list of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getAdminUnits() {
		if (adminUnits == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			adminUnits = em
					.createQuery("from AdministrativeUnit where parent is null and workspace.id = #{defaultWorkspace.id})")
					.getResultList();
		}
		return adminUnits;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if (values == null)
			return null;

		String code = (String)values;

		// search for administrative unit by its code
		for (AdministrativeUnit au: getAdminUnits()) {
			if (au.isSameOrChildCode(code))
				return au.getCode();
		}

		return null;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);

		for (AdministrativeUnit au: getAdminUnits()) {
			if (au.getCode().equals(key))
				return au.getName().toString();
		}
		
		// if range is not found, return undefined 
		return super.getDisplayText(key);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String s[] = getFieldName().split("\\.");
		
		def.table(s[0])
			.join(s[1], "administrativeunit.id")
			.select("code");
		
/*		TableJoin join = def.addJoin("administrativeunit", "id", s[0], s[1]);
		def.selectField(join.getAlias() + ".code");
*/	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {

//		String s[] = getFieldName().split("\\.");

		String alias = def.join("administrativeunit.id", getFieldName()).getAlias();
		
//		String alias = def.table(s[0]).join(s[1], "administrativeunit.id").getAlias();
		
		String p = getFieldName().replace('.', '_');
		def.addRestriction(alias + ".code like :" + p);
		def.addParameter(p, value + "%");
		
/*		TableJoin join = def.addJoin("administrativeunit", "id", s[0], s[1]);
		String p = getFieldName().replace('.', '_');
		def.addRestriction(join.getAlias() + ".code  like :" + p);
		def.addParameter(p, value + "%");
*/	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		return value;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<AdministrativeUnit> lst = getAdminUnits();
		List<FilterOption> options = new ArrayList<FilterOption>();
		for (AdministrativeUnit adm: lst) {
			options.add(new FilterOption(adm.getCode(), adm.getName().toString()));
		}

		return options;
	}

}
