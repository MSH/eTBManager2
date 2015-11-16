package org.msh.tb.bd.dat;

import org.jboss.seam.annotations.Name;
import org.msh.tb.reports2.ReportGroup;
import org.msh.tb.reports2.ReportResources;

/**
 * Customization of the DAT for Bangladesh
 *
 * Created by rmemoria on 16/11/15.
 */
@Name("reportResources.bd")
public class ReportResourcesBD extends ReportResources {

    /** {@inheritDoc}
     */
    @Override
    protected ReportGroup addCaseDataVariables() {
        ReportGroup grp = super.addCaseDataVariables();

        // add specific variable for namibia
        add(grp, new FollowupSmearVariable());
        return grp;
    }

}
