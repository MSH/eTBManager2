package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.filters.ValueIteratorInt;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;

public class MonthOfTreatVariable extends VariableImpl {

	public MonthOfTreatVariable() {
		super("monthTreat", "manag.reportgen.var.monthtreat", null);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.select("timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate)");
		def.addRestriction("tbcase.endtreatmentdate is not null");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if (values == null) {
			return 0L;
		}
		Long val = ((Long)values) + 1;
		if (val > 36)
			val = 37L;
		return val;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);
		
		if (key.equals(37L))
			return formatMessage("manag.reportgen.over", 36);

		// if range is not found, return undefined 
		return super.getDisplayText(key);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		if (value == null)
			return;

        String sql = value.mapSqlOR(new ValueIteratorInt() {
            @Override
            public String iterateInt(Integer value, int index) {
                if (value == null) {
                    return null;
                }
                String s;
                if (value == 36)
                     s = "(timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate) >= " + value.toString() + ")";
                else s = "(timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate) = " + value.toString() + ")";
                return null;
            }
        });
        def.addRestriction(sql);

/*
        if (value.getClass().isArray()) {
            String sql = "";
            for (String s: (String[])value) {
                Integer val = Integer.parseInt((String)s) - 1;
                if (!sql.isEmpty()) {
                    sql += " or ";
                }
                if (val == 36)
                    sql += "(timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate) >= " + val.toString() + ")";
                else sql += "(timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate) = " + val.toString() + ")";
            }
            def.addRestriction("(" + sql + ")");
        }
        else {
            Integer val = Integer.parseInt((String)value) - 1;
            if (val == 36)
                def.addRestriction("timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate) >= " + val.toString());
            else def.addRestriction("timestampdiff(month, tbcase.initreatmentdate, tbcase.endtreatmentdate) = " + val.toString());
        }
*/
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> lst = new ArrayList<FilterOption>();
		for (int i = 1; i <= 36; i++) {
			lst.add(new FilterOption(i, Integer.toString(i)));
		}
		lst.add(new FilterOption(37, formatMessage("manag.reportgen.over", 36)));
		return lst;
	}

}
