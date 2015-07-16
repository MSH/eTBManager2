package org.msh.tb.kh.dat;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.reports2.variables.FieldValueVariable;

/**
 * Created by rmemoria on 16/7/15.
 */
public class FieldValueKHVariable extends FieldValueVariable {
    public FieldValueKHVariable(String id, String keylabel, String fieldName, TbField tbfield) {
        super(id, keylabel, fieldName, tbfield);
    }


    /* (non-Javadoc)
     * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
     */
    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        //def.addJoin("casedatabr", "id", "tbcase", "id");
        def.join("tbcasekh", "id", "tbcase", "id");
        super.prepareVariableQuery(def, iteration);
    }

    /* (non-Javadoc)
     * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
     */
    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
                                   ValueHandler value) {
        super.prepareFilterQuery(def, oper, value);
        //def.addJoin("casedatabr", "id", "tbcase", "id");
        def.join("tbcasekh", "id", "tbcase", "id");
    }


}
