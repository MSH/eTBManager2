package org.msh.tb.kh.dat;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.reports2.ReportGroup;
import org.msh.tb.reports2.ReportResources;

/**
 * Created by rmemoria on 16/7/15.
 */
@Name("reportResources.kh")
public class ReportResourcesKH extends ReportResources {
    @Override
    protected ReportGroup addCaseDataVariables() {
        ReportGroup grp = super.addCaseDataVariables();

        add(grp, new FieldValueKHVariable("sustype", "TbCase.suspectType", "tbcasekh.suspecttype", TbField.SUSPECT_TYPE));

        return grp;
    }
}
