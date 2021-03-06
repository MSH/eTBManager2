package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.filters.ValueIteratorInt;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Variable of report indicator that generates a list of months of treatment
 * where the first exam (culture and/or microscopy) was negative
 * 
 * @author Ricardo Memoria
 *
 */
public class NegativationMonthVariable extends VariableImpl {

	private boolean culture;
	
	public NegativationMonthVariable(String id, boolean culture) {
		super(id, null, null);
		this.culture = culture;

		if (culture) {
			setKeylabel("manag.reportgen.var.cultureneg");
		}
		else {
			setKeylabel("manag.reportgen.var.micneg");
		}
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String tbl;
		if (culture)
			 tbl = "examculture";
		else tbl = "exammicroscopy";

		def.select("timestampdiff(month, tbcase.initreatmentdate, " + tbl + ".dateCollected)");

		def.table("tbcase").join("id", tbl + ".case_id");
        addCommonRestrictions(def);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if ((key == null) || ((new Integer(-1)).equals(key)))
			return Messages.instance().get("global.atdiag");

		if (key.equals(37L))
			return formatMessage("manag.reportgen.over", 36);

		return key.toString();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		Long month = ((Long)values) + 1;

		if (month < 1)
			return -1;
		
		if (month > 36)
			month = 37L;

		return month.intValue();
	}

    protected String getTableName() {
        return culture? "examculture": "exammicroscopy";
    }

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		def.table("tbcase").join("id", getTableName() + ".case_id");
		def.addRestriction("tbcase.initreatmentdate is not null");

        String sql = value.mapSqlOR(new ValueIteratorInt() {
            @Override
            public String iterateInt(Integer value, int index) {
                return sqlFilter(value);
            }
        });
        def.addRestriction(sql);

/*
        if (value.getClass().isArray()) {
            Integer[] values = (Integer[])value;
            String s = "";
            for (Integer val: values) {
                if (!s.isEmpty()) {
                    s += " or ";
                }
                s += sqlFilter(val);
            }
            def.addRestriction("(" + s + ")");
        }
        else {
            String s = sqlFilter((Integer)value);
            def.addRestriction(s);
        }
*/

        addCommonRestrictions(def);
	}

    /**
     * Create filter for the negativation month
     * @param month negativation month, or null if it's before treatment
     * @return sql restriction
     */
    protected String sqlFilter(Integer month) {
        String s = "timestampdiff(month, tbcase.initreatmentdate, " + getTableName() + ".dateCollected) ";

        if (month == null) {
            s += " < 0";
        }
        else {
            int num = (Integer)month;

            if (num < 37)
                s += " = " + Integer.toString(num - 1);
            else s += " >= " + Integer.toString(num - 1);
        }
        return "(" + s + ")";
    }


    /**
     * Add common restrictions to both filter and variable selection
     * @param def the {@link org.msh.reports.query.SQLDefs} implementation
     */
    public void addCommonRestrictions(SQLDefs def) {
        def.addRestriction("tbcase.initreatmentdate is not null");

        if (culture) {
            def.addRestriction("examculture.dateCollected = (select min(exam.dateCollected) " +
                    "from examculture exam where exam.case_id = tbcase.id " +
                    "and result=0)");
        }
        else {
            def.addRestriction("exammicroscopy.dateCollected = (select min(exam.dateCollected) " +
                    "from exammicroscopy exam where exam.case_id = tbcase.id " +
                    "and result=0)");
        }
    }

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		return convertIntFilter(value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> opts = new ArrayList<FilterOption>();
		opts.add(createFilterOption(KEY_NULL));
		for (int i = 1; i <= 37; i++) {
			opts.add(createFilterOption(new Long(i)));
		}
		return opts;
	}

}
