package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.FilterValue;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.reports2.VariableImpl;
import org.msh.utils.date.DateUtils;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Generates interim outcome assessment based on WHO form 6
 * Created by rmemoria on 11/6/15.
 */
public class InterimOutcomeVariable extends VariableImpl {

    public static final int KEY_NOTONTREAT = 1;
    public static final int KEY_OUTINTERIM = 2;
    public static final int KEY_NEGATIVE = 3;
    public static final int KEY_POSITIVE = 4;
    public static final int KEY_UNKNOWN = 5;
    public static final int KEY_DIED = 6;
    public static final int KEY_LOST_FOLLOWUP = 7;
    public static final int KEY_TRANSFERREDOUT = 8;

    // default number of months of interim assessment
    private int intrMonths = 6;


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

        return interimKey(vals);
    }


    /**
     * Calculate the keys based on the case results
     * @param vals Values returned from the database
     * @return the keys of the report
     */
    private Object interimKey(Object[] vals) {
        Date dtIniTreat = (Date)vals[0];
        Integer st = (Integer)vals[1];
        CaseState state = st != null?  CaseState.values()[st]: null;
        Date dtCult = (Date)vals[2];
        Date dtMicro = (Date)vals[3];
        Date dtEndTreat = (Date)vals[4];

        if (dtIniTreat == null) {
            return KEY_NOTONTREAT;
        }

        int intrMonths = 6;
        int treatMonths = DateUtils.monthsBetween(dtIniTreat, dtEndTreat);

        if (treatMonths < intrMonths) {
            return KEY_OUTINTERIM;
        }

        if ((state.ordinal() > CaseState.TRANSFERRING.ordinal()) && (treatMonths <= intrMonths)) {
            return state.ordinal() + 100;
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

        FilterValue val = def.getFilterValue("intmonth");
        if (val != null && val.getValue() != null) {
            int month = Integer.parseInt(val.getValue());
            intrMonths = month;
        }

//        def.addRestriction("tbcase.iniTreatmentDate is not null");
    }

    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
        super.prepareFilterQuery(def, oper, value);
    }

    /* (non-Javadoc)
         * @see org.msh.tb.reports2.variables.CountingVariable#getDisplayText(java.lang.Object)
         */
    @Override
    public String getDisplayText(Object key) {
        int iteration = (Integer)key;

        if (iteration > 100) {
            CaseState st = CaseState.values()[iteration - 100];
            return Messages.instance().get(st.getKey());
        }

        switch (iteration) {
            case KEY_NOTONTREAT: return Messages.instance().get("cases.ongoing.notontreat");
            case KEY_OUTINTERIM: {
                String s = Messages.instance().get("manag.ind.interim.outinterim");
                return MessageFormat.format(s, intrMonths);
            }
            case KEY_NEGATIVE: return Messages.instance().get("manag.ind.interim.negative");
            case KEY_POSITIVE: return Messages.instance().get("manag.ind.interim.positive");
            case KEY_UNKNOWN: return Messages.instance().get("manag.ind.interim.unknown");
            case KEY_LOST_FOLLOWUP: return Messages.instance().get("CaseState.DEFAULTED");
            case KEY_DIED: return Messages.instance().get("CaseState.DIED");
            case KEY_TRANSFERREDOUT: return Messages.instance().get("CaseState.TRANSFERRED_OUT");
        }
        return super.getDisplayText(key);
    }

    @Override
    public boolean isGrouped() {
        return true;
    }

    @Override
    public Object createGroupKey(Object key) {
        Integer val = (Integer) interimKey((Object[])key);

        if (val <= KEY_OUTINTERIM) {
            return 1;
        }

        if (val < 100) {
            return 2;
        }

        return 3;
    }

    @Override
    public String getGroupDisplayText(Object key) {
        if (key != null) {
            int index = (Integer)key;
            switch (index) {
                case 1: return Messages.instance().get("manag.ind.interim.noassess");
                case 2: {
                    String s = Messages.instance().get("manag.ind.interim.title1");
                    return MessageFormat.format(s, intrMonths);
                }
                case 3: return Messages.instance().get("manag.ind.interim.title2");
            }
        }
        return super.getGroupDisplayText(key);
    }
}
