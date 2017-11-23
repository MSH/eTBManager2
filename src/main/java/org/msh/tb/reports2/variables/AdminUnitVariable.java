package org.msh.tb.reports2.variables;

import org.jboss.seam.Component;
import org.msh.reports.FilterValue;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.application.App;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Variable to handle administrative unit fields
 * @author Ricardo Memoria
 *
 */
public class AdminUnitVariable extends VariableImpl {

	private List<AdministrativeUnit> adminUnits;

    private String parentCode;
	
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
            Query qry;
            if (parentCode != null) {
                qry = em.createQuery("from AdministrativeUnit where parent.id in (select id from AdministrativeUnit " +
                        " where code = :code and workspace.id = #{defaultWorkspace.id})")
                        .setParameter("code", parentCode);
            }
            else {
                qry = em.createQuery("from AdministrativeUnit where parent is null and workspace.id = #{defaultWorkspace.id}");
            }
			adminUnits = qry.getResultList();
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

        FilterValue val = def.getFilterValue(getId());
        if (val != null && val.getValue() != null) {
            adminUnits = null;
            parentCode = val.getValue();
        }
        else {
            parentCode = null;
        }

/*		TableJoin join = def.addJoin("administrativeunit", "id", s[0], s[1]);
		def.selectField(join.getAlias() + ".code");
*/	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(final SQLDefs def, FilterOperation oper, ValueHandler value) {
		final String alias = def.join("administrativeunit.id", getFieldName()).getAlias();
        final String p = getFieldName().replace('.', '_');

        String s = value.mapSqlOR(new ValueHandler.ValueIterator() {
            @Override
            public String iterate(String value, int index) {
                String pname = p + Integer.toString(index);
                def.addParameter(pname, value + "%");
                return "(" + alias + ".code like :" + pname + ")";
            }
        });

        def.addRestriction(s);

        // is an array ?
/*
		if (value.getClass().isArray()) {
            String[] vals = (String[])value;

            String sql = "";
            int index = 1;
            String p = getFieldName().replace('.', '_');

            // create restriction
            for (String code: vals) {
                String pname = p + Integer.toString(index);
                if (!sql.isEmpty()) {
                    sql += " or ";
                }
                sql += "(" + alias + ".code like :" + pname + ")";
                def.addParameter(pname, code + "%");
                index++;
            }

            sql = "(" + sql + ")";
            def.addRestriction(sql);
        }
        else {
            String p = getFieldName().replace('.', '_');
            def.addRestriction(alias + ".code like :" + p);
            def.addParameter(p, value + "%");
        }
*/
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.ADMINUNIT;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
        if (value == null) {
            return null;
        }

        if (value.indexOf(";") >= 0) {
            String[] vals = value.split(";");
            return vals;
        }
        else {
            return value;
        }
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<AdministrativeUnit> lst;
		List<FilterOption> options = new ArrayList<FilterOption>();

        if (param == null) {
            lst = getAdminUnits();
        }
        else {
            EntityManager em = App.getEntityManager();

            lst = App.getEntityManager().createQuery("from AdministrativeUnit where parent.id = (select min(id)" +
                    "from AdministrativeUnit where code = :code and workspace.id=#{defaultWorkspace.id}) " +
                    "order by name.name1")
                    .setParameter("code", param)
                    .getResultList();
        }

        // fill the options
        for (AdministrativeUnit adm: lst) {
            options.add(new FilterOption(adm.getCode(), adm.getName().toString()));
        }

		return options;
	}

}
