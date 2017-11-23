package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;

/**
 * Variable used in Data Analysis Tool to display the Rifampicin resistance of TB detected tests
 *
 * Created by rmemoria on 14/9/15.
 */
public class XpertRifResVariable extends LabResultVariable {

    public XpertRifResVariable() {
        super("genx_rifres", "manag.reportgen.var.xpertrrresult", "examxpert.rifResult", XpertRifResult.class, UnitType.EXAM_XPERT);
    }

    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        super.prepareVariableQuery(def, iteration);
        def.addRestriction("result = " + XpertResult.TB_DETECTED.ordinal());
    }
}
