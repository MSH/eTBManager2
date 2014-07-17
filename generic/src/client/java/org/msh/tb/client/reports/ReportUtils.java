package org.msh.tb.client.reports;

import org.msh.tb.client.AppResources;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CReportUIData;
import org.msh.tb.client.shared.model.CVariable;

/**
 * Created by ricardo on 11/07/14.
 */
public class ReportUtils {

    public static final String KEY_REPORTUI = "report.ui";

    /**
     * Set the report UI data, containing global information, like variables, filters and available reports
     * @return instance of {@link org.msh.tb.client.shared.model.CReportUIData}
     */
    public static CReportUIData getReportUIData() {
        return (CReportUIData) AppResources.instance().get(KEY_REPORTUI);
    }

    public static void setReportUIData(CReportUIData report) {
        AppResources.instance().set(KEY_REPORTUI, report);
    }

    /**
     * Return a variable data by its ID
     * @param id is the ID of the variable
     * @return instance of {@link org.msh.tb.client.shared.model.CVariable}, or null if variable is not found
     */
    public static CVariable findVariableById(String id) {
        CReportUIData data = getReportUIData();

        if ((data == null) || (data.getGroups() == null)) {
            return null;
        }

        for (CGroup grp: data.getGroups()) {
            if (grp.getVariables() != null) {
                for (CVariable var: grp.getVariables()) {
                    if (var.getId().equals(id)) {
                        return var;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Search for a filter by its ID
     * @param id is the filter's ID
     * @return instance of {@link CVariable}, or null if filter is not found
     */
    public static CFilter findFilterById(String id) {
        CReportUIData data = getReportUIData();

        if ((data == null) || (data.getGroups() == null)) {
            return null;
        }

        for (CGroup grp: data.getGroups()) {
            if (grp.getFilters() != null) {
                for (CFilter filter: grp.getFilters()) {
                    if (filter.getId().equals(id)) {
                        return filter;
                    }
                }
            }
        }
        return null;
    }
}
