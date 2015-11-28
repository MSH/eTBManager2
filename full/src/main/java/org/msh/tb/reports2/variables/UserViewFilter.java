package org.msh.tb.reports2.variables;

import org.jboss.seam.security.Identity;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.reports.query.SqlBuilder;
import org.msh.reports.query.TableJoin;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;
import org.msh.tb.reports2.VariableImpl;

/**
 * Filter to restrict user data access. There are two options:
 *  - User view - Just cases that can be seen by the user according to its user view configuration
 *  - User unit - Just the cases of the user unit
 * Created by rmemoria on 27/11/15.
 */
public class UserViewFilter extends EnumFieldVariable {

    public enum ViewRestriction {
        USERVIEW, // restrict data by user view
        USERUNIT  // restrict data by user unit
    }

    public UserViewFilter() {
        super("userview", "manag.reportgen.var.userview", null, ViewRestriction.class);
    }

    @Override
    public boolean isMultiSelection() {
        return false;
    }


    @Override
    public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
        Integer index = value.asInteger();
        if (index == null) {
            return;
        }

        ViewRestriction val = ViewRestriction.values()[index];

        switch (val) {
            case USERVIEW: applyUserViewRestrictions(def, false);
                break;
            case USERUNIT: applyUserViewRestrictions(def, true);
        }
    }



    /**
     * Apply user view restrictions according to the user view configuration
     * @param def The query definition to receive restrictions
     */
    private void applyUserViewRestrictions(SQLDefs def, boolean unitView) {
        if (Identity.instance().isLoggedIn()) {
            UserWorkspace user = UserSession.getUserWorkspace();

            // user is limited to just an administrative unit ?
            if (!unitView && user.getView() == UserView.ADMINUNIT) {
                AdministrativeUnit au = user.getAdminUnit();
                if (au != null) {
                    TableJoin join = def.join("tbunit.id", "tbcase.owner_unit_id").join("adminunit_id", "administrativeunit.id");
                    def.addRestriction("administrativeunit.code like :code_1");
                    def.addParameter("code_1", au.getCode() + "%");
                }
                return;
            }

            // user view is limited to just a health facility ?
            if (unitView || user.getView() == UserView.TBUNIT) {
                Tbunit unit = user.getTbunit();
                if (unit != null) {
                    def.addRestriction("tbcase.owner_unit_id = " + unit.getId());
                }
            }
        }
    }

}
