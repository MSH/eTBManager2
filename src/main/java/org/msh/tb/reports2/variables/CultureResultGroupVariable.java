package org.msh.tb.reports2.variables;

import org.msh.tb.entities.enums.CultureResult;

/**
 * Display the culture result grouped by positive, negative, contaminated, pending and others
 * Created by ricardo on 25/08/14.
 */
public class CultureResultGroupVariable extends LabResultVariable {

    public CultureResultGroupVariable() {
        super("cultresgroup", "manag.reportgen.var.cultresgroup", "examculture.result", CultureResult.class, UnitType.EXAM_CULTURE);
    }

    /* (non-Javadoc)
     * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
     */
    @Override
    public Object createKey(Object value) {
        if (value == null) {
            return CultureResult.PENDING;
        }

        if (!(value instanceof Number)) {
            return super.createKey(value);
        }

        CultureResult res = CultureResult.values()[((Number)value).intValue()];

        if (res.isPositive()) {
            return CultureResult.POSITIVE;
        }

        if (res.isNegative()) {
            return CultureResult.NEGATIVE;
        }

        if (res == CultureResult.CONTAMINATED) {
            return res;
        }

        return CultureResult.OTHER;
    }
}
