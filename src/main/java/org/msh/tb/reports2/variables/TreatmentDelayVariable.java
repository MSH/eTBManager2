package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

import java.util.Date;

/**
 * Created by rmemoria on 15/7/15.
 */
public class TreatmentDelayVariable extends VariableImpl {

    private static final int KEY_MEAN = 1;
    private static final int KEY_MINIMUM = 2;
    private static final int KEY_MAXIMUM = 3;

    private int iteraction;


    public TreatmentDelayVariable() {
        super("treatdelay", "manag.reportgen.var.treatdelay", null);
    }


    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        this.iteraction = iteration;

        if (iteration == 0) {
            def.select("sum(datediff(initreatmentdate, diagnosisDate))/count(*)");
            def.select("min(datediff(initreatmentdate, diagnosisdate))");
            def.select("max(datediff(initreatmentdate, diagnosisdate))");

            def.addRestriction("diagnosisDate <= iniTreatmentDate");
        }
    }

    @Override
    public Object createKey(Object values) {
        Object[] vals = (Object[])values;
        return  vals;
    }

    @Override
    public int getIteractionCount() {
        return 3;
    }
}
