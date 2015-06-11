package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.reports2.VariableImpl;
import org.msh.utils.date.DateUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Generates interim outcome assessment based on WHO form 6
 * Created by rmemoria on 11/6/15.
 */
public class InterimOutcomeVariable extends VariableImpl {

    public static final int KEY_NUMCASES = 1;
    public static final int KEY_NEGATIVE = 2;
    public static final int KEY_POSITIVE = 3;
    public static final int KEY_UNKNOWN = 4;
    public static final int KEY_DIED = 5;
    public static final int KEY_LOST_FOLLOWUP = 6;
    public static final int KEY_TRANSFERREDOUT = 7;

    public InterimOutcomeVariable() {
        super("interim", "manag.reportgen.var.interim", null);
    }

    /* (non-Javadoc)
     * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
     */
    @Override
    public Object createKey(Object values) {
        if (values == null)
            return null;

        Object[] vals = (Object[])values;
        Date dtIniTreat = (Date)vals[0];

        Integer st = (Integer)vals[1];
        CaseState state = st != null?  CaseState.values()[st]: null;
        Date dtCult = (Date)vals[2];
        Date dtMicro = (Date)vals[3];
        Date dtEndTreat = (Date)vals[4];

        return interimKey(dtIniTreat, state, dtCult, dtMicro, dtEndTreat);
    }


    /**
     * Calculate the keys based on the case results
     * @param dtIniTreat initial treatment date
     * @param state case state
     * @param dtCult date of first negative culture
     * @param dtMicro date of first negative microscopy
     * @param dtEndTreatment end of treatment date
     * @return the keys of the report
     */
    private Integer interimKey(Date dtIniTreat, CaseState state, Date dtCult, Date dtMicro, Date dtEndTreatment) {
        int intrMonths = 6;
        int treatMonths = DateUtils.monthsBetween(dtIniTreat, dtEndTreatment);

        if (state == CaseState.DIED) {
            if (treatMonths <= intrMonths) {
                return KEY_DIED;
            }
        }

        if (state == CaseState.DEFAULTED) {
            if (treatMonths <= intrMonths) {
                return KEY_LOST_FOLLOWUP;
            }
        }

        if (state == CaseState.TRANSFERRED_OUT) {
            if (treatMonths <= intrMonths) {
                return KEY_TRANSFERREDOUT;
            }
        }

        // is unknown ?
        if ((dtCult == null) && (dtMicro == null)) {
            return KEY_UNKNOWN;
        }
        else {
            boolean negative = false;

            if ((dtCult != null) && (dtMicro != null) && (dtIniTreat!=null)) {
                int m1 = DateUtils.monthsBetween(dtIniTreat, dtCult);
                int m2 = DateUtils.monthsBetween(dtIniTreat, dtMicro);
                negative = (m1 <= intrMonths) && (m2 <= intrMonths);
            }

            if (negative) {
                return KEY_NEGATIVE;
            }
            else {
                return KEY_POSITIVE;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.msh.tb.reports2.variables.CountingVariable#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
     */
    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        String alias = def.getMasterTable().getAlias();

        String sqlCulture = "(select min(e.dateCollected) from examculture e " +
                "where e.case_id = " + alias + ".id and e.result = " + CultureResult.NEGATIVE.ordinal() + ")";

        String sqlMicroscopy = "(select min(e.dateCollected) from exammicroscopy e " +
                "where e.case_id = " + alias + ".id and e.result = " + MicroscopyResult.NEGATIVE.ordinal() + ")";

        def.select("tbcase.iniTreatmentDate");
        def.select("tbcase.state");
        def.select(sqlCulture);
        def.select(sqlMicroscopy);
        def.select("tbcase.endTreatmentDate");

        def.addRestriction("tbcase.iniTreatmentDate is not null");
    }


    /* (non-Javadoc)
     * @see org.msh.tb.reports2.variables.CountingVariable#getDisplayText(java.lang.Object)
     */
    @Override
    public String getDisplayText(Object key) {
        int iteration = (Integer)key;

        switch (iteration) {
            case KEY_NUMCASES: return Messages.instance().get("manag.ind.interim.starttreat");
            case KEY_NEGATIVE: return Messages.instance().get("manag.ind.interim.negative");
            case KEY_POSITIVE: return Messages.instance().get("manag.ind.interim.positive");
            case KEY_UNKNOWN: return Messages.instance().get("manag.ind.interim.unknown");
            case KEY_LOST_FOLLOWUP: return Messages.instance().get("CaseState.DEFAULTED");
            case KEY_DIED: return Messages.instance().get("CaseState.DIED");
            case KEY_TRANSFERREDOUT: return Messages.instance().get("CaseState.TRANSFERRED_OUT");
        }
        return super.getDisplayText(key);
    }

}
