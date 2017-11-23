package org.msh.tb.reports2.variables;

import org.msh.reports.query.SQLDefs;
import org.msh.reports.variables.Variable;

/**
 * Define a variable that doesn't include any information in the table.
 * This is to be used if no variable is defined in a column or row
 * Created by Ricardo on 09/07/2014.
 */
public class EmptyVariable implements Variable {

    // the ID of the empty variable
    private String id;

    /**
     * Declare default constructor
     * @param id the id user by this variable
     */
    public EmptyVariable(String id) {
        super();
        this.id = id;
    }

    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        // include a fake field just to count as a column
        def.select('"' + id + '"');
    }

    @Override
    public Object createKey(Object values) {
        return values;
    }

    @Override
    public String getDisplayText(Object key) {
        return "Total";
    }

    @Override
    public int compareValues(Object val1, Object val2) {
        return 0;
    }

    @Override
    public int compareGroupValues(Object val1, Object val2) {
        return 0;
    }

    @Override
    public Object[] getDomain() {
        return null;
    }

    @Override
    public boolean isGrouped() {
        return false;
    }

    @Override
    public Object createGroupKey(Object values) {
        return null;
    }

    @Override
    public String getGroupDisplayText(Object key) {
        return null;
    }

    @Override
    public int getIteractionCount() {
        return 0;
    }

    @Override
    public boolean isTotalEnabled() {
        return false;
    }

    @Override
    public Object getUnitType() {
        return null;
    }

    @Override
    public String getUnitTypeLabel() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return "Total";
    }
}
