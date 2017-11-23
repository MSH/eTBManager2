package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.login.SessionData;
import org.msh.tb.login.UserSession;
import org.msh.tb.misc.FieldsOptions;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rmemoria on 14/7/15.
 */
public class RifResistanceDetectionVariable extends VariableImpl {

    public static final int KEY_UNDEFINED = 0;
    public static final int KEY_DST = 1;
    public static final int KEY_XPERT = 2;


    public RifResistanceDetectionVariable() {
        super("rifres", "manag.reportgen.var.rifresdetection", null);
    }

    @Override
    public Object createKey(Object values) {
        Object[] vals = (Object[])values;
        Date dtXpert = (Date)vals[0];
        Date dtDst = (Date)vals[1];

        if (dtXpert == null && dtDst == null) {
            return KEY_UNDEFINED;
        }

        if (dtDst == null) {
            return KEY_XPERT;
        }

        if (dtXpert == null || dtDst.before(dtXpert)) {
            return KEY_DST;
        }

        return KEY_XPERT;
    }

    @Override
    public void prepareVariableQuery(SQLDefs def, int iteration) {
        String alias = def.getMasterTable().getAlias();

        String sqlxpert = "(select min(e.dateCollected) from examxpert e " +
                "where e.case_id = " + alias + ".id and e.result = " + XpertResult.TB_DETECTED.ordinal() +
                " and e.rifresult = " + XpertRifResult.RIF_DETECTED.ordinal() + ")";

        String sqldst = "(select min(e.dateCollected) from examdst e " +
                "inner join examdstresult res on res.exam_id=e.id " +
                "inner join substance s on s.id=res.substance_id " +
                "where e.case_id = " + alias + ".id and s.abbrev_name1 = 'R')";

        def.select(sqlxpert);
        def.select(sqldst);
//        super.prepareVariableQuery(def, iteration);
    }

    @Override
    public String getDisplayText(Object key) {
        int index = (Integer)key;
        switch (index) {
            case KEY_DST: return Messages.instance().get("manag.dat.rifresdetect.dst");
            case KEY_XPERT: return Messages.instance().get("manag.dat.rifresdetect.xpert");
            default: return Messages.instance().get("global.noresult");
        }
    }

    @Override
    public List<FilterOption> getFilterOptions(Object param) {
        List<FilterOption> lst = new ArrayList<FilterOption>();
        lst.add(new FilterOption(KEY_XPERT, getDisplayText(KEY_XPERT)));
        lst.add(new FilterOption(KEY_DST, getDisplayText(KEY_DST)));

        return lst;
    }

    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
        Integer key = value.asInteger();
        if (key == null) {
            return;
        }

        String alias = def.getMasterTable().getAlias();

        // create a join table to make it easier to compare dates
        def.join("(select tbcase.id as case_id, " +
                        "(select min(datecollected) from examxpert where case_id=tbcase.id " +
                        "and result=" + XpertResult.TB_DETECTED.ordinal() +
                        " and rifresult=" + XpertRifResult.RIF_DETECTED.ordinal() + ") dtxpert, " +
                        "(select min(e.dateCollected) from examdst e " +
                        "inner join examdstresult res on res.exam_id=e.id " +
                        "inner join substance s on s.id=res.substance_id where case_id=tbcase.id and s.ABBREV_NAME1 = 'R') dtdst " +
                        "from tbcase inner join patient on patient.id=tbcase.patient_id " +
                        "where patient.workspace_id = :wsid2)",
                "case_id",
                "tbcase",
                "id");

        def.addParameter("wsid2", UserSession.getWorkspace().getId());

        String restriction;
        if (key == KEY_XPERT) {
            restriction = "dtxpert is not null and (dtdst is null or dtxpert <= dtdst)";
        }
        else {
            restriction = "dtdst is not null and (dtxpert is null or dtdst < dtxpert)";
        }

        def.addRestriction(restriction);

//        super.prepareFilterQuery(def, oper, value);
    }
}
