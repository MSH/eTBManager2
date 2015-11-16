package org.msh.tb.bd.dat;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.bd.entities.enums.SmearStatus;
import org.msh.tb.reports2.VariableImpl;
import org.msh.tb.reports2.variables.EnumFieldVariable;

/**
 * Created by rmemoria on 16/11/15.
 */
public class FollowupSmearVariable extends EnumFieldVariable {
    public FollowupSmearVariable() {
        super("followupsmear.bd", "SmearStatusTB12", "tbcasebd.followupSmearStatus", SmearStatus.class);
    }

    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        def.join("tbcasebd", "id", "tbcase", "id");
        super.prepareVariableQuery(def, iteration);
    }

    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
        def.join("tbcasebd", "id", "tbcase", "id");
        super.prepareFilterQuery(def, oper, value);
    }
}
