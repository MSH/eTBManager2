package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;

/**
 * Created by rmemoria on 17/11/15.
 */
public class NotifTreatmentSiteVariable extends AdminUnitVariable {
    public NotifTreatmentSiteVariable() {
        super("ntsite", "manag.reportgen.var.notiftreatsite", "tbunit.adminunit_id");
    }

    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        def.join("tbunit", "id", "tbcase", "owner_unit_id");
        super.prepareVariableQuery(def, iteration);
    }

    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
        def.join("tbunit", "id", "tbcase", "owner_unit_id");
        super.prepareFilterQuery(def, oper, value);
    }
}
