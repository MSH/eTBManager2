package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.FilterType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 11/6/15.
 */
public class InterimMonthFilter implements Filter {

    private static final String OPT_OVER24 = "over24";
    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation comp, ValueHandler value) {
        // do nothing, because it is used just by the interim report
    }

    @Override
    public String getFilterType() {
        return FilterType.REMOTE_OPTIONS;
    }

    @Override
    public List<FilterOption> getFilterOptions(Object param) {
        List<FilterOption> lst = new ArrayList<FilterOption>();

        for (int i = 1; i < 24; i++) {
            String s = Integer.toString(i);
            lst.add(new FilterOption(s, s));
        }

        String s = MessageFormat.format(Messages.instance().get("manag.reportgen.overmonths"), 24);
        lst.add(new FilterOption(OPT_OVER24, s));
        return lst;
    }

    @Override
    public boolean isFilterLazyInitialized() {
        return false;
    }

    @Override
    public boolean isMultiSelection() {
        return false;
    }

    @Override
    public String getId() {
        return "intmonth";
    }

    @Override
    public String getLabel() {
        return Messages.instance().get("reports.interimMonths");
    }
}
